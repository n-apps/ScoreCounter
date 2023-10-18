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
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import ua.napps.scorekeeper.R;
import ua.napps.scorekeeper.listeners.DragItemListener;
import ua.napps.scorekeeper.utils.ColorUtil;

public class CountersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ItemDragAdapter {

    public static final int MODE_INCREASE_VALUE = 1;
    public static final int MODE_DECREASE_VALUE = 2;
    public static final int MODE_SET_VALUE = 3;
    public static final int TYPE_FULL = 1;
    public static final int TYPE_COMPACT = 2;

    static final String INCREASE_VALUE_CLICK = "increase_value_click";
    static final String DECREASE_VALUE_CLICK = "decrease_value_click";
    private final CounterActionCallback callback;
    private final DragItemListener dragViewListener;
    private List<Counter> counters;
    private Counter lastMovedCounter = null;

    CountersAdapter(CounterActionCallback callback, DragItemListener dragViewListener) {
        this.callback = callback;
        this.dragViewListener = dragViewListener;
    }

    @Override
    public int getItemCount() {
        return counters == null ? 0 : counters.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_FULL) {
            final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_counter_full, parent, false);
            return new CounterFullViewHolder(v, callback);
        } else {
            final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_counter_compact, parent, false);
            return new CounterCompactViewHolder(v, callback);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        final Counter counter = counters.get(position);
        int counterColor = Color.parseColor(counter.getColor());
        if (getItemViewType(position) == TYPE_FULL) {
            CounterFullViewHolder holder = (CounterFullViewHolder) viewHolder;
            holder.counter = counter;
            holder.counterName.setText(counter.getName());
            holder.counterValue.setText(String.format(Locale.FRANCE, "%,d", counter.getValue()));
            holder.container.setCardBackgroundColor(counterColor);
            final boolean darkBackground = ColorUtil.isDarkBackground(counterColor);
            int tintColor = darkBackground ? Color.WHITE : 0xDE000000;
            holder.counterName.setTextColor(tintColor);
            holder.counterValue.setTextColor(tintColor);

            Drawable wrapDrawable1 = DrawableCompat.wrap(holder.increaseImageView.getDrawable().mutate());
            Drawable wrapDrawable2 = DrawableCompat.wrap(holder.decreaseImageView.getDrawable().mutate());
            Drawable wrapDrawable3 = DrawableCompat.wrap(holder.counterOptionsImageView.getDrawable().mutate());
            DrawableCompat.setTint(wrapDrawable1, tintColor);
            DrawableCompat.setTint(wrapDrawable2, tintColor);
            DrawableCompat.setTint(wrapDrawable3, tintColor);
        } else {
            CounterCompactViewHolder holder = (CounterCompactViewHolder) viewHolder;
            holder.counter = counter;
            holder.counterName.setText(counter.getName());
            holder.counterValue.setText(String.format(Locale.FRANCE, "%,d", counter.getValue()));
            holder.container.setCardBackgroundColor(counterColor);
            final boolean darkBackground = ColorUtil.isDarkBackground(counterColor);
            int tintColor = darkBackground ? Color.WHITE : 0xDE000000;
            holder.counterName.setTextColor(tintColor);
            holder.counterValue.setTextColor(tintColor);

            Drawable wrapDrawable1 = DrawableCompat.wrap(holder.increaseImageView.getDrawable().mutate());
            Drawable wrapDrawable2 = DrawableCompat.wrap(holder.decreaseImageView.getDrawable().mutate());
            DrawableCompat.setTint(wrapDrawable1, tintColor);
            DrawableCompat.setTint(wrapDrawable2, tintColor);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (getItemCount() < 6) {
            return TYPE_FULL;
        } else {
            return TYPE_COMPACT;
        }
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
                            && Objects.equals(newCounter.getStep(), oldCounter.getStep())
                            && Objects.equals(newCounter.getDefaultValue(), oldCounter.getDefaultValue())
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

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        Counter counter = counters.remove(fromPosition);
        counters.add(toPosition > fromPosition ? toPosition : toPosition, counter);
        notifyItemMoved(fromPosition, toPosition);

        if (lastMovedCounter == null) {
            lastMovedCounter = counter;
        }
    }

    @Override
    public void onItemClear(int fromIndex, int toIndex) {
        if (lastMovedCounter != null) {
            dragViewListener.afterDrag(lastMovedCounter, fromIndex, toIndex);
        }
        lastMovedCounter = null;
    }

    public class CounterCompactViewHolder extends RecyclerView.ViewHolder {

        final ImageView decreaseImageView;
        final ImageView increaseImageView;
        final TextView counterValue;
        final MaterialCardView container;

        private final CounterActionCallback counterActionCallback;
        private final TextView counterName;
        public Counter counter;

        @SuppressLint("ClickableViewAccessibility")
        CounterCompactViewHolder(View v, CounterActionCallback callback) {
            super(v);
            counterActionCallback = callback;
            counterValue = v.findViewById(R.id.tv_counter_value);
            counterName = v.findViewById(R.id.tv_counter_name);
            increaseImageView = v.findViewById(R.id.iv_increase);
            decreaseImageView = v.findViewById(R.id.iv_decrease);
            container = v.findViewById(R.id.card);
            counterName.setOnClickListener(v1 -> counterActionCallback.onEditClick(counter));

            final CounterCompactViewHolder holder = this;
            counterName.setOnLongClickListener(view -> {
                dragViewListener.onStartDrag(holder);
                return false;
            });

            increaseImageView.setOnClickListener(v1 -> {
                notifyItemChanged(getAdapterPosition(), INCREASE_VALUE_CLICK);
                counterActionCallback.onSingleClick(counter, getAdapterPosition(), MODE_INCREASE_VALUE);
            });

            decreaseImageView.setOnClickListener(v2 -> {
                notifyItemChanged(getAdapterPosition(), DECREASE_VALUE_CLICK);
                counterActionCallback.onSingleClick(counter, getAdapterPosition(), MODE_DECREASE_VALUE);
            });
            counterValue.setOnClickListener(v3 -> counterActionCallback.onSingleClick(counter, getAdapterPosition(), MODE_SET_VALUE));

            increaseImageView.setOnLongClickListener(v1 -> {
                counterActionCallback.onLongClick(counter, getAdapterPosition(), MODE_INCREASE_VALUE);
                return true;
            });

            decreaseImageView.setOnLongClickListener(v2 -> {
                counterActionCallback.onLongClick(counter, getAdapterPosition(), MODE_DECREASE_VALUE);
                return true;
            });

            counterValue.setOnLongClickListener(v13 -> {
                counterActionCallback.onLongClick(counter, getAdapterPosition(), MODE_SET_VALUE);
                return true;
            });
        }

    }

    public class CounterFullViewHolder extends RecyclerView.ViewHolder implements Callback {

        private static final int MSG_PERFORM_LONGCLICK = 1;

        final ImageView decreaseImageView;
        final ImageView increaseImageView;
        final TextView counterValue;
        final MaterialCardView container;

        private final int TIME_LONG_CLICK = 300;
        private final CounterActionCallback counterActionCallback;
        private final ViewGroup counterClickableArea;
        private final ImageView counterOptionsImageView;
        private final TextView counterName;
        private final Handler handler;
        public Counter counter;
        private float lastX;
        private LongClickTimerTask timerTask;

        @SuppressLint("ClickableViewAccessibility")
        CounterFullViewHolder(View v, CounterActionCallback callback) {
            super(v);
            counterActionCallback = callback;
            handler = new Handler(this);
            counterValue = v.findViewById(R.id.tv_counter_value);
            counterName = v.findViewById(R.id.tv_counter_name);
            counterOptionsImageView = v.findViewById(R.id.iv_counter_edit);
            counterClickableArea = v.findViewById(R.id.counter_interaction_area);
            increaseImageView = v.findViewById(R.id.iv_increase);
            decreaseImageView = v.findViewById(R.id.iv_decrease);
            container = v.findViewById(R.id.card);
            counterName.setOnClickListener(v1 -> counterActionCallback.onNameClick(counter));
            counterOptionsImageView.setOnClickListener(v2 -> counterActionCallback.onEditClick(counter));

            final CounterFullViewHolder holder = this;
            counterName.setOnLongClickListener(view -> {
                dragViewListener.onStartDrag(holder);
                return false;
            });

            counterOptionsImageView.setOnLongClickListener(view -> {
                dragViewListener.onStartDrag(holder);
                return false;
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
                    int interactionAreaWidth = counterClickableArea.getWidth() / 3;
                    if (lastX >= interactionAreaWidth * 2) {
                        counterActionCallback.onLongClick(counter, getAdapterPosition(), MODE_INCREASE_VALUE);
                    } else if (lastX <= interactionAreaWidth) {
                        counterActionCallback.onLongClick(counter, getAdapterPosition(), MODE_DECREASE_VALUE);
                    } else {
                        counterActionCallback.onLongClick(counter, getAdapterPosition(), MODE_SET_VALUE);
                    }
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
            if (counter.getStep() == 0) return;

            float width = counterClickableArea.getWidth();
            if (x >= width - width / 3) {
                notifyItemChanged(getAdapterPosition(), INCREASE_VALUE_CLICK);
                counterActionCallback.onSingleClick(counter, getAdapterPosition(), MODE_INCREASE_VALUE);
            } else if (x <= width / 3) {
                notifyItemChanged(getAdapterPosition(), DECREASE_VALUE_CLICK);
                counterActionCallback.onSingleClick(counter, getAdapterPosition(), MODE_DECREASE_VALUE);
            } else {
                counterActionCallback.onSingleClick(counter, getAdapterPosition(), MODE_SET_VALUE);
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
