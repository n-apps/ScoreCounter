package ua.napps.scorekeeper.counters;

interface CounterActionCallback {

    void onDecreaseClick(Counter counter);

    void onIncreaseClick(Counter counter);

    void onLongClick(Counter counter, boolean isIncrease);

    void onNameClick(int counterId);

    void onEditClick(int counterId);

}
