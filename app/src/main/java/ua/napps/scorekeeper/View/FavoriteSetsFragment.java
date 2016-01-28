package ua.napps.scorekeeper.View;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import ua.com.napps.scorekeeper.R;
import ua.com.napps.scorekeeper.databinding.FragmentSetsBinding;
import ua.napps.scorekeeper.Models.FavoriteSet;
import ua.napps.scorekeeper.Utils.PrefUtil;
import ua.napps.scorekeeper.View.EditFavoriteSetFragment.EditFavSetDialogListener;

import static ua.napps.scorekeeper.Helpers.Constants.FAV_ARRAY;

@SuppressWarnings({ "WeakerAccess", "unused" }) public class FavoriteSetsFragment extends Fragment
    implements EditFavSetDialogListener {

  LinearLayout emptyState;
  RecyclerView recyclerView;

  private FavoriteSetsAdapter setsAdapter;
  FavSetLoadedListener mCallback;

  public static FavoriteSetsFragment newInstance() {
    return new FavoriteSetsFragment();
  }

  @SuppressWarnings("ConstantConditions") @Nullable @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    FragmentSetsBinding binding =
        DataBindingUtil.inflate(inflater, R.layout.fragment_sets, container, false);

    setHasOptionsMenu(true);
    binding.toolbar.setTitle(getString(R.string.favorite_sets_title));
    binding.toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
    binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        getActivity().onBackPressed();
      }
    });

    emptyState = binding.emptyState;
    recyclerView = binding.favoriteSetsRecyclerView;
    binding.favoriteSetsFab.setOnClickListener(onFabClick);

    initRecyclerView();
    checkEmptyState();

    return binding.getRoot();
  }

  private void initRecyclerView() {
    if (setsAdapter == null) setsAdapter = new FavoriteSetsAdapter(getFavorites());
    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    recyclerView.setAdapter(setsAdapter);
  }

  @Override public void onAttach(Context context) {
    super.onAttach(context);

    try {
      mCallback = (FavSetLoadedListener) context;
    } catch (ClassCastException e) {
      throw new ClassCastException(context.toString() + " must implement FavSetLoadedListener");
    }
  }

  @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    menu.clear();
    super.onCreateOptionsMenu(menu, inflater);
  }

  private ArrayList<FavoriteSet> getFavorites() {
    ArrayList<FavoriteSet> favoriteSets = new ArrayList<>();
    String json = PrefUtil.getString(getContext(), FAV_ARRAY, "");

    try {
      Type listType = new TypeToken<ArrayList<FavoriteSet>>() {
      }.getType();
      favoriteSets = new Gson().fromJson(json, listType);
      if (favoriteSets == null) favoriteSets = new ArrayList<>();
    } catch (JsonSyntaxException ex) {
      Log.e("---", ex.getMessage());
    }

    return favoriteSets;
  }

  private void checkEmptyState() {
    emptyState.setVisibility((getFavorites().size() == 0) ? View.VISIBLE : View.GONE);
  }

  @Override public void onFavSetUpdated(FavoriteSet set) {
    setsAdapter.update(set);
  }

  @Override public void onFavSetDeleted(FavoriteSet set) {
    setsAdapter.remove(set);
    checkEmptyState();
  }

  @Override public void onFavSetAdded(FavoriteSet set) {
    setsAdapter.add(set);
    checkEmptyState();
  }

  View.OnClickListener onFabClick = new View.OnClickListener() {
    @Override public void onClick(View v) {
      FragmentManager fm = getActivity().getSupportFragmentManager();
      EditFavoriteSetFragment favDialog = EditFavoriteSetFragment.newInstance(null, true);
      favDialog.show(fm, "edit_fav_dialog");
    }
  };

  public interface FavSetLoadedListener {
    void onFavSetLoaded(FavoriteSet set);
  }

  @SuppressWarnings("unused") class FavoritesViewHolder extends RecyclerView.ViewHolder
      implements View.OnClickListener {

    @Bind(R.id.favorite_set_name) TextView mSetName;
    @Bind(R.id.favorite_set_edit) ImageView mEditSet;
    @Bind(R.id.favorite_set_icon) ImageView mSetIcon;

    public FavoritesViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);

      itemView.setOnClickListener(this);
      mEditSet.setOnClickListener(this);
    }

    @Override public void onClick(View v) {
      int position = getAdapterPosition();
      int id = v.getId();

      switch (id) {
        case R.id.favorite_set_item:
          mCallback.onFavSetLoaded(setsAdapter.getItem(position));
          getActivity().onBackPressed();
          break;
        case R.id.favorite_set_edit:
          FragmentManager fm = getActivity().getSupportFragmentManager();
          EditFavoriteSetFragment favDialog =
              EditFavoriteSetFragment.newInstance(setsAdapter.getItem(position), false);
          favDialog.show(fm, "edit_fav_dialog");
          break;
      }
    }
  }

  public class FavoriteSetsAdapter extends RecyclerView.Adapter<FavoritesViewHolder> {

    private final ArrayList<FavoriteSet> mFavoriteSets;

    public FavoriteSetsAdapter(ArrayList<FavoriteSet> favoriteSets) {
      mFavoriteSets = favoriteSets;
    }

    @Override public FavoritesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      LayoutInflater layoutInflater = LayoutInflater.from(getContext());
      View v = layoutInflater.inflate(R.layout.favorite_set_item, parent, false);
      return new FavoritesViewHolder(v);
    }

    @Override public void onBindViewHolder(FavoritesViewHolder holder, int position) {
      holder.mSetName.setText(mFavoriteSets.get(position).getName());
      holder.mSetIcon.setColorFilter(mFavoriteSets.get(position).getIconColor());
    }

    @Override public int getItemCount() {
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
