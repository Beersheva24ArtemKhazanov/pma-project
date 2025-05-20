package telran.pma;

import java.util.Map;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.SesException;
import telran.pma.logger.Logger;

public class MailSenderSes implements MailSender {
    private static final String DEFAULT_SENDER_EMAIL_ADDRESS = "tulenka04@gmail.com";
    private static final String DEFAULT_REGION_FOR_AWS = "us-east-1";
    Logger logger = loggers[0];
    SesClient sesClient;
    Map<String, String> env = System.getenv();
    String senderEmail = getSenderEmail();
    Region region = getRegion();

    public MailSenderSes() {
        sesClient = SesClient.builder().region(region).build();
    }

    @Override
    public void sendMail(String subject, String mail, String message) {
        try {
            SendEmailRequest emailRequest = SendEmailRequest.builder()
                    .destination(Destination.builder().toAddresses(mail).build())
                    .message(Message.builder()
                            .subject(Content.builder().data(subject).build())
                            .body(Body.builder()
                                    .text(Content.builder().data(message).build())
                                    .build())
                            .build())
                    .source(senderEmail)
                    .build();
            var response = sesClient.sendEmail(emailRequest);
            logger.log("finest", "response: " + response);
        } catch (SesException e) {
            logger.log("severe", "error of sending mail: " + e.awsErrorDetails().errorMessage());
            throw new RuntimeException(e);
        }
    }

    private Region getRegion() {
        String regionStr = env.getOrDefault("REGION_FOR_AWS", DEFAULT_REGION_FOR_AWS);
        logger.log("finest", "region value of the REGION_FOR_AWS variable is " + regionStr);
        return Region.of(regionStr);
    }

    private String getSenderEmail() {
        return env.getOrDefault("SENDER_EMAIL_ADDRESS", DEFAULT_SENDER_EMAIL_ADDRESS);
    }

}
