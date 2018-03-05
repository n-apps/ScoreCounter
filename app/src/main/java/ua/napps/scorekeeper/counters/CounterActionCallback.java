package ua.napps.scorekeeper.counters;

import android.view.View;

interface CounterActionCallback {

    void onDecreaseClick(Counter counter);

    void onIncreaseClick(Counter counter);

    void onLongClick(Counter counter, boolean isIncrease);

    void onNameClick(Counter counter);

    void onEditClick(View view, Counter counter);

}
