package telran.pma;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import telran.pma.api.ApprovalData;

public class DynamoDbStreamApprovalData extends DynamoDbStream<ApprovalData> {

    public DynamoDbStreamApprovalData(String table) {
        super(table);
    }

    @Override
    Map<String, AttributeValue> getMap(ApprovalData obj) {
        HashMap<String, AttributeValue> map = new HashMap<>() {{
            put("id", AttributeValue.builder().n(obj.id() + "").build());
            put("patientCallId", AttributeValue.builder().n(obj.patientCallId() + "").build());
            put("timestamp", AttributeValue.builder().n(obj.timestamp() + "").build());
            put("recommendationData", AttributeValue.builder().s(Arrays.toString(obj.recommendationData())).build());
        }};
        return map;
    }

}
