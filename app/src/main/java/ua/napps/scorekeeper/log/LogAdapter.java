package ua.napps.scorekeeper.log;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import ua.com.napps.scorekeeper.R;

public class LogAdapter extends RecyclerView.Adapter<LogAdapter.LogAdapterViewHolder> {
    private ArrayList<LogEntry> logEntries;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class LogAdapterViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView tv_test;

        public LogAdapterViewHolder(View v) {
            super(v);
            tv_test = v.findViewById(R.id.tv_item_log_test);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public LogAdapter(ArrayList<LogEntry> logEntries) {
        this.logEntries = logEntries;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public LogAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_log_entry, parent, false);

        return new LogAdapterViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(LogAdapterViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.tv_test.setText(logEntries.get(position).toString());

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return logEntries.size();
    }
}