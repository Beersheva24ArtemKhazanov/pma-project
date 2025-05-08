package telran.pma;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.UUID;
import java.util.stream.IntStream;

import org.json.JSONArray;
import org.json.JSONObject;

import telran.pma.api.PatientData;
import telran.pma.api.Recommendation;

public class CheckingSchema {

    public static ArrayList<Recommendation> getRecommendations(PatientData patientData, int vasLevel, String protocol) {
        ArrayList<Recommendation> recommendations = new ArrayList<>();
        JSONArray jsonPainLevelArray = getArrayOfPainLevel(new JSONArray(protocol), vasLevel);
        for (int i = 0; i < jsonPainLevelArray.length(); i++) {
            ArrayList<Recommendation> fromFlowRec = getRecommendations(jsonPainLevelArray.getJSONObject(i),
                    patientData);
            if (fromFlowRec != null) {
                Iterator<Recommendation> iterator = fromFlowRec.iterator();
                while (iterator.hasNext()) {
                    recommendations.add(iterator.next());
                }
            }
        }
        return recommendations.isEmpty() ? null : recommendations;
    }

    private static ArrayList<Recommendation> getRecommendations(JSONObject flow, PatientData patientData) {
        ArrayList<Recommendation> fromFlowRec = new ArrayList<>();
        boolean avoid = false;
        if (checkFlowOnAvoid(flow, patientData)) {
            avoid = true;
        } else {
            int interval = checkflowOnInterval(flow.getJSONObject("first"), patientData);
            String dosing = checkFlowOnDosing(flow.getJSONObject("first"), patientData);
            String route = flow.getString("route");
            Recommendation recommendationFirst = createRecommendation(interval, dosing, route,
                    flow.getJSONObject("first"));
            fromFlowRec.add(recommendationFirst);
            if (flow.has("second")) {
                interval = checkflowOnInterval(flow.getJSONObject("second"), patientData);
                dosing = checkFlowOnDosing(flow.getJSONObject("second"), patientData);
                Recommendation recommendationSecond = createRecommendation(interval, dosing, route,
                        flow.getJSONObject("second"));
                fromFlowRec.add(recommendationSecond);
            }
        }
        return avoid ? null : fromFlowRec;
    }

    private static String checkFlowOnDosing(JSONObject jsonObject, PatientData patientData) {
        String dosing = jsonObject.getString("dosing");
        if (jsonObject.has("age_adjustment")) {
            JSONObject ageAdjustment = jsonObject.getJSONObject("age_adjustment");
            dosing = ageAdjustment.has("dosing") && ageAdjustment.getInt("max_threshold") < patientData.age()
                    ? String.valueOf(ageAdjustment.getInt("dosing"))
                    : dosing;
        }
        if (jsonObject.has("weight")) {
            JSONObject weight = jsonObject.getJSONObject("weight");
            dosing = weight.has("dosing") && weight.getInt("min_threshold") > patientData.weight()
                    ? String.valueOf(weight.getInt("dosing"))
                    : dosing;
        }
        if (jsonObject.has("child_pugh")) {
            JSONArray childPugh = jsonObject.getJSONArray("child_pugh");
            dosing = IntStream.range(0, childPugh.length())
                    .mapToObj(childPugh::getJSONObject)
                    .filter(obj -> patientData.childPugh().equals(obj.optString("class")))
                    .map(obj -> obj.optString("dosing"))
                    .filter(s -> !s.isEmpty())
                    .findFirst()
                    .orElse(dosing);
        }
        return dosing;
    }

    private static int checkflowOnInterval(JSONObject jsonObject, PatientData patientData) {
        int interval = jsonObject.getInt("interval");
        if (jsonObject.has("age_adjustment")) {
            JSONObject ageAdjustment = jsonObject.getJSONObject("age_adjustment");
            interval = ageAdjustment.has("interval") && ageAdjustment.getInt("max_threshold") < patientData.age()
                    ? ageAdjustment.getInt("interval")
                    : interval;
        }
        if (jsonObject.has("weight")) {
            JSONObject weight = jsonObject.getJSONObject("weight");
            interval = weight.has("interval") && weight.getInt("min_threshold") > patientData.weight()
                    ? weight.getInt("interval")
                    : interval;
        }
        if (jsonObject.has("child_pugh")) {
            JSONArray childPugh = jsonObject.getJSONArray("child_pugh");
            interval = IntStream.range(0, childPugh.length())
                    .mapToObj(childPugh::getJSONObject)
                    .filter(obj -> patientData.childPugh().equals(obj.optString("class")))
                    .map(obj -> obj.optInt("interval"))
                    .filter(n -> n != 0)
                    .findFirst()
                    .orElse(interval);
        }
        return interval;
    }

