package telran.pma.api;


import org.json.JSONObject;

public record SavedPatientCallData(long id, long patientId, int vasLevel, long timestamp) {
    public static SavedPatientCallData of(String jsonStr) {
        JSONObject json = new JSONObject(jsonStr);
        Long id = json.getLong("id");
        long patientId = json.getLong("patientId");
        int vasLevel = json.getInt("vasLevel");
        long timestamp = json.getLong("timestamp");
        return new SavedPatientCallData(id, patientId, vasLevel, timestamp);
    }

    @Override
    public String toString() {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("patientId", patientId);
        json.put("vasLevel", vasLevel);
        json.put("timestamp", timestamp);
        return json.toString();
    }
}
