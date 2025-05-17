package telran.pma.api;

import org.json.JSONObject;

public record SettingsData(long intervalForDoctor, long intervalForNurse) {
    public static SettingsData of(String jsonStr) {
        JSONObject json = new JSONObject(jsonStr);
        long intervalForDoctor = json.getLong("intervalForDoctor");
        long intervalForNurse = json.getInt("intervalForNurse");
        return new SettingsData(intervalForDoctor, intervalForNurse);
    }

    @Override
    public String toString() {
        JSONObject json = new JSONObject();
        json.put("intervalForDoctor", intervalForDoctor);
        json.put("intervalForNurse", intervalForNurse);
        return json.toString();
    }
}
