package telran.pma.logger;

import java.util.HashMap;
import java.util.logging.*;

public class LoggerStandard implements Logger {
    java.util.logging.Logger logger;
    static String defaultValue = Logger.defaultValue;
    static HashMap<String, String> levelsMap = new HashMap<>() {{
        put("debug", "config");
        put("trace", "fine");
        put("error", "severe");
    }};
    static void setDefaultLevel(String level) {
        LoggerStandard.defaultValue = level;
    }

    public LoggerStandard(String loggerName) {
        LogManager.getLogManager().reset();
        this.logger = java.util.logging.Logger.getLogger(loggerName);
        String level = System.getenv("LOGGER_LEVEL");
        if (level == null) {
            level = defaultValue;
        }
        String javaLevel = levelsMap.get(level);
        if (javaLevel != null) {
            level = javaLevel;
        }
        Level loggerLevel = Level.parse(level.toUpperCase());
        logger.setLevel(loggerLevel);
        Handler consoHandler = new ConsoleHandler();
        consoHandler.setLevel(loggerLevel);
        logger.addHandler(consoHandler);

    }
    @Override
    public void log(String level, String message) {
        String javaLevel = levelsMap.get(level);
        if (javaLevel != null) {
            level = javaLevel;
        }
        logger.log(Level.parse(level.toUpperCase()), message);
    }

}
