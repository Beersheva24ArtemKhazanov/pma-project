package telran.pma;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import telran.pma.api.NotificationData;
import telran.pma.api.SettingsData;
import telran.pma.dataSource.ApprovalsDataSource;
import telran.pma.dataSource.EmployeesDataSource;
import telran.pma.dataSource.SettingsDataSource;
import telran.pma.logger.Logger;
import telran.pma.logger.LoggerStandard;

public class AppNotificationProvider implements RequestHandler<Object, String> {
    private static final String DEFAULT_STREAM_NAME = "notifications";
    private static final String DEFAULT_STREAM_CLASS_NAME = "telran.pma.DynamoDbStreamNotificationData";
    private static final String DEFAULT_USER_NAME = "postgres";
    private static final String DEFAULT_DB_CONNECTION_STRING = "jdbc:postgresql://postgres-pma.cul4ey8o8fp4.us-east-1.rds.amazonaws.com:5432/postgres";
    Map<String, String> env = System.getenv();
    String streamName = getStreamName();
    String streamClassName = getStreamClassName();
    String connectionStr = getConnectionString();
    String username = getUsername();
    String password = getPassword();
    Logger logger = new LoggerStandard("schedule-notification");
    MiddlewareDataStream<NotificationData> stream;
    SettingsDataSource settingsDataSource = new SettingsDataSource(connectionStr, username, password, logger);
    ApprovalsDataSource approvalsDataSource = new ApprovalsDataSource(connectionStr, username, password, logger);
    EmployeesDataSource employeesDataSource = new EmployeesDataSource(connectionStr, username, password, logger);

    @SuppressWarnings("unchecked")
    public AppNotificationProvider() {
        logger.log("config", "Stream name is " + streamName);
        logger.log("config", "Stream class name is " + streamClassName);
        try {
            stream = (MiddlewareDataStream<NotificationData>) MiddlewareDataStreamFactory.getStream(streamClassName,
                    streamName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String handleRequest(Object input, Context context) {
        boolean isComplete = false;
        SettingsData settings = settingsDataSource.getSettings();
        ArrayList<HashMap<String, Object>> approvals = approvalsDataSource.getApprovals("toNurse");
        for (HashMap<String, Object> approval : approvals) {
            long interval = (Long) approval.get("timestamp") + settings.intervalForNurse();
            long currentTime = System.currentTimeMillis();
            if (currentTime > interval) {
                HashMap<String, Object> empl = employeesDataSource.getEmployee("Nurse");
                String message = String.format("time to process patient call expired please process call with id: %d", approval.get("patientCallId"));
                NotificationData notify = new NotificationData(empl.get("email").toString(), message);
                logger.log("finest", "NotificationData: " + notify.toString());
                stream.publish(notify);
                isComplete = true;
            }
        }
        return isComplete ? "Function complete" : "Empty Result";
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
}