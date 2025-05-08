package telran.monitoring;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.apache.logging.log4j.*;
import org.apache.poi.ss.usermodel.Cell;
import org.json.*;

public class ConverterSchema {
	private static final String FIRST = "first";
	private static final String SECOND = "second";
	private static final String DRUG = "drug";
	private static final String ACTIVE_MOIETY = "active_moiety";
	private static final String DOSING = "dosing";
	private static final String MAX_THRESHOULD = "max_threshold";
	private static final String MIN_THRESHOULD = "min_threshold";
	private static final String AVOID = "avoid";
	private static final String AGE_ADJUSTMENT = "age_adjustment";
	private static final String INTERVAL = "interval";
	private static final String WEIGHT = "weight";
	private static final String CLASS = "class";
	private static final String CHILD_PUGH = "child_pugh";
	private static final String GFR = "gfr";
	private static final String FIRST_DRUG_ONLY = "first_drug_only";
	private static final String PLT = "plt";
	private static final String WBC = "wbc";
	private static final String SAT = "sat";
	private static final String SODIUM = "sodium";
	private static final String SENSITIVITY = "sensitivity";
	private static final String CONTRAINDICATIONS = "contraindications";
	private static String currentPainLevel = "";
	private static LinkedHashMap<String, JSONArray> painJSONObjects;
	private static JSONObject firstJSONObject;
	private static JSONObject secondJSONObject;
	private static JSONObject rowJSONObj;
	private static Logger logger = ExcelConverterAppl.logger;

	public static LinkedHashMap<String, Consumer<Cell>> getHandlers(LinkedHashMap<String, JSONArray> painJSONObjects) {
		ConverterSchema.painJSONObjects = painJSONObjects;
		return new LinkedHashMap<String, Consumer<Cell>>() {
			{
				put("pain_level", ConverterSchema::painLevelHandler);
				put("regimen_hierarchy", ConverterSchema::hierarchyHandler);
				put("route", ConverterSchema::routeHandler);
				put("first_drug", ConverterSchema::firstDrugHandler);
				put("1st_active_moiety", ConverterSchema::firstActiveMoietyHandler);
				put("1st_dosing_(mg)", ConverterSchema::firstDosingHandler);
				put("1st_age_adjustments_(avoid_if_years)", ConverterSchema::firstAgeAdjustmentHandler);
				put("1st_interval_(hrs)", ConverterSchema::firstIntervalHandler);
				put("1st_weight_(kg)", ConverterSchema::firstWeightHandler);
				put("1st_child_pugh_(class)", ConverterSchema::firstChildPughHandler);
				put("2nd_active_moiety", ConverterSchema::secondActiveMoietyHandler);
				put("2nd_dosing_(mg)", ConverterSchema::secondDosingHandler);
				put("2nd_age_adjustments_(avoid_if)", ConverterSchema::secondAgeAdjustmentHandler);
				put("2nd_interval_(hrs)", ConverterSchema::secondIntervalHandler);
				put("2nd_weight_(kg)", ConverterSchema::secondWeightHandler);
				put("2st_child_pugh_(class)", ConverterSchema::secondChildPughHandler);
				put("gfr_(ml/min)", ConverterSchema::gfrHandler);
				put("plt_(avoid_if_k/µl)", ConverterSchema::pltHandler);
				put("wbc_(avoid_if_10e3/microliter)", ConverterSchema::wbcHandler);
				put("sat_(avoid_if_%)", ConverterSchema::satHandler);
				put("sodium_(avoid_if_meq/l)", ConverterSchema::sodiumHandler);
				put("sensitivity_(avoid_if)", ConverterSchema::sensitivityHandler);
				put("contraindications", ConverterSchema::contraindicationsHandler);
			}
		};
	}

	static void painLevelHandler(Cell cell) {

		String level = getStringValue(cell);
		if (!currentPainLevel.equals(level)) {
			currentPainLevel = level;

		}
		rowJSONObj = new JSONObject();
		firstJSONObject = new JSONObject();

		rowJSONObj.put(FIRST, firstJSONObject);
		painJSONObjects.computeIfAbsent(currentPainLevel, k -> new JSONArray()).put(rowJSONObj);

	}

	static void hierarchyHandler(Cell cell) {
		JSONObject jsonObj = getJSONObject();
		String value = getStringValue(cell);
		jsonObj.put("hierarchy", (int) Double.parseDouble(value));

	}

	private static String getStringValue(Cell cell) {
		String value = null;
		try {
			value = cell.getStringCellValue();
			logger.trace("value: {} ", value);
			if (value == null || value.isBlank() || value.isEmpty() || value.equalsIgnoreCase("NA")) {
				value = null;
			}
		} catch (Exception e) {
			value = cell.getNumericCellValue() + "";
		}
		return value;
	}

	private static JSONObject getJSONObject() {
		return rowJSONObj;
	}

