package telran.pma;

import java.util.HashMap;
import java.util.Map;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import telran.pma.api.SavedPatientCallData;

public class DynamoDbStreamSavedPatientCallData extends DynamoDbStream<SavedPatientCallData> {

    public DynamoDbStreamSavedPatientCallData(String table) {
        super(table);
    }

    @Override
    Map<String, AttributeValue> getMap(SavedPatientCallData obj) {
        HashMap<String, AttributeValue> map = new HashMap<>() {{
            put("id", AttributeValue.builder().n(obj.id() + "").build());
            put("patientId", AttributeValue.builder().n(obj.patientId() + "").build());
            put("vasLevel", AttributeValue.builder().n(obj.vasLevel() + "").build());
            put("timestamp", AttributeValue.builder().n(obj.timestamp() + "").build());
        }};
        return map;
    }

}
