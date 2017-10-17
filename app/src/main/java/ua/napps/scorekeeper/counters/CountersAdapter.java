package ua.napps.scorekeeper.counters;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.flexbox.FlexboxLayoutManager;
import java.util.List;
import java.util.Objects;
import timber.log.Timber;
import ua.com.napps.scorekeeper.R;
import ua.napps.scorekeeper.counters.CountersAdapter.CountersViewHolder;

public class CountersAdapter extends RecyclerView.Adapter<CountersViewHolder> {

    public static class CountersViewHolder extends RecyclerView.ViewHolder
            implements GestureDetector.OnGestureListener {

        Counter counter;

        CounterActionCallback counterActionCallback;

        FrameLayout counterClickableArea;

        LinearLayout counterHeader;

        TextView counterName;

        TextView counterValue;

        Context context;

        private final GestureDetector gestureDetector;

        CountersViewHolder(View v, CounterActionCallback callback) {
            super(v);
            context = v.getContext();
            counterActionCallback = callback;
            gestureDetector = new GestureDetector(context, this);
            counterValue = v.findViewById(R.id.tv_counter_value);
            counterName = v.findViewById(R.id.tv_counter_name);
            counterClickableArea = v.findViewById(R.id.counter_interaction_area);
            counterHeader = v.findViewById(R.id.counter_header);
            counterHeader.setOnClickListener(v1 -> counterActionCallback.onNameClick(counter));
            counterClickableArea.setOnTouchListener((v1, event) -> gestureDetector.onTouchEvent(event));
        }

        @Override
        public boolean onDown(final MotionEvent e) {

            return true;
        }

        @Override
        public void onShowPress(final MotionEvent e) {

        }

        @Override
        public boolean onSingleTapUp(final MotionEvent e) {
            updateCounter(e);
            return false;
        }

        @Override
        public boolean onScroll(final MotionEvent e1, final MotionEvent e2, final float distanceX,
                final float distanceY) {
            return false;
        }

        @Override
        public void onLongPress(final MotionEvent e) {

        }

        @Override
        public boolean onFling(final MotionEvent e1, final MotionEvent e2, final float velocityX,
                final float velocityY) {
            return false;
        }

        private void updateCounter(final MotionEvent e) {
            Timber.d("updateCounter");
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

    public CountersAdapter(CounterActionCallback callback) {
        this.callback = callback;
    }

    @Override
    public int getItemCount() {
        return counters == null ? 0 : counters.size();
    }

    @Override
    public void onBindViewHolder(CountersViewHolder holder, int position) {
        final Counter counter = counters.get(position);
        holder.counter = counter;
        holder.counterName.setText(counter.getName());
        holder.counterValue.setText(String.valueOf(counter.getValue()));
        holder.itemView.setBackgroundColor(Color.parseColor(counter.getColor()));

        ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
        if (lp instanceof FlexboxLayoutManager.LayoutParams) {
            FlexboxLayoutManager.LayoutParams flexboxLp = (FlexboxLayoutManager.LayoutParams) lp;
            flexboxLp.setFlexGrow(1.0f);
            final int itemCount = getItemCount();

            if (itemCount > 4) {
                flexboxLp.setHeight(height == 0 ? 384 : height);
            } else {
                if (itemCount == 4) {
                    height = holder.itemView.getHeight();
                }
                flexboxLp.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
            }
        }
    }

    @Override
    public CountersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_counter, parent, false);
        return new CountersViewHolder(v, callback);
    }

    public void setProductList(final List<? extends Counter> productList) {
        if (counters == null) {
            counters = productList;
            notifyItemRangeInserted(0, productList.size());
        } else {
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    Counter newProduct = productList.get(newItemPosition);
                    Counter oldProduct = counters.get(oldItemPosition);
                    return Objects.equals(newProduct.getId(), oldProduct.getId()) && Objects.equals(
                            newProduct.getColor(), oldProduct.getColor()) && Objects.equals(newProduct.getName(),
                            oldProduct.getName()) && newProduct.getValue() == oldProduct.getValue();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return Objects.equals(counters.get(oldItemPosition).getId(),
                            productList.get(newItemPosition).getId());
                }

                @Override
                public int getNewListSize() {
                    return productList.size();
                }

                @Override
                public int getOldListSize() {
                    return counters.size();
                }
            });
            counters = productList;
            result.dispatchUpdatesTo(this);
        }
    }
}