	static void routeHandler(Cell cell) {
		JSONObject jsonObject = getJSONObject();
		String value = getStringValue(cell);
		jsonObject.put("route", value);

	}

	static void firstDrugHandler(Cell cell) {
		getFirstStringArray(cell, DRUG);
	}

	private static void getFirstStringArray(Cell cell, String key) {
		JSONObject firstJsonObj = getFirstJSONObject();
		getStringArray(cell, key, firstJsonObj);
	}

	private static void getSecondStringArray(Cell cell, String key) {
		JSONObject secondJsonObj = getSecondJSONObject();
		getStringArray(cell, key, secondJsonObj);
	}

	private static void getStringArray(Cell cell, String key, JSONObject jsonObj) {
		String value = getStringValue(cell);
		if (value != null) {
			String[] valueArray = value.split("\n|OR|\\s");
			JSONArray array = new JSONArray();
			Arrays.stream(valueArray).filter(s -> !s.isEmpty() && !s.isBlank()).map(String::strip).forEach(dv -> array.put(dv));
			jsonObj.put(key, array);
		}

	}

	private static JSONObject getFirstJSONObject() {

		return firstJSONObject;
	}

	private static JSONObject getSecondJSONObject() {
		JSONObject jsonObject = getJSONObject();
		if (secondJSONObject == null) {
			secondJSONObject = new JSONObject();
			jsonObject.put(SECOND, secondJSONObject);
		}

		return secondJSONObject;
	}

	static void firstActiveMoietyHandler(Cell cell) {
		getFirstStringArray(cell, ACTIVE_MOIETY);
	}

	static void firstDosingHandler(Cell cell) {
		JSONObject jsonObject = getFirstJSONObject();
		String value = getStringValue(cell);
		jsonObject.put(DOSING, value);
	}

	static void firstAgeAdjustmentHandler(Cell cell) {
		ageAjustment(cell, ConverterSchema::getFirstJSONObject);

	}

	private static void ageAdjustmentHandler(String value, JSONObject jsonObject) {
		ifAvoidTrue(value, jsonObject, AGE_ADJUSTMENT, 2);
	}

	private static void ifAvoidTrue(String value, JSONObject jsonObject, String key, int nDigits) {
		JSONObject nestedJSONObj = new JSONObject();
		int indexGreater = value.indexOf('>');
		int indexLess = value.indexOf('<');
		int offSet = 1 + nDigits;
		if (indexGreater > -1) {
			nestedJSONObj.put(MAX_THRESHOULD, Integer.parseInt(value, indexGreater + 1, indexGreater + offSet, 10));
		}
		if (indexLess > -1) {
			nestedJSONObj.put(MIN_THRESHOULD, Integer.parseInt(value, indexLess + 1, indexLess + offSet, 10));
		}
		if (indexGreater > -1 || indexLess > -1) {
			nestedJSONObj.put(AVOID, true);
		}
		if (!nestedJSONObj.isEmpty()) {
			jsonObject.put(key, nestedJSONObj);
		}
	}

	static void firstIntervalHandler(Cell cell) {
		intervalHandler(cell, ConverterSchema::getFirstJSONObject);
	}

	private static void intervalHandler(Cell cell, Supplier<JSONObject> jsonObjectSupplier) {
		JSONObject jsonObj = jsonObjectSupplier.get();
		String value = getStringValue(cell);
		if (value != null) {
			int intervalValue;
			try {
				intervalValue = (int) Double.parseDouble(value);
			} catch (NumberFormatException e) {
				intervalValue = 8;
				System.out.println(String.format("row: %d;" + " column: %c - %s", cell.getRowIndex() + 1,
						'A' + cell.getColumnIndex(), value));
			}
			jsonObj.put(INTERVAL, intervalValue);
		}

	}

	static void firstWeightHandler(Cell cell) {
		JSONObject jsonObj = getFirstJSONObject();

		weightAdjustment(cell, jsonObj);

	}

	private static void weightAdjustment(Cell cell, JSONObject jsonObj) {
		String value = getStringValue(cell);
		if (value != null) {
			minThresholdAjustment(value, jsonObj, WEIGHT, new JSONObject(), 2);
		}

	}

	private static void minThresholdAjustment(String value, JSONObject jsonObj, String key, JSONObject nestedJsonObj,
			int nDigits) {
		nestedJsonObj = new JSONObject();
		int indexThreshold = value.indexOf('<');
		if (indexThreshold > -1) {
			int firstIndex = indexThreshold + 1;
			int secondIndex = firstIndex + nDigits;
			int threshold = Integer.parseInt(value, firstIndex, secondIndex, 10);
			nestedJsonObj.put(MIN_THRESHOULD, threshold);
			fillNestedJSONObj(value, nestedJsonObj);
			jsonObj.put(key, nestedJsonObj);

		}
	}

