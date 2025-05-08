package telran.pma;

import telran.pma.logger.Logger;

public interface S3Client {
    Logger[] loggers = new Logger[1];
    String getS3Object(String bucketName, String objectKey);
    static S3Client getS3Client(String providerClassName, Logger logger)  {
        loggers[0] = logger;
        try {
            S3Client client = (S3Client) Class.forName(providerClassName).getDeclaredConstructor().newInstance();
            return client;
        } catch (Exception e) {
            logger.log("severe", "error: " +  e.getMessage()); 
            throw new RuntimeException(e);
        }
    }
}
