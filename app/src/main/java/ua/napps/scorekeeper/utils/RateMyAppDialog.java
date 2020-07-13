package ua.napps.scorekeeper.utils;

import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;

import java.util.Date;

import timber.log.Timber;
import ua.napps.scorekeeper.R;
import ua.napps.scorekeeper.app.App;
import ua.napps.scorekeeper.settings.LocalSettings;

public class RateMyAppDialog {

    private static final String KEY_FIRST_HIT_DATE = "KEY_FIRST_HIT_DATE";
    private static final String KEY_LAUNCH_TIMES = "KEY_LAUNCH_TIMES";

    private final FragmentActivity context;
    private Dialog dialog;

    public RateMyAppDialog(FragmentActivity context) {
        this.context = context;
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

    public void showIfNeeded() {
        if (shouldShow()) {
            tryShow(context);
        }
    }

    private void remindMeLater() {
        registerHitCount(0);
        registerDate();
    }

    private boolean isShowing() {
        return dialog != null && dialog.isShowing();
    }

    private boolean didRate() {
        return LocalSettings.didRate();
    }

    private boolean didNeverReminder() {
        return LocalSettings.didNeverReminder();
    }

    public void tryShow(FragmentActivity context) {
        if (isShowing())
            return;

        try {
            dialog = null;
            dialog = createDialog(context);
            dialog.show();
        } catch (Exception e) {
            //It prevents many Android exceptions
            //when user interactions conflicts with UI thread or Activity expired window token
            //BadTokenException, IllegalStateException ...
            Timber.e(e);
        }
    }

    private boolean shouldShow() {
        if (didRate() || didNeverReminder()) return false;

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

    private Dialog createDialog(FragmentActivity context) {
        final View contentView = LayoutInflater.from(context).inflate(R.layout.dialog_rate_app, null, false);

        final AlertDialog dialog = new AlertDialog.Builder(context)
                .setOnCancelListener(d -> remindMeLater())
                .setView(contentView)
                .create();

        contentView.findViewById(R.id.btn_rate_it).setOnClickListener(v -> {
            Utilities.rateApp(context);
            dialog.dismiss();
        });
        contentView.findViewById(R.id.btn_donate_it).setOnClickListener(v -> {
            DonateDialog donateDialog = new DonateDialog();
            donateDialog.show(context.getSupportFragmentManager(), "donate");
            LocalSettings.markRateApp();
            dialog.dismiss();
        });
        contentView.findViewById(R.id.btn_remind_later).setOnClickListener(v -> {
            remindMeLater();
            dialog.dismiss();
        });
        contentView.findViewById(R.id.btn_no_thanks).setOnClickListener(v -> {
            LocalSettings.neverReminder();
            dialog.dismiss();
        });

        return dialog;
    }
}
