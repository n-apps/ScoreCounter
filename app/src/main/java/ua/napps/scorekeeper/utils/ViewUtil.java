package ua.napps.scorekeeper.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import ua.napps.scorekeeper.R;

public class ViewUtil {

    public static void applyTheme(Activity activity, boolean nightModeActive, boolean moreScreen) {
        int statusBarColor = moreScreen ?
                ContextCompat.getColor(activity, R.color.colorPrimaryVariant) :
                ContextCompat.getColor(activity, R.color.colorSurface);

        boolean lightMode = moreScreen || !nightModeActive;
        activity.getWindow().setStatusBarColor(statusBarColor);
        setLightMode(activity, lightMode);
    }

    public static void setLightMode(@NonNull Activity activity, boolean light) {
        Window window = activity.getWindow();
        View decorView = window.getDecorView();
        setMIUIStatusBarDarkIcon(activity, light);
        setMeizuStatusBarDarkIcon(activity, light);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
            WindowInsetsControllerCompat insetsController = WindowCompat.getInsetsController(window, decorView);
            insetsController.setAppearanceLightStatusBars(light);
        } else {
            int systemUi = decorView.getSystemUiVisibility();
            if (light) {
                systemUi |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            } else {
                systemUi &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
            decorView.setSystemUiVisibility(systemUi);
        }
    }

    private static void setMIUIStatusBarDarkIcon(@NonNull Activity activity, boolean darkIcon) {
        Class<? extends Window> clazz = activity.getWindow().getClass();
        try {
            Class<?> layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
            Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
            int darkModeFlag = field.getInt(layoutParams);
            Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
            extraFlagField.invoke(activity.getWindow(), darkIcon ? darkModeFlag : 0, darkModeFlag);
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    private static void setMeizuStatusBarDarkIcon(@NonNull Activity activity, boolean darkIcon) {
        try {
            WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
            Field darkFlag = WindowManager.LayoutParams.class.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
            Field meizuFlags = WindowManager.LayoutParams.class.getDeclaredField("meizuFlags");
            darkFlag.setAccessible(true);
            meizuFlags.setAccessible(true);
            int bit = darkFlag.getInt(null);
            int value = meizuFlags.getInt(lp);
            if (darkIcon) {
                value |= bit;
            } else {
                value &= ~bit;
            }
            meizuFlags.setInt(lp, value);
            activity.getWindow().setAttributes(lp);
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    public static void setNavBarColor(@NonNull Activity activity, boolean isLight) {
        int oldFlags = activity.getWindow().getDecorView().getSystemUiVisibility();
        // Apply the state flags in priority order
        int newFlags = oldFlags;
        if (isLight) {
            newFlags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        } else {
            newFlags &= ~(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }
        activity.getWindow().setNavigationBarColor(activity.getColor(R.color.colorSurface));
        if (newFlags != oldFlags) {
            activity.getWindow().getDecorView().setSystemUiVisibility(newFlags);
        }
    }

    public static void setNavBarColor(@NonNull Activity activity, boolean isLight, @ColorInt int navigationBarColor) {
        int oldFlags = activity.getWindow().getDecorView().getSystemUiVisibility();
        // Apply the state flags in priority order
        int newFlags = oldFlags;
        if (isLight) {
            newFlags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        } else {
            newFlags &= ~(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }
        activity.getWindow().setNavigationBarColor(navigationBarColor);
        if (newFlags != oldFlags) {
            activity.getWindow().getDecorView().setSystemUiVisibility(newFlags);
        }
    }

    public static boolean isNightModeActive(Context context) {
        int defaultNightMode = AppCompatDelegate.getDefaultNightMode();
        if (defaultNightMode == AppCompatDelegate.MODE_NIGHT_YES) {
            return true;
        }
        if (defaultNightMode == AppCompatDelegate.MODE_NIGHT_NO) {
            return false;
        }

        int currentNightMode = context.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
        switch (currentNightMode) {
            case Configuration.UI_MODE_NIGHT_NO:
            case Configuration.UI_MODE_NIGHT_UNDEFINED:
                return false;
            case Configuration.UI_MODE_NIGHT_YES:
                return true;
        }
        return false;
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

    public static int dip2px(float dpValue, Context context) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

}
