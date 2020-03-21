package ua.napps.scorekeeper.counters;

import android.graphics.Canvas;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class ItemDragHelperCallback extends ItemTouchHelper.Callback{


    private final ItemDragAdapter itemDragAdapter;
    private Integer lastFrom = null;
    private Integer lastTo = null;

    public ItemDragHelperCallback(ItemDragAdapter itemDragAdapter) {
        this.itemDragAdapter = itemDragAdapter;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        // We will start drag event manually
        return false;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        // Swiping not used
        return false;
    }


    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        final int dragFlags = ItemTouchHelper.UP   | ItemTouchHelper.DOWN |
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
        return makeMovementFlags(dragFlags, 0);
    }


    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, RecyclerView.ViewHolder source, RecyclerView.ViewHolder target) {
        // Called during dragging event
        if (source.getItemViewType() != target.getItemViewType()) {
            return false;
        }
        if (lastFrom == null) {
            lastFrom = source.getAdapterPosition();
        }
        lastTo = target.getAdapterPosition();
        itemDragAdapter.onItemMove(source.getAdapterPosition(), target.getAdapterPosition());
        return false;
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        //Do not allow dragging items out of bounds
        float topY = viewHolder.itemView.getTop() + dY;
        float bottomY = topY + viewHolder.itemView.getHeight();
        if (topY < 0) {
            dY = 0;
        } else if (bottomY > recyclerView.getHeight()) {
            dY = recyclerView.getHeight() - viewHolder.itemView.getHeight() - viewHolder.itemView.getTop();
        }

        float minX = viewHolder.itemView.getLeft() + dX;
        float maxX = minX + viewHolder.itemView.getWidth();
        if (minX < 0) {
            dX = 0;
        } else if (maxX > recyclerView.getWidth()) {
            dX = recyclerView.getWidth() - viewHolder.itemView.getWidth() - viewHolder.itemView.getLeft();
        }
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    @Override
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        // Called after dragging event, when item is released
        super.clearView(recyclerView, viewHolder);

        if (lastFrom != null && lastTo != null) {
            itemDragAdapter.onItemClear(lastFrom, lastTo);
        }
        lastFrom = null;
        lastTo = null;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
       // No action when swiped
    }


}
