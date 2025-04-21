package telran.pma.logger;

public interface Logger {
    String defaultValue = "info";
    void log(String level, String message);
}
