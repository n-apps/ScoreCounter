package ua.napps.scorekeeper.Events;

import java.util.ArrayList;

import ua.napps.scorekeeper.Models.FavoriteSet;

/**
 * Created by novo on 14-Dec-15.
 */
public class FavoritesUpdated {
    private ArrayList<FavoriteSet> mFavoriteSets;

    public FavoritesUpdated(ArrayList<FavoriteSet> favoriteSets) {
        this.mFavoriteSets = favoriteSets;
    }

    public ArrayList<FavoriteSet> getFavorites() {
        return mFavoriteSets;
    }
}

