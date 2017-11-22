package ua.napps.scorekeeper.counters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import ua.com.napps.scorekeeper.R;
import ua.napps.scorekeeper.counters.CountersAdapter.CountersViewHolder;
import ua.napps.scorekeeper.utils.ColorUtil;

public class CountersAdapter extends RecyclerView.Adapter<CountersViewHolder> {

    public static class CountersViewHolder extends RecyclerView.ViewHolder implements Callback {

        private class LongClickTimerTask extends TimerTask {

            private boolean exec;

            public LongClickTimerTask() {
                exec = true;
            }

            @Override
            public boolean cancel() {
                return super.cancel();
            }

            @Override
            public void run() {
                if (exec) {
                    handler.sendEmptyMessage(MSG_PERFORM_LONGCLICK);
                }
            }

            public void setExecutable(boolean exec) {
                this.exec = exec;
            }

        }

        private static final int MSG_PERFORM_LONGCLICK = 1;

        private final int TIME_LONG_CLICK = 300;

        private Counter counter;

        private final CounterActionCallback counterActionCallback;

        private final FrameLayout counterClickableArea;

        private final TextView counterEdit;

        private final TextView counterName;

        private final TextView counterValue;

        private final ImageView decreaseImageView;

        private final Handler handler;

        private final ImageView increaseImageView;

        private MotionEvent motionEvent;

        private LongClickTimerTask timerTask;

        @SuppressLint("ClickableViewAccessibility")
        CountersViewHolder(View v, CounterActionCallback callback) {
            super(v);
            counterActionCallback = callback;
            handler = new Handler(this);
            counterValue = v.findViewById(R.id.tv_counter_value);
            counterName = v.findViewById(R.id.tv_counter_name);
            counterEdit = v.findViewById(R.id.tv_counter_edit);
            counterClickableArea = v.findViewById(R.id.counter_interaction_area);
            increaseImageView = v.findViewById(R.id.iv_increase);
            decreaseImageView = v.findViewById(R.id.iv_decrease);
            counterName.setOnClickListener(v1 -> counterActionCallback.onNameClick(counter.getId()));
            counterEdit.setOnClickListener(v2 -> counterActionCallback.onEditClick(counter.getId()));

            counterClickableArea.setOnTouchListener(new OnTouchListener() {
                float touchedX, touchedY;

                @Override
                public boolean onTouch(final View v, final MotionEvent event) {
                    switch (event.getActionMasked()) {
                        case MotionEvent.ACTION_DOWN:
                            touchedX = event.getRawX();
                            touchedY = event.getRawY();
                            motionEvent = event;
                            if (timerTask != null) {
                                timerTask.cancel();
                            }
                            // start counting long click time
                            timerTask = new LongClickTimerTask();
                            Timer timer = new Timer();
                            timer.schedule(timerTask, TIME_LONG_CLICK);
                            break;

                        case MotionEvent.ACTION_UP:
                            long time = event.getEventTime() - event.getDownTime();
                            if (time < TIME_LONG_CLICK) {
                                v.performClick();
                                updateCounter(event);
                            }
                            cancelLongClickTask();
                            break;
                        case MotionEvent.ACTION_POINTER_DOWN:
                        case MotionEvent.ACTION_CANCEL:
                        case MotionEvent.ACTION_POINTER_UP:
                            cancelLongClickTask();
                            break;
                    }
                    return false;
                }
            });
        }

        @Override
        public boolean handleMessage(final Message msg) {
            if (msg.what == MSG_PERFORM_LONGCLICK) {
                if (motionEvent != null) {
                    final boolean isIncrease = motionEvent.getX() > counterClickableArea.getWidth() / 2;
                    counterActionCallback.onLongClick(counter, isIncrease);
                }
            }
            return false;
        }

        private void cancelLongClickTask() {
            if (timerTask != null) {
                timerTask.setExecutable(false);
                timerTask.cancel();
            }
            if (motionEvent != null) {
                motionEvent = null;
            }
        }

        private void updateCounter(final MotionEvent e) {
            if (e.getX() > counterClickableArea.getWidth() / 2) {
                counterActionCallback.onIncreaseClick(counter);
            } else {
                counterActionCallback.onDecreaseClick(counter);
            }
        }
    }

    private final CounterActionCallback callback;

    private int containerHeight;

    private int containerWidth;

    private List<Counter> counters;

    private int itemHeight;

    private boolean tryToFitAllCounters;

    CountersAdapter(CounterActionCallback callback) {
        this.callback = callback;
    }

    public int getContainerHeight() {
        return containerHeight;
    }

    public void setContainerHeight(int height) {
        containerHeight = height;
        if (getItemHeight() == 0) {
            setItemHeight(height / getItemCount());
        }

        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return counters == null ? 0 : counters.size();
    }

    public int getItemHeight() {
        return itemHeight;
    }

    public void setItemHeight(int itemHeight) {
        this.itemHeight = itemHeight;
    }

    public boolean isTryToFitAllCounters() {
        return tryToFitAllCounters;
    }

    public void setTryToFitAllCounters(final boolean tryToFitAllCounters) {
        this.tryToFitAllCounters = tryToFitAllCounters;
    }

    @Override
    public void onBindViewHolder(CountersViewHolder holder, int position) {
        final Counter counter = counters.get(position);
        holder.counter = counter;
        holder.counterName.setText(counter.getName());
        holder.counterValue.setText(String.valueOf(counter.getValue()));
        holder.itemView.setBackgroundColor(Color.parseColor(counter.getColor()));
        final boolean darkBackground = ColorUtil.isDarkBackground(counter.getColor());
        final Context context = holder.itemView.getContext();
        final int textColor = ContextCompat
                .getColor(context, darkBackground ? R.color.white : R.color.black);
        holder.counterName.setTextColor(textColor);
        holder.counterValue.setTextColor(textColor);
        holder.counterEdit.setTextColor(textColor);

        Drawable wrapDrawable1 = DrawableCompat.wrap(holder.increaseImageView.getDrawable().mutate());
        Drawable wrapDrawable2 = DrawableCompat.wrap(holder.decreaseImageView.getDrawable().mutate());
        DrawableCompat.setTint(wrapDrawable1, textColor);
        DrawableCompat.setTint(wrapDrawable2, textColor);

        holder.itemView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                holder.itemView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                setItemHeight(holder.itemView.getMeasuredHeight());
            }
        });

        if (!isTryToFitAllCounters()) {
            if (isCounterTooLow()) {// set height as 0.2 of recyclerview height
                RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(containerWidth,
                        getContainerHeight() / 5);
                holder.itemView.setLayoutParams(layoutParams);
            } else { // fit available space
                RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(containerWidth,
                        getContainerHeight() / getItemCount());
                holder.itemView.setLayoutParams(layoutParams);
            }
        } else { // fit available space (also applied for 1st counter)
            RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(containerWidth,
                    getContainerHeight() / getItemCount());
            holder.itemView.setLayoutParams(layoutParams);
        }
    }

    @Override
    public CountersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_counter, parent, false);
        return new CountersViewHolder(v, callback);
    }

    public void setContainerWidth(int widthPixels) {
        containerWidth = widthPixels;
    }

    public void setCountersList(final List<Counter> update) {
        if (counters == null) {
            counters = update;
            notifyItemRangeInserted(0, update.size());
        } else {
            if (update.isEmpty()) {
                setItemHeight(0);
            }

            notifyDataSetChanged();
            counters.clear();
            counters.addAll(update);
        }
    }

    private boolean isCounterTooLow() {
        return getItemCount() > 1 && getItemHeight() <= getContainerHeight() / 5;
    }

}
