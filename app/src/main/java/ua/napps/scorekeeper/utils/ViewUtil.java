package ua.napps.scorekeeper.utils;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;

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
            activity.getWindow().setStatusBarColor(Color.BLACK);
        }

    }

    public static void setNavBarColor(Activity activity, boolean isLight) {
        if (Utilities.hasOreo()) {
            int oldFlags = activity.getWindow().getDecorView().getSystemUiVisibility();
            // Apply the state flags in priority order
            int newFlags = oldFlags;
            if (isLight) {
                newFlags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                activity.getWindow().setNavigationBarColor(Color.WHITE);
            } else {
                newFlags &= ~(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
                activity.getWindow().setNavigationBarColor(Color.BLACK);
            }
            if (newFlags != oldFlags) {
                activity.getWindow().getDecorView().setSystemUiVisibility(newFlags);
            }
        }
    }
}
