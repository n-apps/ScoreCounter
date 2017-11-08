package ua.napps.scorekeeper.settings;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import ua.com.napps.scorekeeper.R;
import ua.napps.scorekeeper.storage.TinyDB;


public class SettingsFragment extends BottomSheetDialogFragment {

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
        SwitchCompat tryToFitAllCounters = contentView.findViewById(R.id.sw_try_to_fit_all_counters);
        final TinyDB settingsDB = new TinyDB(getContext());
        boolean isStayAwake = settingsDB.getBoolean(SettingsUtil.SETTINGS_KEEP_SCREEN_ON, true);
        boolean isTryToFitAllCounters = settingsDB.getBoolean(SettingsUtil.SETTINGS_TRY_TO_FIT_ALL_COUNTERS, false);

        stayAwake.setChecked(isStayAwake);
        stayAwake.setOnCheckedChangeListener(
                (buttonView, isChecked) -> settingsDB.putBoolean(SettingsUtil.SETTINGS_KEEP_SCREEN_ON, isChecked));
        tryToFitAllCounters.setChecked(isTryToFitAllCounters);
        tryToFitAllCounters.setOnCheckedChangeListener((buttonView, isChecked) -> settingsDB
                .putBoolean(SettingsUtil.SETTINGS_TRY_TO_FIT_ALL_COUNTERS, isChecked));
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams)
                ((View) contentView.getParent()).getLayoutParams();
        final CoordinatorLayout.Behavior behavior = params.getBehavior();
        if (behavior != null && behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).setState(BottomSheetBehavior.STATE_HIDDEN);
        }
    }
}
