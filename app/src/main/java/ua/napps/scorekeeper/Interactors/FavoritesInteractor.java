package ua.napps.scorekeeper.Interactors;

import java.util.ArrayList;

import ua.napps.scorekeeper.Models.FavoriteSet;

/**
 * Created by novo on 12/1/2015.
 */
public interface FavoritesInteractor {

    ArrayList<FavoriteSet> getFavorites();

    FavoriteSet getFavSet(int position);

    void addFav(int position, FavoriteSet set);

    void removeFav(int position);
}
