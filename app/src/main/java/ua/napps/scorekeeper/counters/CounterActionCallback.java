package ua.napps.scorekeeper.counters;

import android.view.View;

interface CounterActionCallback {

    void onSingleClick(Counter counter, int mode);

    void onLongClick(Counter counter, int position, int mode);

    void onNameClick(Counter counter);

    void onEditClick(View view, Counter counter);

}