    private static boolean checkFlowOnAvoid(JSONObject flow, PatientData patientData) {
        boolean res = false;
        res = isContraindicationExists(flow.optJSONArray("contraindications"), patientData.contraindications()) ||
                isSensitivityExists(flow.optJSONArray("sensitivity"), patientData.sensetivity()) ||
                isGfrPltSatSodiumExists(flow.optJSONObject("gfr"), patientData.gfr()) ||
                isGfrPltSatSodiumExists(flow.optJSONObject("plt"), patientData.plt()) ||
                isWbcExists(flow.optJSONObject("wbc"), patientData.wbc()) ||
                isGfrPltSatSodiumExists(flow.optJSONObject("sat"), patientData.sat()) ||
                isGfrPltSatSodiumExists(flow.optJSONObject("sodium"), patientData.sodium());
        if (!res) {
            JSONObject first = flow.optJSONObject("first");
            res = avoidIfMoreAge(first.optJSONObject("age_adjustment"), patientData.age()) ||
                    avoidIfLessWeight(first.optJSONObject("weight"), patientData.weight()) ||
                    avoidInChildPugh(first.optJSONArray("child_pugh"), patientData.childPugh());
            if (flow.has("second")) {
                JSONObject second = flow.optJSONObject("second");
                res = avoidIfMoreAge(second.optJSONObject("age_adjustment"), patientData.age()) ||
                        avoidIfLessWeight(second.optJSONObject("weight"), patientData.weight()) ||
                        avoidInChildPugh(second.optJSONArray("child_pugh"), patientData.childPugh());
            }
        }
        return res;
    }

    private static boolean avoidInChildPugh(JSONArray optJSONArray, String childPugh) {
        boolean res = false;
        if (optJSONArray != null) {
            res = IntStream.range(0, optJSONArray.length())
                    .mapToObj(optJSONArray::getJSONObject)
                    .anyMatch(obj -> childPugh.equals(obj.optString("class")) && obj.optBoolean("avoid", false));
        }
        return res;
    }

    private static boolean avoidIfLessWeight(JSONObject optJSONObject, int weight) {
        boolean res = false;
        if (optJSONObject != null) {
            res = optJSONObject.optBoolean("avoid") ? optJSONObject.getInt("min_threshold") > weight : false;
        }
        return res;
    }

    private static boolean avoidIfMoreAge(JSONObject optJSONObject, int age) {
        boolean res = false;
        if (optJSONObject != null) {
            res = optJSONObject.optBoolean("avoid") ? optJSONObject.getInt("max_threshold") < age : false;
        }
        return res;
    }

    private static boolean isWbcExists(JSONObject optJSONObject, double wbc) {
        boolean res = false;
        if (optJSONObject != null && wbc != 0) {
            res = optJSONObject.optBoolean("avoid") ? optJSONObject.getInt("min_threshold") > wbc : false;
        }
        return res;
    }

    private static boolean isGfrPltSatSodiumExists(JSONObject optJSONObject, int num) {
        boolean res = false;
        if (optJSONObject != null && num != 0) {
            res = optJSONObject.optBoolean("avoid") ? optJSONObject.getInt("min_threshold") > num : false;
        }
        return res;
    }

    private static boolean isSensitivityExists(JSONArray optJSONArray, String sensetivity) {
        boolean res = false;
        if (optJSONArray != null && sensetivity != null) {
            res = IntStream.range(0, optJSONArray.length())
                    .mapToObj(optJSONArray::getString)
                    .anyMatch(sensetivity::equals);
        }
        return res;
    }

    private static boolean isContraindicationExists(JSONArray optJSONArray, String[] contraindications) {
        boolean res = false;
        if (optJSONArray != null && contraindications != null) {
            res = IntStream.range(0, optJSONArray.length())
                    .mapToObj(optJSONArray::getString)
                    .anyMatch(Arrays.asList(contraindications)::contains);
        }
        return res;
    }

    private static Recommendation createRecommendation(int interval, String dosing, String route, JSONObject flow) {
        JSONArray activeMoietyArr = flow.getJSONArray("active_moiety");
        String activeMoiety = String.join(", ", IntStream.range(0, activeMoietyArr.length())
                .mapToObj(activeMoietyArr::getString)
                .toArray(String[]::new));
        long id = UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;
        Recommendation recommendation = new Recommendation(id, route, activeMoiety, dosing, interval);
        return recommendation;
    }

    private static JSONArray getArrayOfPainLevel(JSONArray jsonArray, int vasLevel) {
        JSONArray resultArray = new JSONArray();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String key = jsonObject.keys().next();
            String[] parts = key.split("-");
            int start = Integer.parseInt(parts[0]);
            int end = Integer.parseInt(parts[1]);
            if (vasLevel >= start && vasLevel <= end) {
                resultArray = jsonObject.getJSONArray(key);
            }
        }
        return resultArray;
    }
}
