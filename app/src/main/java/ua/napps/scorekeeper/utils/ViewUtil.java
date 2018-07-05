package ua.napps.scorekeeper.utils;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION_CODES;
import android.support.annotation.CheckResult;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.Pair;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ViewUtil {

    /**
     * Sets the status and/or nav bar to be light or not. Light status bar means dark icons.
     *
     * @param activity  activity
     * @param isLight   make sure the system bar is light.
     * @param statusBar if true, make the status bar theme match the isLight param.
     * @param navBar    if true, make the nav bar theme match the isLight param.
     */
    @RequiresApi(api = VERSION_CODES.M)
    public static void setLightStatusBar(@NonNull Activity activity, boolean isLight, boolean statusBar, boolean navBar) {

        Window window = activity.getWindow();
        int oldSystemUiFlags = window.getDecorView().getSystemUiVisibility();
        int newSystemUiFlags = oldSystemUiFlags;
        if (isLight) {
            if (statusBar) {
                window.setStatusBarColor(Color.WHITE);
                newSystemUiFlags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                if (Utilities.checkIsMiuiRom()) {
                    Class<? extends Window> clazz = window.getClass();
                    try {
                        Class<?> layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
                        Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
                        int darkModeFlag = field.getInt(layoutParams);
                        Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
                        extraFlagField.invoke(window, statusBar ? darkModeFlag : 0, darkModeFlag);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            if (navBar && Utilities.isAtLeastO()) {
//                window.setNavigationBarColor(Color.WHITE);
                newSystemUiFlags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            }
        } else {
            if (statusBar) {
                window.setStatusBarColor(Color.BLACK);
                newSystemUiFlags &= ~(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
            if (navBar && Utilities.isAtLeastO()) {
//                window.setNavigationBarColor(Color.BLACK);
                newSystemUiFlags &= ~(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
            }
        }

        if (newSystemUiFlags != oldSystemUiFlags) {
            window.getDecorView().setSystemUiVisibility(newSystemUiFlags);
        }
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
