package ua.napps.scorekeeper.settings;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.SwitchCompat;
import android.view.View;

import ua.com.napps.scorekeeper.BuildConfig;
import ua.com.napps.scorekeeper.R;
import ua.napps.scorekeeper.storage.TinyDB;
import ua.napps.scorekeeper.utils.AndroidFirebaseAnalytics;


public class SettingsFragment extends BottomSheetDialogFragment {

    private static final String FEEDBACK_EMAIL_ADDRESS = "scorekeeper.feedback@gmail.com";

    public SettingsFragment() {
        // Required empty public constructor
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        View contentView = View.inflate(getContext(), R.layout.fragment_settings_bottom_sheet, null);
        dialog.setContentView(contentView);
        SwitchCompat stayAwake = contentView.findViewById(R.id.sw_stay_awake);
        contentView.findViewById(R.id.send_feedback).setOnClickListener(v -> {
            startEmailClient();
            this.dismiss();
        });
        final TinyDB settingsDB = new TinyDB(getContext());
        boolean isStayAwake = settingsDB.getBoolean(Constants.SETTINGS_KEEP_SCREEN_ON, true);
        stayAwake.setChecked(isStayAwake);
        stayAwake.setOnCheckedChangeListener(
                (buttonView, isChecked) -> settingsDB.putBoolean(Constants.SETTINGS_KEEP_SCREEN_ON, isChecked));
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams)
                ((View) contentView.getParent()).getLayoutParams();
        final CoordinatorLayout.Behavior behavior = params.getBehavior();
        if (behavior != null && behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).setState(BottomSheetBehavior.STATE_HIDDEN);
        }
    }

    private void startEmailClient() {
        final String title = getString(R.string.app_name) + " " + BuildConfig.VERSION_NAME;

        Intent intent = new Intent(Intent.ACTION_SENDTO,
                Uri.fromParts("mailto", FEEDBACK_EMAIL_ADDRESS, null));
        intent.putExtra(Intent.EXTRA_EMAIL, FEEDBACK_EMAIL_ADDRESS);
        intent.putExtra(Intent.EXTRA_SUBJECT, title);
        startActivity(Intent.createChooser(intent, getString(R.string.dialog_feedback_title)));
        AndroidFirebaseAnalytics.logEvent(getContext(), "settings_send_feedback");
    }
}
