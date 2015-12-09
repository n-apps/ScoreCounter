package ua.napps.scorekeeper.Adapters;

import android.media.Image;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import ua.com.napps.scorekeeper.R;
import ua.napps.scorekeeper.Helpers.Constants;
import ua.napps.scorekeeper.View.MainActivity;
import ua.napps.scorekeeper.Models.FavoriteSet;

public class AdapterRecent extends RecyclerView.Adapter<AdapterRecent.RecentViewHolder> {
    private final ArrayList<FavoriteSet> list;
    private final LayoutInflater inflater;
    private final MainActivity context;

    public AdapterRecent(MainActivity context, ArrayList<FavoriteSet> list) {
        this.list = list;
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public RecentViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = inflater.inflate(R.layout.fav_item, viewGroup, false);
        return new RecentViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecentViewHolder holder, int i) {
        FavoriteSet favItem = list.get(i);
        if (i == 0 || i > Constants.RECENT_LIST_SIZE || favItem.getLastLoaded() == 0) {
            ViewGroup.LayoutParams params = holder.view.getLayoutParams();
            params.width = 0;
            params.height = 0;
            holder.view.setLayoutParams(params);
        } else {
            holder.icon.setColorFilter(favItem.getIconColor());
            holder.name.setText(favItem.getName());
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class RecentViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final View view;

        @Bind(R.id.diceIcon)
        ImageView icon;

        @Bind(R.id.editTextDiceEdges)
        TextView name;

        @Bind(R.id.editSet)
        ImageView savedSet;

        public RecentViewHolder(View view) {
            super(view);
            this.view = view;
            ButterKnife.bind(this, view);
            savedSet.setVisibility(View.INVISIBLE);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            //  context.onFavItemClick(getAdapterPosition());
        }
    }
}
