package ua.napps.scorekeeper.Events;

import ua.napps.scorekeeper.Models.FavoriteSet;

/**
 * Created by novo on 11/30/2015.
 */
public class FavoriteSetLoaded {
    private FavoriteSet set;

    public FavoriteSetLoaded(FavoriteSet set){
        this.set = set;
    }

    public FavoriteSet getSet() {
        return set;
    }
}
