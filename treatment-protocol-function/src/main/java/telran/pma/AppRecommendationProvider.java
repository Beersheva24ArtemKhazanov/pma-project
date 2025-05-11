package telran.pma;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent.DynamodbStreamRecord;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;

import telran.pma.api.ApprovalData;
import telran.pma.api.PatientData;
import telran.pma.api.Recommendation;
import telran.pma.api.RejectData;
import telran.pma.api.SavedPatientCallData;
import telran.pma.logger.Logger;
import telran.pma.logger.LoggerStandard;

public class AppRecommendationProvider {
    private static final String DEFAULT_STREAM_APPROVAL_NAME = "patient-approvals";
    private static final String DEFAULT_STREAM_APPROVAL_CLASS_NAME = "telran.pma.DynamoDbStreamApprovalData";
    private static final String DEFAULT_STREAM_REJECT_NAME = "patient-rejects";
    private static final String DEFAULT_STREAM_REJECT_CLASS_NAME = "telran.pma.DynamoDbStreamRejectData";
    private static final String DEFAULT_PATIENT_DATA_PROVIDER_CLASS_NAME = "telran.pma.PatientDataHttpClient";
    private static final String DEFAULT_S3_CLIENT_CLASS_NAME = "telran.pma.S3JsonClient";
    private static final String DEFAULT_S3_BUCKET_NAME = "pma-files-bucket";
    private static final String DEFAULT_S3_PATH_TO_JSON = "pma-protocol.json";

    Map<String, String> env = System.getenv();
    String streamApprovalName = getStreamApprovalName();
    String streamRejectName = getStreamRejectName();
    String streamApprovalClassName = getStreamApprovalClassName();
    String streamRejectClassName = getStreamRejectClassName();
    String patientDataProviderClassName = getProviderClientClassName();
    String s3ClientClassName = getS3ClientClassName();
    String s3BucketName = getS3BucketName();
    String s3PathToJson = getS3PathToJson();
    Logger logger = new LoggerStandard("recommendation-provider");
    PatientDataClient patientDataClient;
    MiddlewareDataStream<ApprovalData> streamApproval;
    MiddlewareDataStream<RejectData> streamReject;
    S3JsonClient s3Client;

    @SuppressWarnings("unchecked")
    public AppRecommendationProvider()  {
        logger.log("config", "Stream approval name is " + streamApprovalName);
        logger.log("config", "Stream approval class name is " + streamApprovalClassName);
        try {
            streamApproval = (MiddlewareDataStream<ApprovalData>) MiddlewareDataStreamFactory.getStream(streamApprovalClassName, streamApprovalName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        logger.log("config", "Stream reject name is " + streamRejectName);
        logger.log("config", "Stream reject class name is " + streamRejectClassName);
        try {
            streamReject = (MiddlewareDataStream<RejectData>) MiddlewareDataStreamFactory.getStream(streamRejectClassName, streamRejectName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        try {
            patientDataClient = PatientDataClient.getPatientDataClient(patientDataProviderClassName, logger);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        try {
            s3Client = new S3JsonClient();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void handleRequest(final DynamodbEvent event, final Context context) {
        event.getRecords().forEach(this::sensorDataProcessing);
    }

    private void sensorDataProcessing(DynamodbStreamRecord record) {
        String eventName = record.getEventName();
        if (eventName.equalsIgnoreCase("INSERT")) {
            Map<String, AttributeValue> map = record.getDynamodb().getNewImage();
            if (map != null)  {
                SavedPatientCallData savedPatientCall = getSavedPatientCall(map);
                logger.log("finest", savedPatientCall.toString());
                PatientData patientData = patientDataClient.getPatientData(savedPatientCall.patientId());
                logger.log("finest", patientData.toString());
                String protocol = s3Client.getS3Object(s3BucketName, s3PathToJson);
                logger.log("finest", "JSON - PROTOCOL: " + protocol);
                ArrayList<Recommendation> recommendationsList = CheckingSchema.getRecommendations(patientData, savedPatientCall.vasLevel(), protocol);
                String forLog = recommendationsList != null ? recommendationsList.toString() : "No recommendations";
                logger.log("finest", "Recommendations: " + forLog);
                long id = UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;
                long timestamp = System.currentTimeMillis();
                if (recommendationsList != null) {
                    Recommendation[] recommendations = recommendationsList.toArray(new Recommendation[recommendationsList.size()]);
                    ApprovalData approvalData = new ApprovalData(id, savedPatientCall.id(), timestamp, recommendations);
                    logger.log("finest", "ApprovalData: " + approvalData.toString());
                    streamApproval.publish(approvalData);
                } else {
                    String resason = "No recommendations for this patient call";
                    RejectData rejectData = new RejectData(id, savedPatientCall.id(), resason, timestamp);
                    logger.log("finest", "RejectData: " + rejectData.toString());
                    streamReject.publish(rejectData);
                }
            }   
        }
    }

    private SavedPatientCallData getSavedPatientCall(Map<String, AttributeValue> map) {
        long id = Long.parseLong(map.get("id").getN());
        long patientId = Long.parseLong(map.get("patientId").getN());
        int vasLevel = Integer.parseInt(map.get("vasLevel").getN());
        long timestamp = Long.parseLong(map.get("timestamp").getN());
        SavedPatientCallData savedPatientCall = new SavedPatientCallData(id, patientId, vasLevel, timestamp);
        return savedPatientCall;
    }

    private String getS3ClientClassName() {
        String res = env.getOrDefault("S3_CLIENT_CLASS_NAME", DEFAULT_S3_CLIENT_CLASS_NAME);
        return res;
    }

    private String getS3PathToJson() {
        String res = env.getOrDefault("S3_PATH_TO_JSON", DEFAULT_S3_PATH_TO_JSON);
        return res;
    }

    private String getS3BucketName() {
        String res = env.getOrDefault("S3_BUCKET_NAME", DEFAULT_S3_BUCKET_NAME);
        return res;
    }

    private String getProviderClientClassName() {
        String res = env.getOrDefault("PATIENT_DATA_PROVIDER_CLASS_NAME", DEFAULT_PATIENT_DATA_PROVIDER_CLASS_NAME);
        return res;
    }

    private String getStreamRejectClassName() {
        String res = env.getOrDefault("STREAM_REJECT_CLASS_NAME", DEFAULT_STREAM_REJECT_CLASS_NAME);
        return res;
    }

    private String getStreamApprovalClassName() {
        String res = env.getOrDefault("STREAM_APPROVAL_CLASS_NAME", DEFAULT_STREAM_APPROVAL_CLASS_NAME);
        return res;
    }

    private String getStreamRejectName() {
        String res = env.getOrDefault("STREAM_REJECT_NAME", DEFAULT_STREAM_REJECT_NAME);
        return res;
    }

    private String getStreamApprovalName() {
        String res = env.getOrDefault("STREAM_APPROVAL_NAME", DEFAULT_STREAM_APPROVAL_NAME);
        return res;
    }
}