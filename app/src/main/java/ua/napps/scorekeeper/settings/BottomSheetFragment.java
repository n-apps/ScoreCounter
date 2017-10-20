package ua.napps.scorekeeper.settings;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import ua.com.napps.scorekeeper.R;
import ua.napps.scorekeeper.app.Constants;
import ua.napps.scorekeeper.storage.TinyDB;


public class BottomSheetFragment extends BottomSheetDialogFragment {

    public BottomSheetFragment() {
        // Required empty public constructor
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        View contentView = View.inflate(getContext(), R.layout.fragment_settings_bottom_sheet, null);
        dialog.setContentView(contentView);
        SwitchCompat stayAwake = contentView.findViewById(R.id.sw_stay_awake);
        final TinyDB settingsDB = new TinyDB(getContext());
        boolean isStayAwake = settingsDB.getBoolean(Constants.SETTINGS_STAY_AWAKE);

        stayAwake.setChecked(isStayAwake);
        stayAwake.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settingsDB.putBoolean(Constants.SETTINGS_STAY_AWAKE, isChecked);
        });
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams)
                ((View) contentView.getParent()).getLayoutParams();
        final CoordinatorLayout.Behavior behavior = params.getBehavior();
        if (behavior != null && behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).setState(BottomSheetBehavior.STATE_HIDDEN);
        }
    }
}
