package ua.napps.scorekeeper.Interactors;

import android.content.Context;
import android.content.SharedPreferences;

import com.apkfuns.logutils.LogUtils;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import ua.napps.scorekeeper.Helpers.Constants;
import ua.napps.scorekeeper.Models.FavoriteSet;

import static ua.napps.scorekeeper.Helpers.Constants.PREFS_NAME;

/**
 * Created by novo on 12/1/2015.
 */
public class FavoritesInteractorImpl implements FavoritesInteractor {

    private Context mContext;
    private ArrayList<FavoriteSet> mFavoriteSets;

    private static FavoritesInteractorImpl instance;

    private FavoritesInteractorImpl(Context context) {
        this.mContext = context;
    }

    public static FavoritesInteractorImpl getInstance(Context context) {
        if (instance == null) {
            instance = new FavoritesInteractorImpl(context);
            LogUtils.i("new instance");
        }
        return instance;
    }

    @Override
    public ArrayList<FavoriteSet> getFavorites() {
        LogUtils.i("getFavorites");

        if (mFavoriteSets == null) {
            LogUtils.i("access SharedPreferences");
            SharedPreferences sp = mContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String json = sp.getString(Constants.FAV_ARRAY, "");

            try {
                Type listType = new TypeToken<ArrayList<FavoriteSet>>() {
                }.getType();
                mFavoriteSets = new Gson().fromJson(json, listType);
                LogUtils.i(String.format("mFavorites size: %d", mFavoriteSets.size()));
            } catch (JsonSyntaxException ex) {
                LogUtils.e(ex.getMessage());
            }
        }
        return mFavoriteSets;
    }

    @Override
    public FavoriteSet getFavSet(int position) {
        LogUtils.i("getFavSet");
        return getFavorites().get(position);
    }

    @Override
    public void addFav(int position, FavoriteSet set) {
        LogUtils.i("addFav");
        getFavorites().add(set);
    }

    @Override
    public void removeFav(int position) {
        LogUtils.i("removeFav");
        getFavorites().remove(position);
    }
}
