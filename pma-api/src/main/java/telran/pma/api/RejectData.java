package telran.pma.api;

import org.json.JSONObject;

public record RejectData(long id, long patientCallId, String reason, long timestamp) {
    public static RejectData of(String jsonStr)  {
        JSONObject json = new JSONObject(jsonStr);
        long id = json.getLong("Id");
        long patientCallId = json.getLong("patientCallId");
        long timestamp = json.getLong("timestamp");
        String reason = json.getString("reason");
        return new RejectData(id, patientCallId, reason,  timestamp);
    }

    @Override
    public String toString() {
        JSONObject json = new JSONObject();
        json.put("Id", id);
        json.put("patientCallId", patientCallId);
        json.put("reason", reason);
        json.put("timestamp", timestamp);
        return json.toString();
    }
}
