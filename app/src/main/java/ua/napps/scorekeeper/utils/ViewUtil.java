package ua.napps.scorekeeper.utils;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.support.annotation.CheckResult;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.text.TextUtils;
import android.util.Pair;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ViewUtil {

    public static void setLightStatusBar(@NonNull Activity activity, boolean isLightStatusBar, @ColorInt int lightColor, @ColorInt int darkColor) {

        Window window = activity.getWindow();

        int oldSystemUiFlags = activity.getWindow().getDecorView().getSystemUiVisibility();
        int newSystemUiFlags = oldSystemUiFlags;

        if (isLightStatusBar) {
            if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
                window.setStatusBarColor(lightColor);
                if (VERSION.SDK_INT >= VERSION_CODES.M) {
                    newSystemUiFlags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                }
            }
        } else {
            if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
                window.setStatusBarColor(darkColor);
                if (VERSION.SDK_INT >= VERSION_CODES.M) {
                    newSystemUiFlags &= ~(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                }
            }
        }
//        if (VERSION.SDK_INT >= VERSION_CODES.O) {
//            window.setNavigationBarColor(darkColor);
//            newSystemUiFlags &= ~(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
//        }
        if (newSystemUiFlags != oldSystemUiFlags) {
            window.getDecorView().setSystemUiVisibility(newSystemUiFlags);
        }
        if (checkIsMiuiRom()) {
            Class<? extends Window> clazz = window.getClass();
            try {
                Class<?> layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
                Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
                int darkModeFlag = field.getInt(layoutParams);
                Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
                extraFlagField.invoke(window, isLightStatusBar ? darkModeFlag : 0, darkModeFlag);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private static boolean checkIsMiuiRom() {
        return !TextUtils.isEmpty(getSystemProperty("ro.miui.ui.version.name"));
    }

    private static String getSystemProperty(String propName) {
        String line;
        BufferedReader input = null;
        try {
            Process p = Runtime.getRuntime().exec("getprop " + propName);
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
        } catch (IOException ex) {
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    // do nothing
                }
            }
        }
        return line;
    }

    public static Pair<Float, Float> getCenter(final View view) {
        return Pair.create(
                view.getX() + view.getWidth() / 2,
                view.getY() + view.getHeight() / 2
        );
    }

    public static void setCursorTint(@NonNull EditText editText, @ColorInt int color) {
        try {
            Field fCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
            fCursorDrawableRes.setAccessible(true);
            int mCursorDrawableRes = fCursorDrawableRes.getInt(editText);
            Field fEditor = TextView.class.getDeclaredField("mEditor");
            fEditor.setAccessible(true);
            Object editor = fEditor.get(editText);
            Class<?> clazz = editor.getClass();
            Field fCursorDrawable = clazz.getDeclaredField("mCursorDrawable");
            fCursorDrawable.setAccessible(true);
            Drawable[] drawables = new Drawable[2];
            drawables[0] = ContextCompat.getDrawable(editText.getContext(), mCursorDrawableRes);
            drawables[0] = createTintedDrawable(drawables[0], color);
            drawables[1] = ContextCompat.getDrawable(editText.getContext(), mCursorDrawableRes);
            drawables[1] = createTintedDrawable(drawables[1], color);
            fCursorDrawable.set(editor, drawables);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // This returns a NEW Drawable because of the mutate() call. The mutate() call is necessary because Drawables with the same resource have shared states otherwise.
    @CheckResult
    @Nullable
    private static Drawable createTintedDrawable(@Nullable Drawable drawable, @ColorInt int color) {
        if (drawable == null) return null;
        drawable = DrawableCompat.wrap(drawable.mutate());
        DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_IN);
        DrawableCompat.setTint(drawable, color);
        return drawable;
    }

    // This returns a NEW Drawable because of the mutate() call. The mutate() call is necessary because Drawables with the same resource have shared states otherwise.
    @CheckResult
    @Nullable
    public static Drawable createTintedDrawable(@Nullable Drawable drawable, @NonNull ColorStateList sl) {
        if (drawable == null) return null;
        drawable = DrawableCompat.wrap(drawable.mutate());
        DrawableCompat.setTintList(drawable, sl);
        return drawable;
    }
}
