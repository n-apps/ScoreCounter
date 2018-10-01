package ua.napps.scorekeeper.log;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import ua.com.napps.scorekeeper.R;
import ua.napps.scorekeeper.utils.RoundedColorView;

public class LogAdapter extends RecyclerView.Adapter<LogAdapter.LogAdapterViewHolder> {
    private ArrayList<LogEntry> logEntries;

    static class LogAdapterViewHolder extends RecyclerView.ViewHolder {
        TextView tv_counter,tv_info,tv_time;
        RoundedColorView rcv_color;

        LogAdapterViewHolder(View v) {
            super(v);
            tv_counter = v.findViewById(R.id.tv_item_log_counter);
            tv_info = v.findViewById(R.id.tv_item_log_info);
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

        holder.tv_counter.setText(logEntry.counter.getName());

        String info = "";
        switch (logEntry.type){
            case INC:
                info = "\u2795 ";
                break;
            case INC_C:
                info = "\u2795 ";
                break;
            case DEC:
                info = "\u2796 ";
                break;
            case DEC_C:
                info = "\u2796 ";
                break;
            case SET:
                info = "\uD83D\uDD8A ";
                break;
            case RMV:
                info = "\uD83D\uDDD1 ";
                break;
            case RST:
                info = "\uD83D\uDD04 ";
                break;
        }

        DecimalFormat decimalFormat = (DecimalFormat)DecimalFormat.getNumberInstance(Locale.ENGLISH); // TODO use user locale

        info = info + decimalFormat.format(logEntry.value) + " [" + decimalFormat.format(logEntry.prevValue) + " \u27A1 " + decimalFormat.format(logEntry.prevValue + logEntry.value) + "]";

        holder.tv_info.setText(info);

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