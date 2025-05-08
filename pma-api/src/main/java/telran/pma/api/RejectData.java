package telran.pma.api;

import org.json.JSONObject;

public record RejectData(long id, long patientCallId, long timestamp) {
    public static RejectData of(String jsonStr)  {
        JSONObject json = new JSONObject(jsonStr);
        long id = json.getLong("Id");
        long patientCallId = json.getLong("patientCallId");
        long timestamp = json.getLong("timestamp");
        return new RejectData(id, patientCallId, timestamp);
    }

    @Override
    public String toString() {
        JSONObject json = new JSONObject();
        json.put("Id", id);
        json.put("patientCallId", patientCallId);
        json.put("timestamp", timestamp);
        return json.toString();
    }
}
