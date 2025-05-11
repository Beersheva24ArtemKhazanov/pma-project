package telran.pma;

import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent.DynamodbStreamRecord;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;

import telran.pma.api.RejectData;
import telran.pma.api.SaverData;
import telran.pma.logger.Logger;
import telran.pma.logger.LoggerStandard;

public class RejectPopulator {
    private final String DEFAULT_DATA_SAVER_CLASS_NAME = "telran.pma.RejectSaverData";
    private Map<String, String> env = System.getenv();
    private String dataSaverClassName = getDataSaverClassName();
    SaverData dataSaver;
    Logger logger = new LoggerStandard("reject-populator");

    public RejectPopulator() {
        logger.log("config", "Data saver class name is " + dataSaverClassName);
        dataSaver = SaverData.getSaverDataInstance(dataSaverClassName, logger);
    }

    public void handleRequest(final DynamodbEvent event, final Context context) {
        event.getRecords().forEach(this::sensorDataProcessing);
    }

    private void sensorDataProcessing(DynamodbStreamRecord record) {
        String eventName = record.getEventName();
        if (eventName.equalsIgnoreCase("INSERT")) {
            Map<String, AttributeValue> map = record.getDynamodb().getNewImage();
            if (map != null) {
                RejectData reject = getReject(map);
                logger.log("finest", "Reject Data is " + reject.toString());
                Map<String, Object> mapForSaving = getMapForSaving(reject);
                logger.log("finest", "map passed to saver: " + mapForSaving.toString());
                dataSaver.saveData(mapForSaving);
                logger.log("finest", "Reject Data saved successfully");
            }
        }
    }

    private Map<String, Object> getMapForSaving(RejectData reject) {
        Map<String, Object> map = Map.of(
                "id", reject.id(),
                "patientCallId", reject.patientCallId(),
                "reason", reject.reason(),
                "timestamp", reject.timestamp());
        return map;
    }

    private RejectData getReject(Map<String, AttributeValue> map) {
        long id = Long.parseLong(map.get("id").getN());
        long patientCallId = Long.parseLong(map.get("patientCallId").getN());
        String reason = map.get("reason").getS();
        long timestamp = Long.parseLong(map.get("timestamp").getN());
        return new RejectData(id, patientCallId, reason, timestamp);
    }

    private String getDataSaverClassName() {
        return env.getOrDefault("DATA_SAVER_CLASS_NAME", DEFAULT_DATA_SAVER_CLASS_NAME);
    }

    
}