package telran.pma;

import java.util.HashMap;
import java.util.Map;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import telran.pma.api.PatientCallData;

public class DynamoDbStreamPatientCallData extends DynamoDbStream<PatientCallData> {

    public DynamoDbStreamPatientCallData(String table) {
        super(table);
    }

    @Override
    Map<String, AttributeValue> getMap(PatientCallData obj) {
        HashMap<String, AttributeValue> map = new HashMap<>() {{
            put("patientId", AttributeValue.builder().n(obj.patientId() + "").build());
            put("vasLevel", AttributeValue.builder().n(obj.vasLevel() + "").build());
            put("timestamp", AttributeValue.builder().n(obj.timestamp() + "").build());
        }};
        return map;
    }

}
