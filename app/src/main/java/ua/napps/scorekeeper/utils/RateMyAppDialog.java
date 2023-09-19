package ua.napps.scorekeeper.utils;

import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;

import java.util.Date;

import timber.log.Timber;
import ua.napps.scorekeeper.R;
import ua.napps.scorekeeper.settings.LocalSettings;

public class RateMyAppDialog {

    public static final int MIM_LAUNCH_TIMES = 5;
    public static final int MIN_DAYS = 5;

    private final FragmentActivity context;
    private Dialog dialog;

    public RateMyAppDialog(FragmentActivity activity) {
        this.context = activity;
    }

    public void onStart() {
        if (didDonateAlready()) return;

        int launchTimes = LocalSettings.getAppLaunchTimes();
        long firstDate = LocalSettings.getFirstHitDate();

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

    public void showAnyway() {
        tryShow(context);
    }

    private void remindMeLater() {
        registerHitCount(0);
        registerDate();
    }

    private boolean isShowing() {
        return dialog != null && dialog.isShowing();
    }

    private boolean didDonateAlready() {
        return LocalSettings.didDonate();
    }


    private void tryShow(FragmentActivity activity) {
        if (isShowing())
            return;

        try {
            dialog = null;
            dialog = createDialog(activity);
            dialog.show();
        } catch (Exception e) {
            //It prevents many Android exceptions
            //when user interactions conflicts with UI thread or Activity expired window token
            //BadTokenException, IllegalStateException ...
            Timber.e(e);
        }
    }

    private boolean shouldShow() {
        if (didDonateAlready()) return false;

        int launchTimes = LocalSettings.getAppLaunchTimes();
        long firstDate = LocalSettings.getFirstHitDate();
        long today = new Date().getTime();

        return daysBetween(firstDate, today) > MIN_DAYS && launchTimes > MIM_LAUNCH_TIMES;
    }

    private void registerHitCount(int hitCount) {
        LocalSettings.saveAppLaunchTimes(hitCount);
    }

    private void registerDate() {
        Date today = new Date();
        LocalSettings.saveFirstHitDate(today.getTime());
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

        contentView.findViewById(R.id.iv_donate).setOnClickListener(v -> {
            DonateDialog donateDialog = new DonateDialog();
            donateDialog.show(context.getSupportFragmentManager(), "donate");
            dialog.dismiss();
        });
        contentView.findViewById(R.id.btn_donate_it).setOnClickListener(v -> {
            DonateDialog donateDialog = new DonateDialog();
            donateDialog.show(context.getSupportFragmentManager(), "donate");
            dialog.dismiss();
        });
        contentView.findViewById(R.id.btn_remind_later).setOnClickListener(v -> {
            remindMeLater();
            dialog.dismiss();
        });

        return dialog;
    }

}
