//package ua.napps.scorekeeper.Adapters;
//
//import android.content.Context;
//import android.support.v7.widget.RecyclerView;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Comparator;
//
//import butterknife.Bind;
//import butterknife.ButterKnife;
//import butterknife.OnClick;
//import de.greenrobot.event.EventBus;
//import ua.com.napps.scorekeeper.R;
//import ua.napps.scorekeeper.DialogEditFav;
//import ua.napps.scorekeeper.Events.FavoriteSetLoaded;
//import ua.napps.scorekeeper.Interactors.FavoritesInteractorImpl;
//import ua.napps.scorekeeper.Models.FavoriteSet;
//
//import static ua.napps.scorekeeper.Interactors.FavoritesInteractorImpl.*;
//
//public class FavoriteSetsAdapter extends RecyclerView.Adapter<FavoriteSetsAdapter.FavoritesViewHolder> {
//    private final ArrayList<FavoriteSet> mItems;
//    private final LayoutInflater mInflater;
//    private final Context mContext;
//    private final View mEmptyStateView;
//    private boolean currentSetSaved;
//
//    public FavoriteSetsAdapter(Context context, View emptyStateView) {
//        this.mItems = getInstance(context).getFavorites();
//        this.mEmptyStateView = emptyStateView;
//        this.mContext = context;
//        this.mInflater = LayoutInflater.from(context);
//        if (mItems.size() > 1) {
//            FavoriteSet remove = mItems.remove(0);
//            Collections.sort(mItems, new Comparator<FavoriteSet>() {
//                @Override
//                public int compare(FavoriteSet i1, FavoriteSet i2) {
//                    return i1.getName().compareTo(i2.getName());
//                }
//            });
//            mItems.add(0, remove);
//            notifyDataSetChanged();
//        }
//    }
//
////    @Override
////    public FavoritesViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
////        View v = mInflater.inflate(R.layout.fav_item, viewGroup, false);
////        return new FavoritesViewHolder(v);
////    }
////
////    @Override
////    public void onBindViewHolder(FavoritesViewHolder holder, int i) {
////        FavoriteSet favItem = mItems.get(i);
////        holder.icon.setColorFilter(favItem.getIconColor());
////        holder.name.setText(favItem.getName());
////        if (i == 0) {
////            if (currentSetSaved) {
////                ViewGroup.LayoutParams params = holder.view.getLayoutParams();
////                params.width = 0;
////                params.height = 0;
////                holder.view.setLayoutParams(params);
////            } else {
////                holder.currentSetView.setVisibility(View.VISIBLE);
////                holder.savedSetView.setVisibility(View.INVISIBLE);
////                holder.name.setEnabled(false);
////            }
////        } else {
////            holder.name.setEnabled(true);
////            holder.currentSetView.setVisibility(View.INVISIBLE);
////            holder.savedSetView.setVisibility(View.VISIBLE);
////        }
////        if (mItems.size() < 2) mEmptyStateView.setVisibility(View.VISIBLE);
////        else mEmptyStateView.setVisibility(View.GONE);
////    }
////
////    @Override
////    public int getItemCount() {
////        return mItems.size();
////    }
////
////    public void delFavItem(FavoriteSet favItem) {
////        mItems.remove(favItem);
////        notifyDataSetChanged();
////    }
////
////    public void saveCurrentSet(FavoriteSet item) {
////        getInstance(mContext).addFav(1, item);
////        currentSetSaved = true;
////        notifyDataSetChanged();
////    }
//
//
//}
