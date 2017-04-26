package ua.napps.scorekeeper.utils;

import android.databinding.BindingAdapter;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.widget.ImageView;
import android.widget.TextView;

public class DataBindingAdapters {

    public static final String TAG = "DataBindingAdapters";

    /**
     * Prevent instantiation
     */
    private DataBindingAdapters() {
    }

    @BindingAdapter("colorTint") public static void setColorTint(ImageView view, int color) {
        DrawableCompat.setTint(view.getDrawable(), color);
    }

    @BindingAdapter({ "bind:font" })
    public static void setFont(TextView textView, String fontName) {
        textView.setTypeface(FontCache.getInstance(textView.getContext()).get(fontName));
    }

}
