package ua.napps.scorekeeper.log;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import ua.napps.scorekeeper.R;
import ua.napps.scorekeeper.utils.Singleton;

public class LogAdapter extends RecyclerView.Adapter<LogAdapter.LogAdapterViewHolder> {
    private final ArrayList<LogEntry> logEntries;

    static class LogAdapterViewHolder extends RecyclerView.ViewHolder {
        final TextView tv_counter;
        final TextView tv_info;
        final TextView tv_time;
        final FrameLayout rcv_color;

        LogAdapterViewHolder(View v) {
            super(v);
            tv_counter = v.findViewById(R.id.tv_item_log_counter);
            tv_info = v.findViewById(R.id.tv_item_log_info);
            tv_time = v.findViewById(R.id.tv_item_log_time);
            rcv_color = v.findViewById(R.id.fl_item_log_color);
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

        DecimalFormat decimalFormat = (DecimalFormat)DecimalFormat.getNumberInstance(Singleton.getInstance().getMainContext().getResources().getConfiguration().locale);

        String info = "";
        switch (logEntry.type){
            case INC:
            case INC_C:
                info = "\u2795  ";
                break;
            case DEC:
            case DEC_C:
                info = "\u2796  ";
                break;
            case SET:
                info = "\uD83D\uDD8A  " + decimalFormat.format(logEntry.value) + " [" + decimalFormat.format(logEntry.prevValue) + " \u279d " + decimalFormat.format( logEntry.value) + "]";
                break;
            case RMV:
                info = "\uD83D\uDDD1  [" + decimalFormat.format(logEntry.prevValue) + " \u279d \u2620️ ]";
                break;
            case RST:
                info = "\uD83D\uDD04  [" + decimalFormat.format(logEntry.prevValue) + " \u279d 0]";
                break;
        }

        if(logEntry.type == LogType.INC ||  logEntry.type == LogType.INC_C ){
            info = info + decimalFormat.format(logEntry.value) + " [" + decimalFormat.format(logEntry.prevValue) + " \u279d " + decimalFormat.format(logEntry.prevValue + logEntry.value) + "]";
        }
        if(logEntry.type == LogType.DEC_C || logEntry.type == LogType.DEC){
            info = info + decimalFormat.format(logEntry.value) + " [" + decimalFormat.format(logEntry.prevValue) + " \u279d " + decimalFormat.format(logEntry.prevValue - logEntry.value) + "]";
        }

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
