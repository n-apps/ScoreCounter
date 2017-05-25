package ua.napps.scorekeeper.utils;

import android.databinding.BindingAdapter;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.View;
import android.widget.ImageView;

public class DataBindingAdapters {

    public static final String TAG = "DataBindingAdapters";

    /**
     * Prevent instantiation
     */
    private DataBindingAdapters() {
    }

    @BindingAdapter("colorTint") public static void setColorTint(ImageView view, int color) {
        DrawableCompat.wrap(view.getDrawable());
        DrawableCompat.setTint(view.getDrawable(), color);
    }

    @BindingAdapter({ "bind:repeatableCallback" })
    public static void setRepeatableClick(View view, View.OnClickListener onClickListener) {
        view.setOnTouchListener(new RepeatListener(onClickListener, null));
    }
}
