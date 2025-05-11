package telran.pma;

import java.util.ArrayList;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent.DynamodbStreamRecord;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;

import telran.pma.api.ApprovalData;
import telran.pma.api.Recommendation;
import telran.pma.api.SaverData;
import telran.pma.logger.Logger;
import telran.pma.logger.LoggerStandard;

public class ApprovalPopulator {
    private final String DEFAULT_APPROVAL_SAVER_CLASS_NAME = "telran.pma.ApprovalSaverData";
    private final String DEFAULT_RECOMMENDATION_SAVER_CLASS_NAME = "telran.pma.RecommendationSaverData";
    private Map<String, String> env = System.getenv();
    private String approvalSaverClassName = getApprovalSaverClassName();
    private String recommendationSaverClassName = getRecommendationSaverClassName();
    Logger logger = new LoggerStandard("approval-populator"); 
    SaverData approvalSaver;
    SaverData recommendationSaver;

    public ApprovalPopulator() {
        logger.log("config", "Approval saver class name is " + approvalSaverClassName);
        approvalSaver = SaverData.getSaverDataInstance(approvalSaverClassName, logger);
        logger.log("config", "Recommendation saver class name is " + recommendationSaverClassName);
        recommendationSaver = SaverData.getSaverDataInstance(recommendationSaverClassName, logger);
    }

    public void handleRequest(final DynamodbEvent event, final Context context) {
        event.getRecords().forEach(this::sensorDataProcessing);
    }

    private void sensorDataProcessing(DynamodbStreamRecord record) {
        String eventName = record.getEventName();
        if(eventName.equalsIgnoreCase("INSERT")) {
            Map<String, AttributeValue> map = record.getDynamodb().getNewImage();
            if (map != null) {
                ApprovalData approval = getApproval(map);
                logger.log("finest", "Approval Data is " + approval.toString());
                Map<String, Object> mapApprovalForSaving = getApprovalMapForSaving(approval);
                logger.log("finest", "map Approval passed to saver: " + mapApprovalForSaving.toString());
                ArrayList<Map<String, Object>> listRecommendationForSaving = getRecommendationListForSaving(approval.recommendationData(), approval.id());
                logger.log("finest", "map Recommendations passed to saver: " + listRecommendationForSaving.toString());
                approvalSaver.saveData(mapApprovalForSaving);
                logger.log("finest", "Approval Data saved successfully");
                listRecommendationForSaving.forEach(recommendation -> {
                    recommendationSaver.saveData(recommendation);
                });
                logger.log("finest", "Recommendation Data saved successfully");
            }
        }
    }

    private ArrayList<Map<String, Object>> getRecommendationListForSaving(Recommendation[] recommendations, long approvalId) {
        ArrayList<Map<String, Object>> list = new ArrayList<>();
        for (Recommendation recommendation : recommendations) {
            Map<String, Object> map = Map.of(
                "id", recommendation.id(),
                "approvalId", approvalId,
                "route", recommendation.route(),
                "activeMoiety", recommendation.activeMoiety(),
                "dosing", recommendation.dosing(),
                "interval", recommendation.interval()
        );
            list.add(map);
        }
        return list;
    }

    private Map<String, Object> getApprovalMapForSaving(ApprovalData approval) {
        Map<String, Object> map = Map.of(
                "id", approval.id(),
                "patientCallId", approval.patientCallId(),
                "type", "toDoctor",
                "isApproved", false,
                "timestamp", approval.timestamp()
        );
        return map;
    }

    private ApprovalData getApproval(Map<String, AttributeValue> map) {
        long id = Long.parseLong(map.get("id").getN());
        long patientCallId = Long.parseLong(map.get("patientCallId").getN());
        long timestamp = Long.parseLong(map.get("timestamp").getN());
        Recommendation[] recommendations = getRecommendations(new JSONArray(map.get("recommendationData").getS()));
        ApprovalData approval = new ApprovalData(id, patientCallId, timestamp, recommendations);
        return approval;
    }

    private Recommendation[] getRecommendations(JSONArray jsonArray) {
        Recommendation[] recommendations = new Recommendation[jsonArray.length()];
        for (int i = 0; i < jsonArray.length(); i++) {
            recommendations[i] = getRecommendation(jsonArray.getJSONObject(i));
        }
        return recommendations;
    }


    private Recommendation getRecommendation(JSONObject jsonObject) {
        long id = jsonObject.getLong("recId");
        String route = jsonObject.getString("route");
        String activeMoiety = jsonObject.getString("activeMoiety");
        String dosing = jsonObject.getString("dosing");
        int interval = jsonObject.getInt("interval");
        Recommendation recommendation = new Recommendation(id, route, activeMoiety, dosing, interval);
        return recommendation;
    }

    private String getRecommendationSaverClassName() {
        return env.getOrDefault("RECOMMENDATION_SAVER_CLASS_NAME", DEFAULT_RECOMMENDATION_SAVER_CLASS_NAME);
    }

    private String getApprovalSaverClassName() {
        return env.getOrDefault("APPROVAL_SAVER_CLASS_NAME", DEFAULT_APPROVAL_SAVER_CLASS_NAME);
    }

}