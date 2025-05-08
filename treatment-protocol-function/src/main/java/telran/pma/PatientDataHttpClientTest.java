package telran.pma;

import org.json.JSONObject;

import telran.pma.api.PatientData;

public class PatientDataHttpClientTest implements PatientDataClient {

    @Override
    public PatientData getPatientData(long patientId) {
        JSONObject json = new JSONObject();
        json.put("id", patientId);
        json.put("age", 17);
        json.put("weight", 49);
        json.put("childPugh", "B");
        json.put("gfr", 31);
        json.put("plt", 123);
        json.put("wbc", 5.6);
        json.put("sat", 100);
        json.put("sodium", 140);
        json.put("sensetivity", "");
        json.put("contraindications", new String[] { "537.201", "571.301" });
        return PatientData.of(json.toString());
    }

}
