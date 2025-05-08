package telran.pma.api;

import java.util.Arrays;

import org.json.JSONObject;

public record ApprovalData(long id, long patientCallId, long timestamp, Recommendation[] recommendationData) {
    public static ApprovalData of(String jsonStr) {
        JSONObject json = new JSONObject(jsonStr);
        long id = json.getLong("Id");
        long patientCallId = json.getLong("patientCallId");
        long timestamp = json.getLong("timestamp");
        Recommendation[] recommendationData = Arrays.stream(json.getJSONArray("recommendationData").toList().toArray())
                .map(obj -> Recommendation.of(obj.toString()))
                .toArray(Recommendation[]::new);
        return new ApprovalData(id, patientCallId, timestamp, recommendationData);
    }

    @Override
    public String toString() {
        JSONObject json = new JSONObject();
        json.put("Id", id);
        json.put("patientCallId", patientCallId);
        json.put("timestamp", timestamp);
        json.put("recommendationData", Arrays.toString(recommendationData));
        return json.toString();
    }
}
