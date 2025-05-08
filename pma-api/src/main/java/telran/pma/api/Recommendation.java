package telran.pma.api;

import org.json.JSONObject;

public record Recommendation(long id, String route, String activeMoiety, String dosing, int interval) {
    public static Recommendation of(String jsonStr) {
        JSONObject json = new JSONObject(jsonStr);
        String route = json.getString("route");
        long id = json.getLong("recId");
        String activeMoiety = json.getString("activeMoiety");
        String dosing = json.getString("dosing");
        int interval = json.getInt("interval");
        return new Recommendation(id, route, activeMoiety, dosing, interval);
    }

    @Override
    public String toString() {
        JSONObject json = new JSONObject();
        json.put("recId", id);
        json.put("route", route);
        json.put("activeMoiety", activeMoiety);
        json.put("dosing", dosing);
        json.put("interval", interval);
        return json.toString();
    }
}
