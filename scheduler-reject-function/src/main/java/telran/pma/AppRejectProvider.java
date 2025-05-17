package telran.pma;

import java.util.Map;
import java.util.UUID;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import telran.pma.api.RejectData;
import telran.pma.api.SaverData;
import telran.pma.api.SettingsData;
import telran.pma.logger.Logger;
import telran.pma.logger.LoggerStandard;

public class AppRejectProvider implements RequestHandler<Object, String> {
    private static final String DEFAULT_STREAM_NAME = "patient-rejects";
    private static final String DEFAULT_STREAM_CLASS_NAME = "telran.pma.DynamoDbStreamRejectData";
    private static final String DEFAULT_USER_NAME = "postgres";
    private static final String DEFAULT_DB_CONNECTION_STRING = "jdbc:postgresql://postgres-pma.cul4ey8o8fp4.us-east-1.rds.amazonaws.com:5432/postgres";
    private final String DEFAULT_DATA_SAVER_CLASS_NAME = "telran.pma.ApprovalUpdaterData";
    Map<String, String> env = System.getenv();
    String streamName = getStreamName();
    String streamClassName = getStreamClassName();
    String connectionStr = getConnectionString();
    String username = getUsername();
    String password = getPassword();
    String dataSaverClassName = getDataSaverClassName();
    Logger logger = new LoggerStandard("reject-provider");
    MiddlewareDataStream<RejectData> stream;
    SettingsDataSource settingsDataSource = new SettingsDataSource(connectionStr, username, password, logger);
    ApprovalsDataSource approvalsDataSource = new ApprovalsDataSource(connectionStr, username, password, logger);
    SaverData dataSaver;

    @SuppressWarnings("unchecked")
    public AppRejectProvider() {
        logger.log("config", "Stream name is " + streamName);
        logger.log("config", "Stream class name is " + streamClassName);
        try {
            stream = (MiddlewareDataStream<RejectData>) MiddlewareDataStreamFactory.getStream(streamClassName,
                    streamName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        logger.log("config", "Data saver class name is " + dataSaverClassName);
        dataSaver = SaverData.getSaverDataInstance(dataSaverClassName, logger);
    }

    @Override
    public String handleRequest(Object input, Context context) {
        boolean isComplete = false;
        SettingsData settings = settingsDataSource.getSettings();
        Map<String, Object>[] approvals = approvalsDataSource.getApprovals();
        for (Map<String, Object> approval : approvals) {
            long interval = (Long)approval.get("timestamp") + settings.intervalForDoctor();
            long currentTime = System.currentTimeMillis();
            if (currentTime > interval) {
                long id = UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;
                String resason = "The doctor's response time has expired";
                RejectData rejectData = new RejectData(id, (Long)approval.get("patientCallId"), resason, currentTime);
                logger.log("finest", "RejectData: " + rejectData.toString());
                approval.put("type", "toNurse");
                logger.log("finest", "Approval for update: " + approval.toString());
                dataSaver.saveData(approval);
                logger.log("finest", "Approval Data updated successfully");
                stream.publish(rejectData);
                isComplete=true;
            }
        }
        return isComplete ? "Function complete" : "Emty Result"; 
    }

    private String getPassword() {
        String password = env.get("DB_PASSWORD");
        if (password == null) {
            throw new RuntimeException("password must be specified in environment variable");
        }
        return password;
    }

    private String getUsername() {
        String username = env.getOrDefault("USERNAME", DEFAULT_USER_NAME);
        return username;
    }

    private String getConnectionString() {
        String connectionString = env.getOrDefault("DB_CONNECTION_STRING", DEFAULT_DB_CONNECTION_STRING);
        return connectionString;
    }

    private String getStreamName() {
        String res = env.getOrDefault("STREAM_NAME", DEFAULT_STREAM_NAME);
        return res;
    }

    private String getStreamClassName() {
        String res = env.getOrDefault("STREAM_CLASS_NAME", DEFAULT_STREAM_CLASS_NAME);
        return res;
    }

    private String getDataSaverClassName() {
        return env.getOrDefault("DATA_SAVER_CLASS_NAME", DEFAULT_DATA_SAVER_CLASS_NAME);
    }

}