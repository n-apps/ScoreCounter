package ua.napps.scorekeeper.utils;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import androidx.annotation.CheckResult;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import android.util.Pair;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.lang.reflect.Field;

public class ViewUtil {

    public static void setLightStatusBar(Activity activity) {
        setLightStatusBar(activity, 0);
    }

    public static void setLightStatusBar(Activity activity, int color) {
        if (Utilities.hasMarshmallow()) {
            int oldFlags = activity.getWindow().getDecorView().getSystemUiVisibility();
            // Apply the state flags in priority order
            int newFlags = oldFlags;
            newFlags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            if (newFlags != oldFlags) {
                activity.getWindow().getDecorView().setSystemUiVisibility(newFlags);
            }
        }
        setStatusBarColor(activity, color);
    }

    public static void clearLightStatusBar(Activity activity) {
        clearLightStatusBar(activity, 0);
    }

    public static void clearLightStatusBar(Activity activity, int color) {
        if (Utilities.hasMarshmallow()) {
            int oldFlags = activity.getWindow().getDecorView().getSystemUiVisibility();
            // Apply the state flags in priority order
            int newFlags = oldFlags;
            newFlags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            if (newFlags != oldFlags) {
                activity.getWindow().getDecorView().setSystemUiVisibility(newFlags);
            }
        }
        setStatusBarColor(activity, color);
    }

    public static void setStatusBarColor(Activity activity, int color) {
        if (Utilities.hasLollipop() && color != 0) {
            activity.getWindow().setStatusBarColor(color);
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
