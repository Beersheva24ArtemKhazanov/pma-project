package telran.pma;

import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent.DynamodbStreamRecord;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;

import telran.pma.api.PatientCallData;
import telran.pma.api.SaverData;
import telran.pma.logger.Logger;
import telran.pma.logger.LoggerStandard;

public class PatientCallPopulator {
    private static final String DEFAULT_STREAM_NAME = "saved-patient-calls";
    private final String DEFAULT_DATA_SAVER_CLASS_NAME = "telran.pma.PatientCallSaverData";
    private final String DEFAULT_STREAM_CLASS_NAME = "telran.pma.DynamoDbStreamPatientCallData";
    private Map<String, String> env = System.getenv();
    private String streamName = getStreamName();
    private String streamClassName = getStreamClassName();
    private String dataSaverClassName = getDataSaverClassName();
    Logger logger = new LoggerStandard(streamName); 
    MiddlewareDataStream<PatientCallData> dataStream;
    SaverData dataSaver;

    @SuppressWarnings("unchecked")
    public PatientCallPopulator()  {
        logger.log("config", "Stream name is " + streamName);
        try {
            dataStream = (MiddlewareDataStream<PatientCallData>) MiddlewareDataStreamFactory.getStream(streamClassName, streamName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        logger.log("config", "Data saver class name is " + dataSaverClassName);
        dataSaver = SaverData.getSaverDataInstance(dataSaverClassName, logger);
    }

    public void handleRequest(final DynamodbEvent event, final Context context) {
        event.getRecords().forEach(this::sensorDataProcessing);
    }

    private void sensorDataProcessing(DynamodbStreamRecord record) {
        String eventName = record.getEventName();
        if(eventName.equalsIgnoreCase("INSERT")) {
            Map<String, AttributeValue> map = record.getDynamodb().getNewImage();
            if (map != null) {
                PatientCallData patientCall = getPatientCall(map);
                logger.log("finest", patientCall.toString());
                Map<String, Object> mapForSaving = getMapForSaving(patientCall);
                logger.log("finest", "map passed to saver: " + mapForSaving.toString());
                dataSaver.saveData(mapForSaving);
                dataStream.publish(patientCall);
            }
        }
    }

    private Map<String, Object> getMapForSaving(PatientCallData patientCall) {
        Map<String, Object> map = Map.of(
                "patientId", patientCall.patientId(),
                "vasLevel", patientCall.vasLevel(),
                "timestamp", patientCall.timestamp()
        );
        return map;
    }

    private PatientCallData getPatientCall(Map<String, AttributeValue> map) {
        long patientId = Long.parseLong(map.get("patientId").getN());
        int vasLevel = Integer.parseInt(map.get("vasLevel").getN());
        long timestamp = Long.parseLong(map.get("timestamp").getN());
        PatientCallData patientCall = new PatientCallData(patientId, vasLevel, timestamp);
        return patientCall;
    }

    private String getDataSaverClassName() {
        return env.getOrDefault("DATA_SAVER_CLASS_NAME", DEFAULT_DATA_SAVER_CLASS_NAME);
    }

    private String getStreamClassName() {
        return env.getOrDefault("DATA_STREAM_CLASS_NAME", DEFAULT_STREAM_CLASS_NAME);
    }

    private String getStreamName() {
        return env.getOrDefault("STREAM_NAME", DEFAULT_STREAM_NAME);
    }
}