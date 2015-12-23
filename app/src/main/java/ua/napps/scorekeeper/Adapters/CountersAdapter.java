package ua.napps.scorekeeper.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import ua.com.napps.scorekeeper.R;
import ua.napps.scorekeeper.CounterView;
import ua.napps.scorekeeper.Interactors.CurrentSet;
import ua.napps.scorekeeper.Models.Counter;
import ua.napps.scorekeeper.View.MainActivity;

import static ua.napps.scorekeeper.Helpers.Constants.LONG_PRESS_TIMEOUT;

/**
 * Created by novo on 22-Dec-15.
 */
public class CountersAdapter extends RecyclerView.Adapter<CountersAdapter.MyViewHolder> {

    private ArrayList<Counter> mCounters;
    private final MainActivity mContext;


    public CountersAdapter(MainActivity context) {
        this.mCounters = CurrentSet.getCurrentSet().getCounters();
        this.mContext = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MyViewHolder holder = new MyViewHolder(LayoutInflater.from(mContext).inflate(R.layout.view_counter, parent, false));
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.mCaption.setText(mCounters.get(position).getCaption());
        holder.mValue.setText(String.valueOf(mCounters.get(position).getValue()));
        holder.mCounterView.setBackgroundColor(mCounters.get(position).getColor());
    }

    @Override
    public int getItemCount() {
        return mCounters.size();
    }

    public void setCounters(ArrayList<Counter> items) {
        mCounters = items;
    }

    public Counter getCounter(int position) {
        return mCounters.get(position);
    }

    private static OnItemClickListener sItemClickListener;

    public interface OnItemClickListener {
        void onCaptionClick(int position);

        void onValueClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        sItemClickListener = listener;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.caption)
        TextView mCaption;
        @Bind(R.id.value)
        TextView mValue;
        @Bind(R.id.rootCounterView)
        CounterView mCounterView;

        public MyViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            mCounterView.setOnTouchListener(new OnTouchListener() {
                                                @Override
                                                public boolean onTouch(View v, MotionEvent event) {
                                                    switch (event.getAction()) {
                                                        case MotionEvent.ACTION_UP:
                                                            boolean longClick = event.getEventTime() - event.getDownTime() > LONG_PRESS_TIMEOUT;
                                                            if (longClick) return true;
                                                            int newValue = getCounter(getAdapterPosition()).getValue();

                                                            if (event.getX() > v.getWidth() / 2) {
                                                                getCounter(getAdapterPosition()).setValue(newValue + 1);
                                                            } else {
                                                                getCounter(getAdapterPosition()).setValue(newValue - 1);
                                                            }
                                                            notifyItemChanged(getAdapterPosition());
                                                            YoYo.with(Techniques.ZoomIn)
                                                                    .duration(200)
                                                                    .playOn(mValue);

                                                            break;
                                                        case MotionEvent.ACTION_CANCEL:
                                                            break;
                                                    }
                                                    return true;
                                                }
                                            }

            );

            mCaption.setOnClickListener(new OnClickListener()

                                        {
                                            @Override
                                            public void onClick(View v) {
                                                // Triggers click upwards to the adapter on click
                                                if (sItemClickListener != null)
                                                    sItemClickListener.onCaptionClick(getLayoutPosition());
                                            }
                                        }

            );


        }

    }
}
