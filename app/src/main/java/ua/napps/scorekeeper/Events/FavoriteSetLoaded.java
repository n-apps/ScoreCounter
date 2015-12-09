package ua.napps.scorekeeper.Events;

/**
 * Created by novo on 11/30/2015.
 */
public class FavoriteSetLoaded {
    int position;

    public FavoriteSetLoaded(int pos){
        this.position = pos;
    }

    public int getFavoriteSetPosition() {
        return position;
    }

}
