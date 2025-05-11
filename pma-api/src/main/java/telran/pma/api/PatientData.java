package telran.pma.api;

import org.json.JSONObject;

public record PatientData(long id, int age, int weight, String childPugh, int gfr, int plt, double wbc, int sat, int sodium, String sensetivity, String[] contraindications) {
    public static PatientData of(String jsonStr) {
        JSONObject json = new JSONObject(jsonStr);
        long id = json.getLong("id");
        int age = json.getInt("age");
        int weight = json.getInt("weight");
        String childPugh = json.getString("childPugh");
        int gfr = json.getInt("gfr");
        int plt = json.getInt("plt");
        double wbc = json.getDouble("wbc");
        int sat = json.getInt("sat");
        int sodium = json.getInt("sodium");
        String sensetivity = json.optString("sensetivity");
        String[] contraindications = json.optJSONArray("contraindications") != null ? 
            json.getJSONArray("contraindications").toList().toArray(new String[0]) : 
            null;
        return new PatientData(id, age, weight, childPugh, gfr, plt, wbc, sat, sodium, sensetivity, contraindications);
    }

    @Override
    public String toString() {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("age", age);
        json.put("weight", weight);
        json.put("childPugh", childPugh);
        json.put("gfr", gfr);
        json.put("plt", plt);
        json.put("wbc", wbc);
        json.put("sat", sat);
        json.put("sodium", sodium);
        json.put("sensetivity", sensetivity);
        if (contraindications != null) {
            json.put("contraindications", contraindications);
        }
        return json.toString();
    }
}
