package telran.pma;

import telran.pma.logger.LoggerStandard;

import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent.DynamodbStreamRecord;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;

import telran.pma.api.NotificationData;
import telran.pma.logger.Logger;

public class AppEmailNotifier {
    private static final String DEFAULT_EMAIL_SENDER_CLASS_NAME = "telran.pma.MailSenderSes";
    private static final String DEFAULT_EMAIL_SUBJECT = "Patient Call ";
    Logger logger = new LoggerStandard("email-notifier");
    Map<String, String> env = System.getenv();
    String mailSenderClassName = getMailSenderClassName();
    String subject = getSubject();
    MailSender mailSender;

    public AppEmailNotifier() {
        try {
            mailSender = MailSender.getMailSender(mailSenderClassName, logger);
        } catch (Exception e) {
            logger.log("severe", "error: " + e);
            throw new RuntimeException(e);
        }
    }

    public void handleRequest(final DynamodbEvent event, final Context context) {
        event.getRecords().forEach(this::sensorDataProcessing);
    }

    private void sensorDataProcessing(DynamodbStreamRecord record) {
        String eventName = record.getEventName();
        if (eventName.equalsIgnoreCase("INSERT")) {
            Map<String, AttributeValue> map = record.getDynamodb().getNewImage();
            if (map != null) {
                NotificationData notification = getNotification(map);
                logger.log("finest", notification.toString());
                sendMail(notification);
            }
        }
    }

    private void sendMail(NotificationData notification) {
        String id = Pattern.compile("\\d+")
            .matcher(notification.message()).results().map(MatchResult::group)
            .findFirst().orElse(null);
        mailSender.sendMail(subject + id, notification.email(), notification.message());
    }

    private NotificationData getNotification(Map<String, AttributeValue> map) {
        String email = map.get("email").getS();
        String message = map.get("message").getS();
        NotificationData notification = new NotificationData(email, message);
        return notification;
    }

    private String getMailSenderClassName() {
        return env.getOrDefault("EMAIL_SENDER_CLASS_NAME", DEFAULT_EMAIL_SENDER_CLASS_NAME);
    }

    private String getSubject() {
        return env.getOrDefault("EMAIL_SUBJECT", DEFAULT_EMAIL_SUBJECT);
    }
}