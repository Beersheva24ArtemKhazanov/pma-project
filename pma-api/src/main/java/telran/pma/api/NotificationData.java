package telran.pma.api;

import org.json.JSONObject;

public record NotificationData(String email, String message) {
    public static NotificationData of(String jsonStr) {
        JSONObject json = new JSONObject(jsonStr);
        String email = json.getString("email");
        String message = json.getString("message");
        return new NotificationData(email, message);
    }

    @Override
    public String toString() {
        JSONObject json = new JSONObject();
        json.put("email", email);
        json.put("message", message);
        return json.toString();
    }
}
