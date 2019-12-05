package ua.napps.scorekeeper.listeners;


import androidx.recyclerview.widget.RecyclerView;
import ua.napps.scorekeeper.counters.Counter;

public interface DragItemListener {

    void onStartDrag(RecyclerView.ViewHolder viewHolder);

    void afterDrag(Counter counter, int fromIndex, int toIndex);
}
