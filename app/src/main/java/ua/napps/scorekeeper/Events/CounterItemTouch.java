package ua.napps.scorekeeper.Events;

import android.view.MotionEvent;
import android.view.View;

/**
 * Created by novo on 2015-12-26.
 */
public class CounterItemTouch {
    public final MotionEvent event;
    public final View view;
    public final int position;

    public CounterItemTouch(int position, View v, MotionEvent event) {
        this.event = event;
        this.view = v;
        this.position = position;
    }
}
