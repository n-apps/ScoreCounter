package ua.napps.scorekeeper.View;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
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
import ua.com.napps.scorekeeper.R;
import ua.napps.scorekeeper.Models.FavoriteSet;
import ua.napps.scorekeeper.Utils.PrefUtil;
import ua.napps.scorekeeper.View.EditFavoriteSetFragment.EditFavSetDialogListener;

import static ua.napps.scorekeeper.Helpers.Constants.FAV_ARRAY;

public class FavoriteSetsFragment extends Fragment implements EditFavSetDialogListener {


    @Bind(R.id.emptyStateFav)
    View mEmptyState;

    @Bind(R.id.FavoriteSetsRecyclerView)
    RecyclerView mFavoritesRecyclerView;

    @OnClick(R.id.fab)
    public void onClick(View v) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        EditFavoriteSetFragment favDialog = EditFavoriteSetFragment.newInstance(null, true);
        favDialog.setTargetFragment(this, 0);
        favDialog.show(fm, "edit_fav_dialog");
    }

    FavoriteSetsAdapter mFavoriteSetsAdapter;
    FavSetLoadedListener mCallback;

    public static FavoriteSetsFragment newInstance() {
        FavoriteSetsFragment fragment = new FavoriteSetsFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.favorite_sets_fragment, container, false);
        ButterKnife.bind(this, view);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.favorites_title);

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        setHasOptionsMenu(true);
        mFavoriteSetsAdapter = new FavoriteSetsAdapter(getFavorites());
        mFavoritesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mFavoritesRecyclerView.setAdapter(mFavoriteSetsAdapter);
        checkEmptyState();
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mCallback = (FavSetLoadedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement FavSetLoadedListener");
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        super.onCreateOptionsMenu(menu, inflater);
    }

    public ArrayList<FavoriteSet> getFavorites() {
        ArrayList<FavoriteSet> favoriteSets = new ArrayList<>();
        String json = PrefUtil.getString(getContext(), FAV_ARRAY, "");

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

    private void checkEmptyState() {
        mEmptyState.setVisibility((getFavorites().size() == 0) ? View.VISIBLE : View.GONE);
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

    public interface FavSetLoadedListener {
        void onFavSetLoaded(FavoriteSet set);
    }

    class FavoritesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @Bind(R.id.setName)
        TextView mSetName;
        @Bind(R.id.editSet)
        ImageView mEditSet;
        @Bind(R.id.setIcon)
        ImageView mSetIcon;

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
                    mCallback.onFavSetLoaded(mFavoriteSetsAdapter.getItem(position));
                    getActivity().onBackPressed();
                    break;
                case R.id.editSet:
                    FragmentManager fm = getActivity().getSupportFragmentManager();
                    EditFavoriteSetFragment favDialog = EditFavoriteSetFragment.newInstance(mFavoriteSetsAdapter.getItem(position), false);
                    favDialog.setTargetFragment(FavoriteSetsFragment.this, 0);
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
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            View v = layoutInflater.inflate(R.layout.favorite_set_item, parent, false);
            return new FavoritesViewHolder(v);
        }

        @Override
        public void onBindViewHolder(FavoritesViewHolder holder, int position) {
            holder.mSetName.setText(mFavoriteSets.get(position).getName());
            holder.mSetIcon.setColorFilter(mFavoriteSets.get(position).getIconColor());
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
            String favSetsJson = new Gson().toJson(mFavoriteSets);
            PrefUtil.putString(getContext(), FAV_ARRAY, favSetsJson);
            notifyItemInserted(position);
        }

        public void update(FavoriteSet item) {
            int position = mFavoriteSets.indexOf(item);
            String favSetsJson = new Gson().toJson(mFavoriteSets);
            PrefUtil.putString(getContext(), FAV_ARRAY, favSetsJson);
            notifyItemChanged(position);
        }

        public void remove(FavoriteSet item) {
            int position = mFavoriteSets.indexOf(item);
            mFavoriteSets.remove(position);
            String favSetsJson = new Gson().toJson(mFavoriteSets);
            PrefUtil.putString(getContext(), FAV_ARRAY, favSetsJson);
            notifyItemRemoved(position);
        }
    }
}
