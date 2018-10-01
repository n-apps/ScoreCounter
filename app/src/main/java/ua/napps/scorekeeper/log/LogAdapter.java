package ua.napps.scorekeeper.log;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;

import ua.com.napps.scorekeeper.R;
import ua.napps.scorekeeper.utils.RoundedColorView;

public class LogAdapter extends RecyclerView.Adapter<LogAdapter.LogAdapterViewHolder> {
    private ArrayList<LogEntry> logEntries;

    static class LogAdapterViewHolder extends RecyclerView.ViewHolder {
        TextView tv_main,tv_time;
        RoundedColorView rcv_color;

        LogAdapterViewHolder(View v) {
            super(v);
            tv_main = v.findViewById(R.id.tv_item_log_main);
            tv_time = v.findViewById(R.id.tv_item_log_time);
            rcv_color = v.findViewById(R.id.rcv_item_log_color);
        }
    }

    LogAdapter(ArrayList<LogEntry> logEntries) {
        this.logEntries = logEntries;
    }

    @NonNull
    @Override
    public LogAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_log_entry, parent, false);

        return new LogAdapterViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull LogAdapterViewHolder holder, int position) {
        LogEntry logEntry = logEntries.get(position);

        holder.tv_main.setText(logEntry.toString());


        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        String formatTime = format.format(logEntry.timestamp);

        holder.tv_time.setText(formatTime);
        holder.rcv_color.setBackgroundColor(Color.parseColor(logEntry.counter.getColor()));
    }

    @Override
    public int getItemCount() {
        return logEntries.size();
    }
}