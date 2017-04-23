package ua.napps.scorekeeper.counters;

public interface CounterActionCallback {
    void onNameClick(String id);

    void onIncreaseClick(String id);

    void onDecreaseClick(String id);
}
