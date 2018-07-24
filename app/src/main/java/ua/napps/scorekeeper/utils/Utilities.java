package ua.napps.scorekeeper.utils;

import android.os.Build;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;

public class Utilities {

    public static Pattern pattern = Pattern.compile("[\\-0-9]+");

    public static Integer parseInt(String value) {
        if (value == null) {
            return 0;
        }
        Integer val = 0;
        try {
            Matcher matcher = pattern.matcher(value);
            if (matcher.find()) {
                String num = matcher.group(0);
                val = Integer.parseInt(num);
            }
        } catch (Exception e) {
            Timber.e(e);
        }
        return val;
    }

    public static boolean checkIsMiuiRom() {
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

    /**
     * Checks if the device has Lolllipop or higher version.
     * @return <code>true</code> if device is a tablet.
     */
    public static boolean hasLollipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    /**
     * Checks if the device has Marshmallow or higher version.
     * @return <code>true</code> if device is a tablet.
     */
    public static boolean hasMarshmallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    /**
     * Checks if the device has Marshmallow or higher version.
     * @return <code>true</code> if device is a tablet.
     */
    public static boolean hasNougat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
    }

    /**
     * Checks if the device has Oreo or higher version.
     * @return <code>true</code> if device is a tablet.
     */
    public static boolean hasOreo() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

}
