package telran.pma;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Random;
import java.util.stream.IntStream;

import telran.pma.api.PatientCallData;
import telran.pma.logger.Logger;
import telran.pma.logger.LoggerStandard;

public class Main {
    static Logger logger = new LoggerStandard("imitator");
    static final int MIN_VAS_LEVEL = 1;
    static final int MAX_VAS_LEVEL = 10;
    static final int TIMEOUT_SEND = 500;
    static final int TIMEOUT_RESPONSE = 10000;
    static final String DEFAULT_HOST = "localhost";
    static final int DEFAULT_PORT = 5500;
    static final int DEFAULT_N_PATIENTS = 10;
    static final int DEFAULT_N_PACKETS = 20;
    static final long PATIENT_ID_FOR_INFO_LOGGING = 3;
    static HashMap<Long, Integer> patientIdVasLevel = new HashMap<>();
    static DatagramSocket socket = null;
    public static void main(String[] args) throws Exception {
        socket = new DatagramSocket();
        socket.setSoTimeout(TIMEOUT_RESPONSE);
        IntStream.rangeClosed(1, DEFAULT_N_PACKETS).forEach(Main::send);
    }

    static void send(int i) {
        PatientCallData data = getRandomCallData();
        logger.log("finest", data.toString());
        if (data.patientId() == PATIENT_ID_FOR_INFO_LOGGING) {
            logger.log("info", String.format("VAS level for patient %d is %d", PATIENT_ID_FOR_INFO_LOGGING, data.vasLevel()));
        }
        String jsonStr = data.toString();
        try {
            updSend(jsonStr);
            Thread.sleep(TIMEOUT_SEND);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static PatientCallData getRandomCallData() {
        long patientId = getRandomNumber(1, DEFAULT_N_PATIENTS);
        int vasLevel = getRandomNumber(MIN_VAS_LEVEL, MAX_VAS_LEVEL);
        long timestamp = System.currentTimeMillis();
        return new PatientCallData(patientId, vasLevel, timestamp);
    }

    static int getRandomNumber(int minValue, int maxValue) {
        return new Random().nextInt(minValue, maxValue + 1);
    }

    private static void updSend(String jsonStr) throws IOException {
        logger.log("finest", String.format("data to be sent is %s", jsonStr));
        byte[] buffer = jsonStr.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(DEFAULT_HOST), DEFAULT_PORT);
        socket.send(packet);
        socket.receive(packet);
        if (!jsonStr.equals(new String(packet.getData()))) {
            throw new IOException("Data sent and received are not equal");
        }
    }
}