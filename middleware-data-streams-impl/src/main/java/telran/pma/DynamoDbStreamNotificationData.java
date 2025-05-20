package telran.pma;

import java.util.HashMap;
import java.util.Map;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import telran.pma.api.NotificationData;

public class DynamoDbStreamNotificationData extends DynamoDbStream<NotificationData> {

    public DynamoDbStreamNotificationData(String table) {
        super(table);
    }

    @Override
    Map<String, AttributeValue> getMap(NotificationData obj) {
        HashMap<String, AttributeValue> map = new HashMap<>() {{
            put("email", AttributeValue.builder().s(obj.email()).build());
            put("message", AttributeValue.builder().s(obj.message()).build());
        }};
        return map;
    }

}
