package telran.pma;

public interface MiddlewareDataStream<T> {
    void publish(T obj);
}
