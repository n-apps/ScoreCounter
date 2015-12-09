package ua.napps.scorekeeper.View;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.Bind;
import butterknife.ButterKnife;
import ua.com.napps.scorekeeper.R;
import ua.napps.scorekeeper.Adapters.FavoriteSetsAdapter;
import ua.napps.scorekeeper.DialogAddFavSet;
import ua.napps.scorekeeper.DialogEditFav;
import ua.napps.scorekeeper.Models.Counter;
import ua.napps.scorekeeper.Models.FavoriteSet;

import static ua.napps.scorekeeper.Interactors.FavoritesInteractorImpl.getInstance;

public class FragmentFav extends Fragment {

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.emptyStateFav)
    View emptyState;

    @Bind(R.id.favRv)
    RecyclerView favoritesRecyclerView;

    @Bind(R.id.addFavSetFAB)
    FloatingActionButton mFloatingActionButton;

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
                context.closeFragment();
            }
        });
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
                                                     @Override
                                                     public void onClick(View v) {
                                                         new DialogAddFavSet(context);
                                                     }
                                                 }
        );
        favoritesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        favoritesRecyclerView.setAdapter(new FavoriteSetsAdapter(context, emptyState));
        return view;
    }


}
