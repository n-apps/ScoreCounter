package ua.napps.scorekeeper.utils;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;

import ua.napps.scorekeeper.R;

public class ViewUtil {

    public static void setLightStatusBar(Activity activity) {
        if (Utilities.hasMarshmallow()) {
            int oldFlags = activity.getWindow().getDecorView().getSystemUiVisibility();
            // Apply the state flags in priority order
            int newFlags = oldFlags;
            newFlags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            if (newFlags != oldFlags) {
                activity.getWindow().getDecorView().setSystemUiVisibility(newFlags);
            }
        } else {
            if (Utilities.hasLollipop()) {
                activity.getWindow().setStatusBarColor(Color.BLACK);
            }
        }
    }

    public static void clearLightStatusBar(Activity activity) {
        if (Utilities.hasMarshmallow()) {
            int oldFlags = activity.getWindow().getDecorView().getSystemUiVisibility();
            // Apply the state flags in priority order
            int newFlags = oldFlags;
            newFlags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            if (newFlags != oldFlags) {
                activity.getWindow().getDecorView().setSystemUiVisibility(newFlags);
            }
            activity.getWindow().setStatusBarColor(activity.getColor(R.color.primaryBackground));
        }

    }

    public static void setNavBarColor(Activity activity, boolean isLight) {
        if (Utilities.hasOreo()) {
            int oldFlags = activity.getWindow().getDecorView().getSystemUiVisibility();
            // Apply the state flags in priority order
            int newFlags = oldFlags;
            if (isLight) {
                newFlags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            } else {
                newFlags &= ~(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
            }
            activity.getWindow().setNavigationBarColor(activity.getColor(R.color.primaryBackground));
            if (newFlags != oldFlags) {
                activity.getWindow().getDecorView().setSystemUiVisibility(newFlags);
            }
        }
    }
}
