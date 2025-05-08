package telran.pma;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;

public class S3JsonClient implements S3Client {
    AmazonS3 client = AmazonS3ClientBuilder.standard().withRegion("us-east-1").build();

    @Override
    public String getS3Object(String bucketName, String objectKey) {
        S3Object obj = client.getObject(bucketName, objectKey);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(obj.getObjectContent())))  {
            StringBuilder json = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }
            return json.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error reading S3 object: " + e.getMessage(), e);
        }
    }

}
