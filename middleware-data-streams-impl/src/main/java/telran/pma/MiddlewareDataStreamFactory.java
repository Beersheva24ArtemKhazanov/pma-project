package telran.pma;

import java.lang.reflect.Constructor;

public interface MiddlewareDataStreamFactory {
    @SuppressWarnings("rawtypes")
    public static MiddlewareDataStream getStream(String className, String StreamName) throws Exception {
        @SuppressWarnings("unchecked")
        Class<MiddlewareDataStream> clazz = (Class<MiddlewareDataStream>) Class.forName(className);
        Constructor<MiddlewareDataStream> constructor = clazz.getConstructor(String.class);
        return constructor.newInstance(StreamName);
    }
}
