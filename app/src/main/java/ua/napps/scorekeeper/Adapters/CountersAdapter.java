package ua.napps.scorekeeper.Adapters;

import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnLongClick;
import butterknife.OnTouch;
import ua.com.napps.scorekeeper.R;
import ua.napps.scorekeeper.Models.Counter;
import ua.napps.scorekeeper.View.EditCounterFragment;
import ua.napps.scorekeeper.View.MainActivity;

import static android.support.v7.widget.RecyclerView.ViewHolder;
import static ua.napps.scorekeeper.Helpers.Constants.CAPTION_TEXT_SIZE;
import static ua.napps.scorekeeper.Helpers.Constants.CAPTION_TEXT_SIZE_SINGLE_COUNTER;
import static ua.napps.scorekeeper.Helpers.Constants.COUNTER_VALUE_TEXT_SIZE;
import static ua.napps.scorekeeper.Helpers.Constants.COUNTER_VALUE_TEXT_SIZE_SINGLE_COUNTER;
import static ua.napps.scorekeeper.Helpers.Constants.PREV_VALUE_SHOW_DURATION;
import static ua.napps.scorekeeper.Helpers.Constants.PREV_VALUE_TEXT_SIZE;
import static ua.napps.scorekeeper.Helpers.Constants.PREV_VALUE_TEXT_SIZE_SINGLE_COUNTER;
import static ua.napps.scorekeeper.Interactors.CurrentSet.getCurrentSet;

/**
 * Created by novo on 22-Dec-15.
 */
public class CountersAdapter extends RecyclerView.Adapter<CountersAdapter.MyViewHolder> {


    private boolean mIsAllCountersShowing;
    private ArrayList<Counter> mCounters;
    private final MainActivity mContext;


    public CountersAdapter(MainActivity context) {
        this.mContext = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MyViewHolder holder = new MyViewHolder(LayoutInflater.from(mContext).inflate(R.layout.counter_view, parent, false));
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.mCounterView.setBackgroundColor(mCounters.get(position).getColor());
        holder.mCaption.setText(mCounters.get(position).getCaption());
        holder.mCaption.setTextColor(mCounters.get(position).getTextColor());
        holder.mValue.setText(String.valueOf(mCounters.get(position).getValue()));
        holder.mValue.setTextColor(mCounters.get(position).getTextColor());
        holder.mPrevValue.setTextColor(mCounters.get(position).getTextColor());

        if (mIsAllCountersShowing) {
            holder.mCaption.setTextSize(CAPTION_TEXT_SIZE / getItemCount());
            holder.mValue.setTextSize(COUNTER_VALUE_TEXT_SIZE / getItemCount());
            holder.mPrevValue.setTextSize(PREV_VALUE_TEXT_SIZE / getItemCount());
        } else {
            holder.mCaption.setTextSize(CAPTION_TEXT_SIZE_SINGLE_COUNTER);
            holder.mValue.setTextSize(COUNTER_VALUE_TEXT_SIZE_SINGLE_COUNTER);
            holder.mPrevValue.setTextSize(PREV_VALUE_TEXT_SIZE_SINGLE_COUNTER);
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

    class MyViewHolder extends ViewHolder {
        @Bind(R.id.caption)
        TextView mCaption;
        @Bind(R.id.value)
        TextView mValue;
        @Bind(R.id.prevValue)
        TextView mPrevValue;
        @Bind(R.id.rootCounterView)
        LinearLayout mCounterView;

        private long startShowingPrevValue;

        @OnLongClick(R.id.rootCounterView)
        public boolean onLongClick(View v) {
            FragmentManager fragmentManager = mContext.getSupportFragmentManager();
            EditCounterFragment editCounterFragment = EditCounterFragment.newInstance(getAdapterPosition());
            editCounterFragment.show(fragmentManager, "edit_counter_dialog");
            return true;
        }

        @OnTouch(R.id.rootCounterView)
        public boolean onTouchItem(View v, MotionEvent event) {
            int position = getAdapterPosition();

            switch (event.getAction()) {
                case MotionEvent.ACTION_UP:

                    if (mPrevValue.getVisibility() != View.GONE) showPrevValue();

                    if (event.getX() > v.getWidth() / 2)
                        getCurrentSet().getCounter(position).increaseValue();
                    else getCurrentSet().getCounter(position).decreaseValue();
                    notifyItemChanged(position);
                    YoYo.with(Techniques.ZoomIn)
                            .duration(200)
                            .playOn(v.findViewById(R.id.value));
                    break;
                case MotionEvent.ACTION_CANCEL:
                    break;
            }
            return false;
        }

        private void showPrevValue() {
            long now = System.currentTimeMillis();

            if (now - PREV_VALUE_SHOW_DURATION > startShowingPrevValue) {
                mPrevValue.setText(mValue.getText());
            }
            startShowingPrevValue = now;
        }

        public MyViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
