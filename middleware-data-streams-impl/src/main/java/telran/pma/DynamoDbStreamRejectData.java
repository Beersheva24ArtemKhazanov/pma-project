package telran.pma;

import java.util.HashMap;
import java.util.Map;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import telran.pma.api.RejectData;

public class DynamoDbStreamRejectData extends DynamoDbStream<RejectData>  {

    public DynamoDbStreamRejectData(String table) {
        super(table);
    }

    @Override
    Map<String, AttributeValue> getMap(RejectData obj) {
       HashMap<String, AttributeValue> map = new HashMap<>() {{
            put("id", AttributeValue.builder().n(obj.id() + "").build());
            put("patientCallId", AttributeValue.builder().n(obj.patientCallId() + "").build());
            put("reason", AttributeValue.builder().s(obj.reason()).build());
            put("timestamp", AttributeValue.builder().n(obj.timestamp() + "").build());
        }};
        return map;
    }

}
