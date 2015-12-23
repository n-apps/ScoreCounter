package ua.napps.scorekeeper.Adapters;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;
import ua.com.napps.scorekeeper.R;
import ua.napps.scorekeeper.Events.CounterCaptionClick;
import ua.napps.scorekeeper.Interactors.CurrentSet;
import ua.napps.scorekeeper.Models.Counter;
import ua.napps.scorekeeper.View.MainActivity;

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
        holder.text.setText(mCounters.get(position).getCaption());
        holder.value.setText(String.valueOf(mCounters.get(position).getValue()));
    }

    @Override
    public int getItemCount() {
        return mCounters.size();
    }

    public void addCounter(Counter item) {
        mCounters.add(item);
        notifyItemInserted(getItemCount());
    }

    public void setCounters(ArrayList<Counter> items) {
        mCounters = items;
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView text;
        TextView value;

        public MyViewHolder(View itemView) {
            super(itemView);
            text = (TextView) itemView.findViewById(R.id.caption);
            value = (TextView) itemView.findViewById(R.id.value);
            text.setOnClickListener(this);
            value.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Log.i("***", "onClick");
            int position = getAdapterPosition();

            int id = v.getId();

            switch (id) {
                case R.id.caption:
                    EventBus.getDefault().post(new CounterCaptionClick(mCounters.get(position)));
                    break;
                case R.id.value:
                    Toast.makeText(v.getContext(), "position " + position, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
}
