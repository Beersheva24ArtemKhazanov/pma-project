package telran.pma;

import telran.pma.logger.Logger;
import telran.pma.logger.LoggerStandard;

public class TestStream<T> implements MiddlewareDataStream<T> {
    String loggerName;
    Logger logger;
    public TestStream(String loggerName) {
        logger = new LoggerStandard(loggerName);
    }

    @Override
    public void publish(T obj) {
        logger.log("info", obj.toString());
    }

}
