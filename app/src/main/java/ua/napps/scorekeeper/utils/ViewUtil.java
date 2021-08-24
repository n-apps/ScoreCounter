package ua.napps.scorekeeper.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
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
            activity.getWindow().setStatusBarColor(Color.BLACK);
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

    public static void shakeView(final View view, final float x, final int num) {
        if (view == null) {
            return;
        }
        if (num == 6) {
            view.setTranslationX(0);
            return;
        }
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(ObjectAnimator.ofFloat(view, "translationX", x));
        animatorSet.setDuration(50);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                shakeView(view, num == 5 ? 0 : -x, num + 1);
            }
        });
        animatorSet.start();
    }

}
