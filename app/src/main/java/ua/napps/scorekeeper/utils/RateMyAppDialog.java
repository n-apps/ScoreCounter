package ua.napps.scorekeeper.utils;

import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.View;

import androidx.fragment.app.FragmentActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.MaterialShapeDrawable;

import timber.log.Timber;
import ua.napps.scorekeeper.R;
import ua.napps.scorekeeper.settings.LocalSettings;
import ua.napps.scorekeeper.settings.TipActivity;

public class RateMyAppDialog {

    public static final int LAUNCHES_UNTIL_PROMPT = 6;
    public static final int DAYS_UNTIL_PROMPT = 3;

    private final FragmentActivity context;
    private Dialog dialog;

    public RateMyAppDialog(FragmentActivity activity) {
        this.context = activity;
    }

    public void showIfNeeded() {
        if (shouldShow()) {
            tryShow(context);
        } else {
            int launchTimes = LocalSettings.getAppLaunchTimes();

            registerHitCount(++launchTimes);
        }
    }

    public void showAnyway() {
        tryShow(context);
    }

    private void remindMeLater() {
        registerHitCount(0);
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

        int launchCount = LocalSettings.getAppLaunchTimes();

        if (launchCount >= LAUNCHES_UNTIL_PROMPT) {
            long firstLaunch = LocalSettings.getFirstHitDate();
            return System.currentTimeMillis() >= firstLaunch +
                    (DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000);
        }
        return false;
    }

    private void registerHitCount(int hitCount) {
        LocalSettings.saveAppLaunchTimes(hitCount);
    }

    private Dialog createDialog(FragmentActivity context) {
        final View contentView = LayoutInflater.from(context).inflate(R.layout.dialog_rate_app, null, false);

        final MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(context)
                .setOnCancelListener(d -> remindMeLater())
                .setView(contentView);

        contentView.findViewById(R.id.btn_donate_it).setOnClickListener(v -> {
            showTipScreen();
            dialog.dismiss();
        });
        contentView.findViewById(R.id.btn_remind_later).setOnClickListener(v -> {
            remindMeLater();
            dialog.dismiss();
        });

        // Set a custom ShapeAppearanceModel
        MaterialShapeDrawable alertBackground = (MaterialShapeDrawable) materialAlertDialogBuilder.getBackground();
        if (alertBackground != null) {
            alertBackground.setShapeAppearanceModel(
                    alertBackground.getShapeAppearanceModel()
                            .toBuilder()
                            .setAllCorners(CornerFamily.ROUNDED, ViewUtil.dip2px(16, context))
                            .build());
        }
        return materialAlertDialogBuilder.create();
    }

    private void showTipScreen() {
        TipActivity.start(context);
    }

}
