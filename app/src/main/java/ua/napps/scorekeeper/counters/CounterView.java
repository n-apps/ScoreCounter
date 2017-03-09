package ua.napps.scorekeeper.counters;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import ua.napps.scorekeeper.data.CurrentSet;

public class CounterView extends FrameLayout implements GestureDetector.OnGestureListener {

    private GestureDetector gestureDetector;
    private Counter counter;

    public CounterView(@NonNull Context context) {
        super(context);
        init();
    }

    public CounterView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CounterView(@NonNull Context context, @Nullable AttributeSet attrs,
            @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public CounterView(@NonNull Context context, @Nullable AttributeSet attrs,
            @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        gestureDetector = new GestureDetector(getContext(), this);
    }

    public Counter getCounter() {
        return counter;
    }

    public void setCounter(Counter counter) {
        this.counter = counter;
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override public void onShowPress(MotionEvent e) {

    }

    @Override public boolean onSingleTapUp(MotionEvent e) {
        if (e.getX() > getWidth() / 2) {
            final int newValue = counter.getValue() + 1;
            counter.setValue(newValue);
        } else {
            final int newValue = counter.getValue() - 1;
            counter.setValue(newValue);
        }
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override public void onLongPress(MotionEvent e) {
        CurrentSet.getInstance().removeCounter(counter);
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }
}
