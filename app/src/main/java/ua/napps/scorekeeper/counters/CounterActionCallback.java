package ua.napps.scorekeeper.counters;

import android.view.View;

public interface CounterActionCallback {
    void onNameClick(String id);

    boolean onLongClick(View v, Counter counter);

    void onIncreaseClick(String id);

    void onDecreaseClick(String id);

    void onCounterAdded(View v);
}
