package ua.napps.scorekeeper.Adapters;

import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnTouch;
import ua.com.napps.scorekeeper.R;
import ua.napps.scorekeeper.Models.Counter;
import ua.napps.scorekeeper.View.EditCounterFragment;
import ua.napps.scorekeeper.View.MainActivity;

import static android.support.v7.widget.RecyclerView.ViewHolder;
import static ua.napps.scorekeeper.Helpers.Constants.PREV_VALUE_SHOW_DURATION;
import static ua.napps.scorekeeper.Models.CurrentSet.getInstance;

/**
 * Created by novo on 22-Dec-15.
 */
public class CountersAdapter extends RecyclerView.Adapter<CountersAdapter.MyViewHolder> {

    private boolean mIsAllCountersShowing;
    private ArrayList<Counter> mCounters;
    private final MainActivity mContext;
    private final float mDensity;

    public CountersAdapter(MainActivity context) {
        this.mContext = context;
        this.mDensity = mContext.getResources().getDisplayMetrics().density;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(mContext).inflate(R.layout.counter_view, parent, false));
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final Counter counter = mCounters.get(position);
        int tsCaption = (int) (mContext.getResources().getDimension(R.dimen.text_size_caption) / mDensity);
        int tsValue = (int) (mContext.getResources().getDimension(R.dimen.text_size_value) / mDensity);

        holder.mCounterView.setBackgroundColor(counter.getColor());
        holder.mCounterName.setText(counter.getCaption());
        holder.mCounterName.setTextColor(counter.getTextColor());
        holder.mCounterValue.setText(String.valueOf(counter.getValue()));
        holder.mCounterValue.setTextColor(counter.getTextColor());
        holder.mPrevValue.setTextColor(counter.getTextColor());
        holder.mCounterView.setRotation(counter.getRotationValue());
        holder.mMinusSymbol.setTextColor(counter.getTextColor());
        holder.mPlusSymbol.setTextColor(counter.getTextColor());

        if (mIsAllCountersShowing) {
            holder.mCounterName.setTextSize(tsCaption / getItemCount());
            holder.mCounterValue.setTextSize(tsValue / getItemCount());
            holder.mPrevValue.setTextSize(tsCaption / getItemCount());
            holder.mMinusSymbol.setTextSize(tsValue / getItemCount());
            holder.mPlusSymbol.setTextSize(tsValue / getItemCount());
        } else {
            holder.mCounterName.setTextSize(tsCaption);
            holder.mCounterValue.setTextSize(tsValue);
            holder.mPrevValue.setTextSize(tsCaption);
            holder.mMinusSymbol.setTextSize(tsCaption);
            holder.mPlusSymbol.setTextSize(tsCaption);
        }
    }

    @Override
    public int getItemCount() {
        return mCounters.size();
    }

    public void setCounters(ArrayList<Counter> items) {
        mCounters = items;
    }

    public void setCountersVisibility(boolean isShowing) {
        mIsAllCountersShowing = isShowing;
    }

    @SuppressWarnings("unused")
    class MyViewHolder extends ViewHolder {
        @Bind(R.id.counter_name)
        TextView mCounterName;
        @Bind(R.id.value)
        TextView mCounterValue;
        @Bind(R.id.prevValue)
        TextView mPrevValue;
        @Bind(R.id.minus)
        TextView mMinusSymbol;
        @Bind(R.id.plus)
        TextView mPlusSymbol;
        @Bind(R.id.rootCounterView)
        FrameLayout mCounterView;

        private long startShowingPrevValue;

        final GestureDetector gestureDetector = new GestureDetector(mContext, new GestureDetector.SimpleOnGestureListener() {

            public void onLongPress(MotionEvent e) {
                FragmentManager fragmentManager = mContext.getSupportFragmentManager();
                EditCounterFragment editCounterFragment = EditCounterFragment.newInstance(getAdapterPosition());
                editCounterFragment.show(fragmentManager, "edit_counter_dialog");
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                updateCounter(e);
                return super.onSingleTapUp(e);
            }

        });

        @OnTouch(R.id.rootCounterView)
        public boolean onTouchItem(MotionEvent event) {
            return gestureDetector.onTouchEvent(event);
        }

        private void updateCounter(MotionEvent e) {
            int position = getAdapterPosition();
            int viewWidth = mCounterView.getWidth();

            if (mPrevValue.getVisibility() != View.GONE) showPrevValue();

            if (e.getX() > viewWidth / 2) {
                getInstance().getCounter(position).increaseValue();
            } else {
                getInstance().getCounter(position).decreaseValue();
            }
            YoYo.with(Techniques.Landing)
                    .duration(300)
                    .playOn(mCounterView.findViewById(R.id.value));
            notifyItemChanged(position);
        }

        private void showPrevValue() {
            long now = System.currentTimeMillis();

            if (now - PREV_VALUE_SHOW_DURATION > startShowingPrevValue) {
                mPrevValue.setText(mCounterValue.getText());
            }
            startShowingPrevValue = now;
        }

        public MyViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
