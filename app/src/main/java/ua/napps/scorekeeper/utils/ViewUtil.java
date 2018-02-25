package ua.napps.scorekeeper.utils;

import android.app.Activity;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import ua.com.napps.scorekeeper.R;

public class ViewUtil {

    public static void setLightStatusBar(@NonNull Activity activity, boolean isLightStatusBar) {

        Window window = activity.getWindow();

        int oldSystemUiFlags = activity.getWindow().getDecorView().getSystemUiVisibility();
        int newSystemUiFlags = oldSystemUiFlags;
        int lightColor = ContextCompat.getColor(activity, R.color.light_status_bar);
        int darkColor = ContextCompat.getColor(activity, R.color.dark_status_bar);

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
        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            window.setNavigationBarColor(darkColor);
            newSystemUiFlags &= ~(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }
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

}
