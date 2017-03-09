package ua.napps.scorekeeper.counters;

public interface CounterActionCallback {
    void onNameClick(Counter counter);

    void onLongClick(Counter counter);
}
