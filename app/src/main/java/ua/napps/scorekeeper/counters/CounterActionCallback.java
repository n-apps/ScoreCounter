package ua.napps.scorekeeper.counters;

interface CounterActionCallback {

    void onDecreaseClick(Counter counter);

    void onIncreaseClick(Counter counter);

    void onLongClick(Counter counter, boolean isIncrease);

    void onNameClick(Counter counter);

    void onEditClick(int counterId);

}
