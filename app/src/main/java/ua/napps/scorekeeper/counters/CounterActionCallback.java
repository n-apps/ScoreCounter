package ua.napps.scorekeeper.counters;

interface CounterActionCallback {

    void onSingleClick(Counter counter, int position, int mode);

    void onLongClick(Counter counter, int position, int mode);

    void onNameClick(Counter counter);

    void onEditClick(Counter counter);

}