	private static void fillNestedJSONObj(String value, JSONObject nestedJsonObj) {
		value = value.replaceAll("\\s", "");
		int indexDosing = value.indexOf("mg");
		int indexInterval = value.indexOf('h');
		if (indexInterval > -1) {
			int startIndexInterval = getStartIndex(indexInterval, value);
			int interval = Integer.parseInt(value, startIndexInterval, indexInterval, 10);
			nestedJsonObj.put(INTERVAL, interval);
		}
		if (indexDosing > -1) {
			int startIndexDosing = getStartIndex(indexDosing, value);
			int dosing = Integer.parseInt(value, startIndexDosing, indexDosing, 10);
			nestedJsonObj.put(DOSING, dosing);
		}
		if (value.indexOf(AVOID) > -1) {
			nestedJsonObj.put(AVOID, true);
		}
	}

	private static int getStartIndex(int indexUnit, String value) {
		int startIndex = indexUnit - 1;
		while (startIndex - 1 > -1 && Character.isDigit(value.charAt(startIndex - 1))) {
			startIndex--;
		}
		return startIndex;
	}

	static void firstChildPughHandler(Cell cell) {
		childPughHandler(cell, ConverterSchema::getFirstJSONObject);
	}

	static void childPughHandler(Cell cell, Supplier<JSONObject> jsonObjectSupplier) {
		JSONObject jsonObj = jsonObjectSupplier.get();
		String value = getStringValue(cell);
		if (value != null) {
			JSONArray nestedJsonArray = new JSONArray();
			String[] valueParts = value.split("\n");
			for (String part : valueParts) {
				JSONObject nestedJsonObj = new JSONObject();
				nestedJsonObj.put(CLASS, part.substring(0, 1));
				fillNestedJSONObj(part, nestedJsonObj);
				nestedJsonArray.put(nestedJsonObj);
			}
			jsonObj.put(CHILD_PUGH, nestedJsonArray);
		}
	}

	static void secondActiveMoietyHandler(Cell cell) {
		getSecondStringArray(cell, ACTIVE_MOIETY);

	}

	static void secondDosingHandler(Cell cell) {
		JSONObject jsonObject = getSecondJSONObject();
		String value = getStringValue(cell);
		jsonObject.put(DOSING, value);
	}

	static void secondAgeAdjustmentHandler(Cell cell) {
		ageAjustment(cell, ConverterSchema::getSecondJSONObject);
	}

	static void ageAjustment(Cell cell, Supplier<JSONObject> jsonObjectSupplier) {
		String value = getStringValue(cell);
		if (value != null) {
			JSONObject jsonObject = jsonObjectSupplier.get();

			ageAdjustmentHandler(value, jsonObject);
		}
	}

	static void secondIntervalHandler(Cell cell) {
		intervalHandler(cell, ConverterSchema::getSecondJSONObject);
	}

	static void secondWeightHandler(Cell cell) {
		JSONObject jsonObj = getSecondJSONObject();
		weightAdjustment(cell, jsonObj);
	}

	static void secondChildPughHandler(Cell cell) {
		childPughHandler(cell, ConverterSchema::getSecondJSONObject);
	}

	static void gfrHandler(Cell cell) {
		String value = getStringValue(cell);
		if (value != null) {
			JSONObject nestedJsonObj = new JSONObject();
			minThresholdAjustment(value, getJSONObject(), GFR, nestedJsonObj, 2);
			if (value.indexOf("1st") > -1) {
				nestedJsonObj.put(FIRST_DRUG_ONLY, true);
			}

		}
	}

	static void pltHandler(Cell cell) {
		String value = getStringValue(cell);
		if (value != null) {
			JSONObject jsonObject = getJSONObject();
			ifAvoidTrue(value, jsonObject, PLT, 3);
		}
	}

	static void wbcHandler(Cell cell) {
		String value = getStringValue(cell);
		if (value != null) {
			JSONObject jsonObject = getJSONObject();
			ifAvoidTrue(value, jsonObject, WBC, 1);
		}
	}

	static void satHandler(Cell cell) {
		String value = getStringValue(cell);
		if (value != null) {
			JSONObject jsonObject = getJSONObject();
			ifAvoidTrue(value, jsonObject, SAT, 2);
		}
	}

	static void sodiumHandler(Cell cell) {
		String value = getStringValue(cell);
		if (value != null) {
			JSONObject jsonObject = getJSONObject();
			ifAvoidTrue(value, jsonObject, SODIUM, 2);
		}
	}

	static void sensitivityHandler(Cell cell) {
		JSONObject jsonObj = getJSONObject();
		getStringArray(cell, SENSITIVITY, jsonObj);
	}

	static void contraindicationsHandler(Cell cell) {
		JSONObject jsonObj = getJSONObject();
		getStringArray(cell, CONTRAINDICATIONS, jsonObj);
	}
}
