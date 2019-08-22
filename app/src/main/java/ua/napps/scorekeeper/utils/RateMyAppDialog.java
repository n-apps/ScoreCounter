package ua.napps.scorekeeper.utils;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import java.util.Date;

import timber.log.Timber;
import ua.napps.scorekeeper.R;
import ua.napps.scorekeeper.app.App;

public class RateMyAppDialog {
    private static final String KEY_WAS_RATED = "KEY_WAS_RATED";
    private static final String KEY_NEVER_REMINDER = "KEY_NEVER_REMINDER";
    private static final String KEY_FIRST_HIT_DATE = "KEY_FIRST_HIT_DATE";
    private static final String KEY_LAUNCH_TIMES = "KEY_LAUNCH_TIMES";

    private final Context mContext;
    private Dialog mDialog;

    public RateMyAppDialog(Context context) {
        mContext = context;
    }

    public void onStart() {
        if (didRate() || didNeverReminder()) return;

        int launchTimes = App.getTinyDB().getInt(KEY_LAUNCH_TIMES, 0);
        long firstDate = App.getTinyDB().getLong(KEY_FIRST_HIT_DATE, -1L);

        if (firstDate == -1L) {
            registerDate();
        }

        registerHitCount(++launchTimes);
    }

    public void showAnyway() {
        tryShow(mContext);
    }

    public void showIfNeeded() {
        if (shouldShow()) {
            tryShow(mContext);
        }
    }

    private void neverReminder() {
        App.getTinyDB().putBoolean(KEY_NEVER_REMINDER, true);
    }

    private void remindMeLater() {
        registerHitCount(0);
        registerDate();
    }

    private boolean isShowing() {
        return mDialog != null && mDialog.isShowing();
    }

    private boolean didRate() {
        return App.getTinyDB().getBoolean(KEY_WAS_RATED, false);
    }

    private boolean didNeverReminder() {
        return App.getTinyDB().getBoolean(KEY_NEVER_REMINDER, false);
    }

    private void tryShow(Context context) {
        if (isShowing())
            return;

        try {
            mDialog = null;
            mDialog = createDialog(context);
            mDialog.show();
            AndroidFirebaseAnalytics.logEvent("RateMyAppScreenAppear");
        } catch (Exception e) {
            //It prevents many Android exceptions
            //when user interactions conflicts with UI thread or Activity expired window token
            //BadTokenException, IllegalStateException ...
            Timber.e(e);
        }
    }


    private boolean shouldShow() {
        if (App.getTinyDB().getBoolean(KEY_NEVER_REMINDER, false)) {
            return false;
        }
        if (App.getTinyDB().getBoolean(KEY_WAS_RATED, false)) {
            return false;
        }

        int launchTimes = App.getTinyDB().getInt(KEY_LAUNCH_TIMES, 0);
        long firstDate = App.getTinyDB().getLong(KEY_FIRST_HIT_DATE, 0L);
        long today = new Date().getTime();

        return daysBetween(firstDate, today) > 14 || launchTimes > 18;
    }

    private void registerHitCount(int hitCount) {
        App.getTinyDB().putInt(KEY_LAUNCH_TIMES, Math.min(hitCount, Integer.MAX_VALUE));
    }

    private void registerDate() {
        Date today = new Date();
        App.getTinyDB().putLong(KEY_FIRST_HIT_DATE, today.getTime());
    }

    private long daysBetween(long firstDate, long lastDate) {
        return (lastDate - firstDate) / (1000 * 60 * 60 * 24);
    }

    private Dialog createDialog(Context context) {
        final View contentView = LayoutInflater.from(context).inflate(R.layout.dialog_rate_app, null, false);

        final AlertDialog dialog = new AlertDialog.Builder(context)
                .setOnCancelListener(d -> remindMeLater())
                .setView(contentView)
                .create();

        contentView.findViewById(R.id.btn_rate_it).setOnClickListener(v -> {
            dialog.dismiss();
            Utilities.rateApp(context);
            AndroidFirebaseAnalytics.logEvent("RateMyAppScreenRateClick");
        });
        contentView.findViewById(R.id.btn_remind_later).setOnClickListener(v -> {
            remindMeLater();
            AndroidFirebaseAnalytics.logEvent("RateMyAppScreenRemindLaterClick");
            dialog.dismiss();
        });
        contentView.findViewById(R.id.btn_no_thanks).setOnClickListener(v -> {
            neverReminder();
            AndroidFirebaseAnalytics.logEvent("RateMyAppScreenNoThanksClick");
            dialog.dismiss();
        });

        return dialog;
    }
}
