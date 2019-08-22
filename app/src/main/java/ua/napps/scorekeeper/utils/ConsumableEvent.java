package ua.napps.scorekeeper.utils;

public class ConsumableEvent<T> {

    private final T payload;
    private boolean consumed;

    public ConsumableEvent(T payload) {
        this.payload = payload;
    }

    public T getPayloadIfNotConsumed() {
        if (consumed) return null;
        consumed = true;
        return payload;
    }
}
