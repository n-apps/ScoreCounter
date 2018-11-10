package ua.napps.scorekeeper.counters;

public interface ItemDragAdapter {

    void onItemMove(int fromPosition, int toPosition);

    void onItemClear(int fromPosition, int toPosition);

}
