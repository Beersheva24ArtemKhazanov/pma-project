package telran.pma;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Map;

import org.apache.log4j.BasicConfigurator;

import telran.pma.api.PatientCallData;
import telran.pma.logger.*;

public class Main {
    private static final int PORT = 5500;
    private static final int MAX_SIZE = 1500;
    private static final String DEFAULT_STREAM_NAME = "patient-calls";
    private static final String DEFAULT_STREAM_CLASS_NAME = "telran.pma.DynamoDbStreamPatientCallData";
    static Logger logger = new LoggerStandard("receiver");
    static Map<String, String> env = System.getenv();


    public static void main(String[] args) {
        BasicConfigurator.configure();
        System.out.println("Application started!");
        try (DatagramSocket socket = new DatagramSocket(PORT);) {
            @SuppressWarnings("unchecked")
            MiddlewareDataStream<PatientCallData> stream = MiddlewareDataStreamFactory.getStream(getDataStreamCalssName(), getTableName());
            byte[] buffer = new byte[MAX_SIZE];
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String jsonStr = new String(packet.getData());
                socket.send(packet);
                stream.publish(PatientCallData.of(jsonStr));
            }
        } catch (Exception e) {
           logger.log("severe", e.toString());
        }
    }

    private static String getTableName() {
        return env.getOrDefault("STREAM_NAME", DEFAULT_STREAM_NAME);
    }
    
    
    private static String getDataStreamCalssName() {
        return env.getOrDefault("DATA_STREAM_CALSS_NAME", DEFAULT_STREAM_CLASS_NAME);
    }
}