package telran.pma;

import telran.pma.api.PatientData;
import telran.pma.logger.Logger;

public interface PatientDataClient {
    Logger[] loggers = new Logger[1];
    PatientData getPatientData (long patientId); 
    static PatientDataClient getPatientDataClient(String providerClassName, Logger logger)  {
        loggers[0] = logger;
        try {
            PatientDataClient client = (PatientDataClient) Class.forName(providerClassName).getDeclaredConstructor().newInstance();
            return client;
        } catch (Exception e) {
            logger.log("severe", "error: " +  e.getMessage()); 
            throw new RuntimeException(e);
        }
    }
}
