package telran.pma.api;

import java.util.Map;

import telran.pma.logger.Logger;

public interface SaverData {
    void saveData(Map<String, Object> data);
    static Logger[] loggers = new Logger[1];
    static SaverData getSaverDataInstance(String className, Logger logger) {
        loggers[0] = logger;
        try {
            return (SaverData) Class.forName(className).getConstructor().newInstance();
        } catch (Exception e) {
            logger.log("severe", "error: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
