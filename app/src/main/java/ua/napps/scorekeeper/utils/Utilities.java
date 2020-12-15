package ua.napps.scorekeeper.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;
import ua.napps.scorekeeper.R;
import ua.napps.scorekeeper.app.App;
import ua.napps.scorekeeper.settings.LocalSettings;

public class Utilities {

    public static final Pattern pattern = Pattern.compile("[\\-0-9]+");

    public static Integer parseInt(String value) {
        if (value == null) {
            return 0;
        }
        int val = 0;
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

    /**
     * Checks if the device has Marshmallow or higher version.
     *
     * @return <code>true</code> if device is a tablet.
     */
    public static boolean hasMarshmallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    /**
     * Checks if the device has Nougat or higher version.
     *
     * @return <code>true</code> if device is a tablet.
     */
    public static boolean hasNougat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
    }

    /**
     * Checks if the device has Oreo or higher version.
     *
     * @return <code>true</code> if device is a tablet.
     */
    public static boolean hasOreo() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    /**
     * Checks if the device has Q or higher version.
     *
     * @return <code>true</code> if device is a tablet.
     */
    public static boolean hasQ() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
    }

    public static void rateApp(Context context) {
        Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        goToMarket.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        LocalSettings.markRateApp();

        try {
            context.startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            Uri playStoreUri = Uri.parse("http://play.google.com/store/apps/details?id=" + context.getPackageName());
            Intent intent = new Intent(Intent.ACTION_VIEW, playStoreUri);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }


    public static void startEmail(Context context) {
        String mailTo = "mailto:scorekeeper.feedback@gmail.com" + "?subject=" +
                Uri.encode(context.getString(R.string.app_name));

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mailTo));
        if (intent.resolveActivity(App.getInstance().getPackageManager()) != null) {
            context.startActivity(intent);
        } else {
            Toast.makeText(context, R.string.error_no_email_client, Toast.LENGTH_SHORT).show();
        }
    }

}
