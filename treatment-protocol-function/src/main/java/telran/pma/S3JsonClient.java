package telran.pma;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import telran.pma.logger.Logger;

public class S3JsonClient{
    Logger[] loggers = new Logger[1];
    S3Client client;
    
    public S3JsonClient() {
        client = S3Client.builder().region(Region.of("us-east-1")).build();
    }
    public String getS3Object(String bucketName, String objectKey) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(client.getObject(b -> b.bucket(bucketName).key(objectKey))))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (Exception e) {
            loggers[0].log("severe", "error: " + e.getMessage());
        }
        return sb.toString();
    }

}
