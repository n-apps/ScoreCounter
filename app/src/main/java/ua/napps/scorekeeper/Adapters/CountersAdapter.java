package ua.napps.scorekeeper.Adapters;

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
import butterknife.OnClick;
import butterknife.OnTouch;
import ua.com.napps.scorekeeper.R;
import ua.napps.scorekeeper.Models.Counter;
import ua.napps.scorekeeper.View.DialogEditCounter;
import ua.napps.scorekeeper.View.MainActivity;

import static android.support.v7.widget.RecyclerView.ViewHolder;
import static ua.napps.scorekeeper.Interactors.CurrentSet.getCurrentSet;

/**
 * Created by novo on 22-Dec-15.
 */
public class CountersAdapter extends RecyclerView.Adapter<CountersAdapter.MyViewHolder> {

    private ArrayList<Counter> mCounters;
    private final MainActivity mContext;


    public CountersAdapter(MainActivity context) {
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

    class MyViewHolder extends ViewHolder {
        @Bind(R.id.caption)
        TextView mCaption;
        @Bind(R.id.value)
        TextView mValue;
        @Bind(R.id.rootCounterView)
        LinearLayout mCounterView;

        @OnTouch(R.id.rootCounterView)
        public boolean onTouchItem(View v, MotionEvent event) {
            int position = getAdapterPosition();
            switch (event.getAction()) {
                case MotionEvent.ACTION_UP:

                    if (event.getX() > v.getWidth() / 2) {
                        getCurrentSet().getCounter(position).increaseValue();
                    } else {
                        getCurrentSet().getCounter(position).decreaseValue();
                    }
                    notifyDataSetChanged();
                    YoYo.with(Techniques.ZoomIn)
                            .duration(200)
                            .playOn(v.findViewById(R.id.value));
                    break;
                case MotionEvent.ACTION_CANCEL:
                    break;
            }
            return true;
        }

        @OnClick(R.id.caption)
        public void onCaptionClick() {
            new DialogEditCounter(mContext, getAdapterPosition());
        }

        public MyViewHolder(final View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }
}
