package it.uniba.dib.sms222332.student;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;

public class ThesisTouchHelperCallback extends ItemTouchHelper.Callback {

    private FavoritesAdapter adapter;

    public ThesisTouchHelperCallback(FavoritesAdapter adapter){
        this.adapter = adapter;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return false;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {

        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;

        return makeMovementFlags(dragFlags, 0);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {

       adapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());

       return true;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

    }
}
