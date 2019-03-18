package ua.napps.scorekeeper.counters;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import ua.com.napps.scorekeeper.R;
import ua.napps.scorekeeper.counters.CountersAdapter.CountersViewHolder;
import ua.napps.scorekeeper.listeners.DragItemListener;
import ua.napps.scorekeeper.utils.ColorUtil;

public class CountersAdapter extends RecyclerView.Adapter<CountersViewHolder> implements ItemDragAdapter {

    static final String INCREASE_VALUE_CLICK = "increase_value_click";
    static final String DECREASE_VALUE_CLICK = "decrease_value_click";

    private final CounterActionCallback callback;
    private final DragItemListener dragViewListener;
    private final int maxFitCounters;
    private int containerHeight;
    private List<Counter> counters;

    CountersAdapter(int maxCountersToFit, CounterActionCallback callback, DragItemListener dragViewListener) {
        this.callback = callback;
        this.dragViewListener = dragViewListener;
        this.maxFitCounters = maxCountersToFit;
    }

    int getMaxFitCounters() {
        return maxFitCounters;
    }

    void setContainerHeight(int height) {
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
        int textColor = darkBackground ? Color.WHITE : 0xDE000000;
        holder.counterName.setTextColor(textColor);
        holder.counterValue.setTextColor(textColor);

        Drawable wrapDrawable1 = DrawableCompat.wrap(holder.increaseImageView.getDrawable().mutate());
        Drawable wrapDrawable2 = DrawableCompat.wrap(holder.decreaseImageView.getDrawable().mutate());
        Drawable wrapDrawable3 = DrawableCompat.wrap(holder.counterOptionsImageView.getDrawable().mutate());
        DrawableCompat.setTint(wrapDrawable1, textColor);
        DrawableCompat.setTint(wrapDrawable2, textColor);
        DrawableCompat.setTint(wrapDrawable3, textColor);

        int itemCount = getItemCount();
        if (itemCount <= maxFitCounters) { // fit available space
            StaggeredGridLayoutManager.LayoutParams lp = new StaggeredGridLayoutManager.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, containerHeight / itemCount);
            holder.itemView.setLayoutParams(lp);
            lp.setFullSpan(false);
        } else {// set height as recyclerview height / maxFitCounters
            StaggeredGridLayoutManager.LayoutParams lp = new StaggeredGridLayoutManager.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, containerHeight / (maxFitCounters > 1 ? itemCount / 2 : 1));
            lp.setFullSpan(position == itemCount - 1 && itemCount % 2 == 1);
            holder.itemView.setLayoutParams(lp);
        }
    }

    @NonNull
    @Override
    public CountersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_counter, parent, false);
        return new CountersViewHolder(v, callback, dragViewListener);
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
                    // We don`t want to check position, because during dragging items were moved only within adapter.
                    // Position in database was changed only after the end of dragging event.
                    // Checking for position change in this method, would result into recycler view redundant refresh.
                }
            });
            counters = update;
            result.dispatchUpdatesTo(this);
        }
    }

    private Counter lastMovedCounter = null;
    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        Counter counter = counters.remove(fromPosition);
        counters.add(toPosition > fromPosition ? toPosition - 0 : toPosition, counter);
        notifyItemMoved(fromPosition, toPosition);

        if (lastMovedCounter == null) {
            lastMovedCounter = counter;
        }
    }

    @Override
    public void onItemClear(int fromPosition, int toPosition) {
        if (lastMovedCounter != null) {
            dragViewListener.afterDrag(lastMovedCounter, fromPosition, toPosition);
        }
        lastMovedCounter = null;
    }

    public class CountersViewHolder extends RecyclerView.ViewHolder implements Callback {

        private static final int MSG_PERFORM_LONGCLICK = 1;

        final ImageView decreaseImageView;
        final ImageView increaseImageView;
        final TextView counterValue;

        private final int TIME_LONG_CLICK = 300;
        private final CounterActionCallback counterActionCallback;
        private final FrameLayout counterClickableArea;
        private final ImageView counterOptionsImageView;
        private final TextView counterName;
        private final Handler handler;
        public Counter counter;
        private float lastX;
        private LongClickTimerTask timerTask;

        @SuppressLint("ClickableViewAccessibility")
        CountersViewHolder(View v, CounterActionCallback callback, DragItemListener dragItemListener) {
            super(v);
            counterActionCallback = callback;
            handler = new Handler(this);
            counterValue = v.findViewById(R.id.tv_counter_value);
            counterName = v.findViewById(R.id.tv_counter_name);
            counterOptionsImageView = v.findViewById(R.id.iv_counter_edit);
            counterClickableArea = v.findViewById(R.id.counter_interaction_area);
            increaseImageView = v.findViewById(R.id.iv_increase);
            decreaseImageView = v.findViewById(R.id.iv_decrease);
            counterName.setOnClickListener(v1 -> counterActionCallback.onNameClick(counter));
            counterOptionsImageView.setOnClickListener(v2 -> counterActionCallback.onEditClick(v, counter));

            final CountersViewHolder holder = this;
            counterName.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    dragViewListener.onStartDrag(holder);
                    return false;
                }
            });

            counterOptionsImageView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    dragViewListener.onStartDrag(holder);
                    return false;
                }
            });

            counterClickableArea.setOnTouchListener((v12, e) -> {
                switch (e.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        lastX = e.getX();
                        if (timerTask != null) {
                            timerTask.cancel();
                        }
                        // start counting long click time
                        timerTask = new LongClickTimerTask();
                        Timer timer = new Timer();
                        timer.schedule(timerTask, TIME_LONG_CLICK);
                        break;

                    case MotionEvent.ACTION_UP:
                        long time = e.getEventTime() - e.getDownTime();
                        if (time < TIME_LONG_CLICK) {
                            v12.performClick();
                            updateCounter(e.getX());
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
                if (lastX != -1) {
                    final boolean isIncrease = lastX > counterClickableArea.getWidth() / 2;
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
            lastX = -1;
        }

        private void updateCounter(float x) {
            if (x > counterClickableArea.getWidth() / 2) {
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
