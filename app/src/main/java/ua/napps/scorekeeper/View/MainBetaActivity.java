package ua.napps.scorekeeper.View;

import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import io.github.luckyandyzhang.cleverrecyclerview.CleverRecyclerView;
import ua.com.napps.scorekeeper.R;
import ua.napps.scorekeeper.Models.Counter;

import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;

public class MainBetaActivity extends AppCompatActivity {

    private CleverRecyclerView mCleverRecyclerView;
    private MyAdapter mAdapter;
    private int mVisibleCounters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_beta);
        if (Build.VERSION.SDK_INT > 18) getWindow().addFlags(FLAG_FULLSCREEN);

        mCleverRecyclerView = (CleverRecyclerView) findViewById(R.id.recyclerView);
        initData();
        if (mAdapter == null) {
            mAdapter = new MyAdapter();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        mCleverRecyclerView.setAdapter(mAdapter);
        mCleverRecyclerView.setVisibleChildCount(mAdapter.getItemCount());
        mCleverRecyclerView.setScrollAnimationDuration(300);
        mCleverRecyclerView.setFlingFriction(0.8f);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mCleverRecyclerView.setOrientation(RecyclerView.VERTICAL);
        } else {
            mCleverRecyclerView.setOrientation(RecyclerView.HORIZONTAL);
        }
    }

    private List<String> mDatas;
    private ArrayList<Counter> mCounters;

    protected void initData() {
        mCounters = new ArrayList<>();
        for (int i = 1; i < 5; i++) {
            mCounters.add(new Counter(String.format("% d", i)));
        }

        mDatas = new ArrayList<>();
        for (int i = 'A'; i < 'z'; i++) {
            mDatas.add("" + (char) i);
        }
    }


    class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            MyViewHolder holder = new MyViewHolder(LayoutInflater.from(MainBetaActivity.this).inflate(R.layout.view_counter, parent, false));
            return holder;
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            holder.text.setText(String.format("Counter%s", mCounters.get(position).getCaption()));
            holder.value.setText(String.valueOf(mCounters.get(position).getValue()));
        }

        @Override
        public int getItemCount() {
            return mCounters.size();
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
                mCounters.add(new Counter(String.format("% d", getItemCount())));
                notifyItemInserted(getItemCount());
                Toast.makeText(v.getContext(), "position " + position, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
