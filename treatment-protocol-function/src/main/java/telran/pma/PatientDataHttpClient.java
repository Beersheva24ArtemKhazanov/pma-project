package telran.pma;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import telran.pma.api.PatientData;
import telran.pma.logger.Logger;

public class PatientDataHttpClient implements PatientDataClient{
    private String baseURL = getBaseURL();
    HttpClient httpClient = HttpClient.newBuilder()
    .followRedirects(HttpClient.Redirect.ALWAYS)
    .build();
    Logger logger = loggers[0];

    public PatientDataHttpClient() {
        logger.log("config", "HTTP client for communicating with Patient Data Provider Service");
        logger.log("info", "base URL: " + baseURL);
    }

    @Override
    public PatientData getPatientData(long patientId) {
        HttpRequest request = HttpRequest.newBuilder().header("Accept", "application/json").GET().uri(URI.create(getURI(patientId))).build();
        try {
            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
            if (response.statusCode() > 399) {
                throw new Exception(response.body());
            };
            logger.log("finest", "Response is: " + response.body().toString());
            PatientData patientData = PatientData.of(response.body().toString());
            logger.log("finest", "PatientData: " + patientData.toString());
            return patientData;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }         
    }

    private String getURI(long patientId) {
        String uri = baseURL + "?id=" + patientId;
        logger.log("fine", "URI: " + uri);
        return uri;
    }

    private String getBaseURL() {
        String baseURL = System.getenv("PATIENT_DATA_URL");
        if (baseURL == null) {
            throw new RuntimeException("PATIENT_DATA_URL environment variable is not set");
        }
        return baseURL;
    }

}
