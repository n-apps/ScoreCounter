package ua.napps.scorekeeper.Models;

import android.graphics.Color;

import java.io.Serializable;
import java.util.ArrayList;

public class FavoriteSet implements Serializable {

    private int iconColor = Color.rgb(224, 224, 224);
    private String name;
    private ArrayList<Counter> counters = new ArrayList<>();

    public FavoriteSet(String name) {
        this.setName(name);
    }

    public int getIconColor() {
        return iconColor;
    }

    public void setIconColor(int iconColor) {
        this.iconColor = iconColor;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Counter> getCounters() {
        return counters;
    }

    public void setCounters(ArrayList<Counter> counters) {
        this.counters = counters;
    }

}
