package ua.napps.scorekeeper.counters;

interface CounterActionCallback {

    void onDecreaseClick(Counter counter);

    void onIncreaseClick(Counter counter);

    void onNameClick(Counter counter);
}
