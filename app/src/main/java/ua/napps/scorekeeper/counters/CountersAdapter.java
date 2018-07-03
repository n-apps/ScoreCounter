package ua.napps.scorekeeper.counters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import ua.com.napps.scorekeeper.R;
import ua.napps.scorekeeper.counters.CountersAdapter.CountersViewHolder;
import ua.napps.scorekeeper.utils.ColorUtil;

public class CountersAdapter extends RecyclerView.Adapter<CountersViewHolder> {

    public static final String INCREASE_VALUE_CLICK = "increase_value_click";
    public static final String DECREASE_VALUE_CLICK = "decrease_value_click";

    private final CounterActionCallback callback;
    private final int maxFitCounters;
    private int containerHeight;
    private List<Counter> counters;

    CountersAdapter(int maxCountersToFit, CounterActionCallback callback) {
        this.callback = callback;
        this.maxFitCounters = maxCountersToFit;
    }

    public int getMaxFitCounters() {
        return maxFitCounters;
    }

    public void setContainerHeight(int height) {
        containerHeight = height;
    }

    @Override
    public int getItemCount() {
        return counters == null ? 0 : counters.size();
    }

    @Override
    public void onBindViewHolder(@NonNull CountersViewHolder holder, int position) {
        final Counter counter = counters.get(position);
        holder.counter = counter;
        holder.counterName.setText(counter.getName());
        holder.counterValue.setText(String.valueOf(counter.getValue()));
        holder.itemView.setBackgroundColor(Color.parseColor(counter.getColor()));
        final boolean darkBackground = ColorUtil.isDarkBackground(counter.getColor());
        final Context context = holder.itemView.getContext();
        final int textColor = ContextCompat.getColor(context, darkBackground ? R.color.white : R.color.black);
        holder.counterName.setTextColor(textColor);
        holder.counterValue.setTextColor(textColor);
        holder.counterEdit.setTextColor(textColor);

        Drawable wrapDrawable1 = DrawableCompat.wrap(holder.increaseImageView.getDrawable().mutate());
        Drawable wrapDrawable2 = DrawableCompat.wrap(holder.decreaseImageView.getDrawable().mutate());
        DrawableCompat.setTint(wrapDrawable1, textColor);
        DrawableCompat.setTint(wrapDrawable2, textColor);

        int itemCount = getItemCount();
        if (itemCount <= maxFitCounters) { // fit available space
            RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, containerHeight / itemCount);
            holder.itemView.setLayoutParams(layoutParams);
        } else {// set height as recyclerview height / maxFitCounters
            RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, containerHeight / (maxFitCounters > 1 ? maxFitCounters / 2 : 1));
            holder.itemView.setLayoutParams(layoutParams);
        }
    }

    @NonNull
    @Override
    public CountersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_counter, parent, false);
        return new CountersViewHolder(v, callback);
    }

    public void setCountersList(final List<Counter> update) {
        if (counters == null) {
            counters = update;
            notifyItemRangeInserted(0, update.size());
        } else {
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return counters.size();
                }

                @Override
                public int getNewListSize() {
                    return update.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return counters.get(oldItemPosition).getId() == update.get(newItemPosition).getId();
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    Counter newCounter = update.get(newItemPosition);
                    Counter oldCounter = counters.get(oldItemPosition);
                    return newCounter.getId() == oldCounter.getId()
                            && Objects.equals(newCounter.getName(), oldCounter.getName())
                            && Objects.equals(newCounter.getColor(), oldCounter.getColor())
                            && newCounter.getValue() == oldCounter.getValue();
                }
            });
            counters = update;
            result.dispatchUpdatesTo(this);
        }
    }

    public class CountersViewHolder extends RecyclerView.ViewHolder implements Callback {

        private static final int MSG_PERFORM_LONGCLICK = 1;

        public final ImageView decreaseImageView;
        public final ImageView increaseImageView;
        public final TextView counterValue;

        private final int TIME_LONG_CLICK = 300;
        private final CounterActionCallback counterActionCallback;
        private final FrameLayout counterClickableArea;
        private final TextView counterEdit;
        private final TextView counterName;
        private final Handler handler;
        private Counter counter;
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
            counterName.setOnClickListener(v1 -> counterActionCallback.onNameClick(counter));
            counterEdit.setOnClickListener(v2 -> counterActionCallback.onEditClick(v, counter));

            counterClickableArea.setOnTouchListener((v12, event) -> {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
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
                            v12.performClick();
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
            });
        }

        @Override
        public boolean handleMessage(final Message msg) {
            if (msg.what == MSG_PERFORM_LONGCLICK) {
                if (motionEvent != null) {
                    final boolean isIncrease = motionEvent.getX() > counterClickableArea.getWidth() / 2;
                    counterActionCallback.onLongClick(counter, getAdapterPosition(), isIncrease);
                }
            }
            return false;
        }

        private void cancelLongClickTask() {
            if (timerTask != null) {
                timerTask.setNonExecutable();
                timerTask.cancel();
            }
            if (motionEvent != null) {
                motionEvent = null;
            }
        }

        private void updateCounter(final MotionEvent e) {
            if (e.getX() > counterClickableArea.getWidth() / 2) {
                notifyItemChanged(getAdapterPosition(), INCREASE_VALUE_CLICK);
                counterActionCallback.onIncreaseClick(counter);
            } else {
                notifyItemChanged(getAdapterPosition(), DECREASE_VALUE_CLICK);
                counterActionCallback.onDecreaseClick(counter);
            }
        }

        private class LongClickTimerTask extends TimerTask {

            private boolean exec;

            LongClickTimerTask() {
                exec = true;
            }

            @Override
            public void run() {
                if (exec) {
                    handler.sendEmptyMessage(MSG_PERFORM_LONGCLICK);
                }
            }

            void setNonExecutable() {
                this.exec = false;
            }

        }
    }

}
