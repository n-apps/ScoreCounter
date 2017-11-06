package ua.napps.scorekeeper.counters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.flexbox.FlexboxLayoutManager;
import java.util.List;
import java.util.Objects;
import ua.com.napps.scorekeeper.R;
import ua.napps.scorekeeper.counters.CountersAdapter.CountersViewHolder;
import ua.napps.scorekeeper.settings.SettingsUtil;
import ua.napps.scorekeeper.utils.ColorUtil;

public class CountersAdapter extends RecyclerView.Adapter<CountersViewHolder> {

    public static class CountersViewHolder extends RecyclerView.ViewHolder {

        final float SINGLE_TAP_COORD_DELTA = 3.5f;

        Counter counter;

        CounterActionCallback counterActionCallback;

        FrameLayout counterClickableArea;

        TextView counterEdit;

        LinearLayout counterHeader;

        TextView counterName;

        TextView counterValue;

        ImageView decreaseImageView;

        ImageView increaseImageView;

        CountersViewHolder(View v, CounterActionCallback callback) {
            super(v);
            counterActionCallback = callback;
            counterValue = v.findViewById(R.id.tv_counter_value);
            counterName = v.findViewById(R.id.tv_counter_name);
            counterEdit = v.findViewById(R.id.tv_counter_edit);
            counterClickableArea = v.findViewById(R.id.counter_interaction_area);
            counterHeader = v.findViewById(R.id.counter_header);
            increaseImageView = v.findViewById(R.id.iv_increase);
            decreaseImageView = v.findViewById(R.id.iv_decrease);
            counterHeader.setOnClickListener(v1 -> counterActionCallback.onNameClick(counter));
            counterClickableArea.setOnTouchListener(new OnTouchListener() {
                float touchedX, touchedY;

                @Override
                public boolean onTouch(final View v, final MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:

                            touchedX = event.getRawX();
                            touchedY = event.getRawY();
                            return true;

                        case MotionEvent.ACTION_MOVE:
                            return true;

                        case MotionEvent.ACTION_UP:
                            if (isSingleTap(event, touchedX, touchedY)) {
                                updateCounter(event);
                                v.performClick();
                            }
                            return true;
                    }
                    return false;
                }
            });
        }

        private boolean isSingleTap(MotionEvent event, float touchedX, float touchedY) {
            float deltaX = Math.abs(touchedX - event.getRawX());
            float deltaY = Math.abs(touchedY - event.getRawY());
            return deltaX <= SINGLE_TAP_COORD_DELTA && deltaY <= SINGLE_TAP_COORD_DELTA;
        }


        private void updateCounter(final MotionEvent e) {
            if (e.getX() > counterClickableArea.getWidth() / 2) {
                counterActionCallback.onIncreaseClick(counter);
            } else {
                counterActionCallback.onDecreaseClick(counter);
            }
        }
    }

    List<? extends Counter> counters;

    private CounterActionCallback callback;

    private int height;

    private boolean tryToFitAllCounters;

    public CountersAdapter(CounterActionCallback callback) {
        this.callback = callback;
    }

    @Override
    public int getItemCount() {
        return counters == null ? 0 : counters.size();
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

        ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
        if (lp instanceof FlexboxLayoutManager.LayoutParams) {
            FlexboxLayoutManager.LayoutParams flexboxLp = (FlexboxLayoutManager.LayoutParams) lp;
            flexboxLp.setFlexGrow(1.0f);
            final int itemCount = getItemCount();
            if (itemCount <= SettingsUtil.MAX_COUNTERS_TO_FIT_ON_SCREEN) {
                flexboxLp.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
            } else if (!tryToFitAllCounters) {
                if (height == 0) {
                    height = holder.itemView.getMinimumHeight();
                }
                flexboxLp.setHeight(height);
            }
        }
    }

    @Override
    public CountersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_counter, parent, false);
        return new CountersViewHolder(v, callback);
    }

    public void setCountersList(final List<? extends Counter> counters) {
        if (this.counters == null) {
            this.counters = counters;
            notifyItemRangeInserted(0, counters.size());
        } else {
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    Counter newCounter = counters.get(newItemPosition);
                    Counter oldCounter = CountersAdapter.this.counters.get(oldItemPosition);
                    return Objects.equals(newCounter.getId(), oldCounter.getId()) && Objects.equals(
                            newCounter.getColor(), oldCounter.getColor()) && Objects.equals(newCounter.getName(),
                            oldCounter.getName()) && newCounter.getValue() == oldCounter.getValue();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return Objects.equals(CountersAdapter.this.counters.get(oldItemPosition).getId(),
                            counters.get(newItemPosition).getId());
                }

                @Override
                public int getNewListSize() {
                    return counters.size();
                }

                @Override
                public int getOldListSize() {
                    return CountersAdapter.this.counters.size();
                }
            });
            this.counters = counters;
            result.dispatchUpdatesTo(this);
        }
    }
}
