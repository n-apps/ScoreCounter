package ua.napps.scorekeeper.View;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.apkfuns.logutils.LogUtils;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import ua.com.napps.scorekeeper.R;
import ua.napps.scorekeeper.Events.FavoriteSetLoaded;
import ua.napps.scorekeeper.Events.FavoritesUpdated;
import ua.napps.scorekeeper.Helpers.Constants;
import ua.napps.scorekeeper.Models.FavoriteSet;
import ua.napps.scorekeeper.View.EditFavSetFragment.EditFavSetDialogListener;

import static ua.napps.scorekeeper.Helpers.Constants.PREFS_NAME;

public class FragmentFav extends Fragment implements EditFavSetDialogListener {

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.emptyStateFav)
    View emptyState;

    @Bind(R.id.favRv)
    RecyclerView favoritesRecyclerView;

    @OnClick(R.id.addFavSetFAB)
    public void onClick(View v) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        EditFavSetFragment favDialog = EditFavSetFragment.newInstance(null, true);
        favDialog.setTargetFragment(this, 0);
        favDialog.show(fm, "edit_fav_dialog");
    }

    FavoriteSetsAdapter mFavoriteSetsAdapter;

    public static FragmentFav newInstance() {
        FragmentFav fragment = new FragmentFav();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fav_fragment, container, false);
        ButterKnife.bind(this, view);

        toolbar.setTitle(R.string.favorites_title);
        final MainActivity context = (MainActivity) getActivity();
        context.setSupportActionBar(toolbar);
        ActionBar actionBar = context.getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.closeFragment("favorites");
            }
        });
        setHasOptionsMenu(true);
        mFavoriteSetsAdapter = new FavoriteSetsAdapter(getFavorites());
        favoritesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        favoritesRecyclerView.setAdapter(mFavoriteSetsAdapter);
        checkEmptyState();
        return view;
    }

    private void checkEmptyState() {
        emptyState.setVisibility((getFavorites().size() == 0) ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        super.onCreateOptionsMenu(menu, inflater);
    }

    public ArrayList<FavoriteSet> getFavorites() {
        ArrayList<FavoriteSet> favoriteSets = new ArrayList<>();
        SharedPreferences sp = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = sp.getString(Constants.FAV_ARRAY, "");

        try {
            Type listType = new TypeToken<ArrayList<FavoriteSet>>() {
            }.getType();
            favoriteSets = new Gson().fromJson(json, listType);
            if (favoriteSets == null) favoriteSets = new ArrayList<>();
        } catch (JsonSyntaxException ex) {
            LogUtils.e(ex.getMessage());
        }

        return favoriteSets;
    }

    @Override
    public void onFavSetUpdated(FavoriteSet set) {
        mFavoriteSetsAdapter.update(set);
    }

    @Override
    public void onFavSetDeleted(FavoriteSet set) {
        mFavoriteSetsAdapter.remove(set);
        checkEmptyState();

    }

    @Override
    public void onFavSetAdded(FavoriteSet set) {
        mFavoriteSetsAdapter.add(set);
        checkEmptyState();
    }

    class FavoritesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @Bind(R.id.setName)
        TextView mSetName;
        @Bind(R.id.editSet)
        ImageView mEditSet;

        public FavoritesViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
            mEditSet.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            int id = v.getId();

            switch (id) {
                case R.id.favItem:
                    EventBus.getDefault().post(new FavoriteSetLoaded(mFavoriteSetsAdapter.getItem(position)));
                    break;
                case R.id.editSet:
                    FragmentManager fm = getActivity().getSupportFragmentManager();
                    EditFavSetFragment favDialog = EditFavSetFragment.newInstance(mFavoriteSetsAdapter.getItem(position), false);
                    favDialog.setTargetFragment(FragmentFav.this, 0);
                    favDialog.show(fm, "edit_fav_dialog");
                    break;
            }
        }
    }

    public class FavoriteSetsAdapter extends RecyclerView.Adapter<FavoritesViewHolder> {

        private ArrayList<FavoriteSet> mFavoriteSets;

        public FavoriteSetsAdapter(ArrayList<FavoriteSet> favoriteSets) {
            mFavoriteSets = favoriteSets;
        }

        @Override
        public FavoritesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View v = layoutInflater.inflate(R.layout.fav_item, parent, false);
            return new FavoritesViewHolder(v);
        }

        @Override
        public void onBindViewHolder(FavoritesViewHolder holder, int position) {
            holder.mSetName.setText(mFavoriteSets.get(position).getName());
        }

        @Override
        public int getItemCount() {
            return mFavoriteSets.size();
        }

        public FavoriteSet getItem(int position) {
            return mFavoriteSets.get(position);
        }

        public void add(FavoriteSet item) {
            int position = mFavoriteSets.size();
            mFavoriteSets.add(position, item);
            EventBus.getDefault().post(new FavoritesUpdated(mFavoriteSets));
            notifyItemInserted(position);
        }

        public void update(FavoriteSet item) {
            int position = mFavoriteSets.indexOf(item);
            EventBus.getDefault().post(new FavoritesUpdated(mFavoriteSets));
            notifyItemChanged(position);
        }

        public void remove(FavoriteSet item) {
            int position = mFavoriteSets.indexOf(item);
            mFavoriteSets.remove(position);
            EventBus.getDefault().post(new FavoritesUpdated(mFavoriteSets));
            notifyItemRemoved(position);
        }
    }
}
