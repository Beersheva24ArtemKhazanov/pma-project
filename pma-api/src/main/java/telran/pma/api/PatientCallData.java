package telran.pma.api;

import org.json.JSONObject;

public record PatientCallData(long patientId, int vasLevel, long timestamp) {
    public static PatientCallData of(String jsonStr) {
        JSONObject json = new JSONObject(jsonStr);
        long patientId = json.getLong("patientId");
        int vasLevel = json.getInt("vasLevel");
        long timestamp = json.getLong("timestamp");
        return new PatientCallData(patientId, vasLevel, timestamp);
    }

    @Override
    public String toString() {
        JSONObject json = new JSONObject();
        json.put("patientId", patientId);
        json.put("vasLevel", vasLevel);
        json.put("timestamp", timestamp);
        return json.toString();
    }
}
