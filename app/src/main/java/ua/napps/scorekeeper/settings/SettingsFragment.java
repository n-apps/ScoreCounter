package ua.napps.scorekeeper.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import ua.com.napps.scorekeeper.BuildConfig;
import ua.com.napps.scorekeeper.R;
import ua.napps.scorekeeper.app.Constants;
import ua.napps.scorekeeper.storage.TinyDB;
import ua.napps.scorekeeper.utils.AndroidFirebaseAnalytics;


public class SettingsFragment extends Fragment implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    private ToggleButton diceSix;
    private ToggleButton diceEight;
    private ToggleButton diceTwenty;
    private ToggleButton diceCustom;
    private int currentDiceVariant;
    private TinyDB settingsDB;

    public SettingsFragment() {
        // Required empty public constructor
    }

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_settings, null);
        SwitchCompat stayAwake = contentView.findViewById(R.id.sw_stay_awake);
        SwitchCompat shakeToRoll = contentView.findViewById(R.id.sw_shake_roll);
        diceSix = contentView.findViewById(R.id.tb_dice_6);
        diceEight = contentView.findViewById(R.id.tb_dice_8);
        diceTwenty = contentView.findViewById(R.id.tb_dice_20);
        diceCustom = contentView.findViewById(R.id.tb_dice_x);
        contentView.findViewById(R.id.tv_request_feature).setOnClickListener(v -> startEmailClient());
        contentView.findViewById(R.id.tv_have_a_problem).setOnClickListener(v -> startEmailClient());

        settingsDB = new TinyDB(getContext());
        boolean isStayAwake = settingsDB.getBoolean(Constants.SETTINGS_KEEP_SCREEN_ON, true);
        boolean shakeToRollEnabled = settingsDB.getBoolean(Constants.SETTINGS_SHAKE_TO_ROLL, true);
        currentDiceVariant = settingsDB.getInt(Constants.SETTINGS_DICE_VARIANT, 6);

        refreshDices(false);

        stayAwake.setChecked(isStayAwake);
        shakeToRoll.setChecked(shakeToRollEnabled);

        stayAwake.setOnCheckedChangeListener(this);
        shakeToRoll.setOnCheckedChangeListener(this);
        diceSix.setOnClickListener(this);
        diceEight.setOnClickListener(this);
        diceTwenty.setOnClickListener(this);
        diceCustom.setOnClickListener(this);

        return contentView;
    }

    private void refreshDices(boolean storeInDB) {
        if (storeInDB && currentDiceVariant <= 100) {
            settingsDB.putInt(Constants.SETTINGS_DICE_VARIANT, currentDiceVariant);
        }
        switch (currentDiceVariant) {
            case 6:
                diceSix.setChecked(true);
                diceEight.setChecked(false);
                diceTwenty.setChecked(false);
                diceCustom.setChecked(false);
                diceCustom.setTextOff("X");
                diceCustom.setTextOn("X");
                break;
            case 8:
                diceSix.setChecked(false);
                diceEight.setChecked(true);
                diceTwenty.setChecked(false);
                diceCustom.setChecked(false);
                diceCustom.setTextOff("X");
                diceCustom.setTextOn("X");
                break;
            case 20:
                diceSix.setChecked(false);
                diceEight.setChecked(false);
                diceTwenty.setChecked(true);
                diceCustom.setChecked(false);
                diceCustom.setTextOff("X");
                diceCustom.setTextOn("X");
                diceCustom.setText("X");
                break;
            default:
                diceSix.setChecked(false);
                diceEight.setChecked(false);
                diceTwenty.setChecked(false);
                diceCustom.setChecked(true);
                String label = "" + currentDiceVariant;
                diceCustom.setTextOff(label);
                diceCustom.setTextOn(label);
                diceCustom.setText(label);
                break;
        }
    }

    private void startEmailClient() {
        final String title = getString(R.string.app_name) + " " + BuildConfig.VERSION_NAME;

        Intent intent = new Intent(Intent.ACTION_SENDTO,
                Uri.fromParts("mailto", Constants.FEEDBACK_EMAIL_ADDRESS, null));
        intent.putExtra(Intent.EXTRA_EMAIL, Constants.FEEDBACK_EMAIL_ADDRESS);
        intent.putExtra(Intent.EXTRA_SUBJECT, title);
        startActivity(Intent.createChooser(intent, getString(R.string.dialog_feedback_title)));
        AndroidFirebaseAnalytics.logEvent(getContext(), "settings_send_feedback");
    }

    @Override
    public void onCheckedChanged(CompoundButton v, boolean isChecked) {
        switch (v.getId()) {
            case R.id.sw_shake_roll:
                settingsDB.putBoolean(Constants.SETTINGS_SHAKE_TO_ROLL, isChecked);
                break;
            case R.id.sw_dark_theme:
                settingsDB.putBoolean(Constants.SETTINGS_DICE_THEME_LIGHT, !isChecked);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tb_dice_6:
                currentDiceVariant = 6;
                refreshDices(true);
                break;
            case R.id.tb_dice_8:
                currentDiceVariant = 8;
                refreshDices(true);
                break;
            case R.id.tb_dice_20:
                currentDiceVariant = 20;
                refreshDices(true);
                break;
            case R.id.tb_dice_x:
                final MaterialDialog md = new MaterialDialog.Builder(getActivity())
                        .content("Custom dice sides. From 1 to 100")
                        .inputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED)
                        .positiveText(R.string.common_set)
                        .negativeColorRes(R.color.primaryColor)
                        .negativeText(R.string.common_cancel)
//                        .typeface("work_sans_light.ttf", "source_sans_pro_regular.ttf")
                        .alwaysCallInputCallback()
                        .input("From 1 to 100", null, false,
                                (dialog, input) -> {
                                    if (input.length() > 0) {
                                        int parseInt = Integer.parseInt(input.toString());
                                        if (parseInt <= 100 && parseInt > 0) {
                                            dialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);
                                        } else {
                                            dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
                                        }
                                    }
                                })
                        .onPositive((dialog, which) -> {
                            currentDiceVariant = Integer.parseInt(dialog.getInputEditText().getText().toString());
                            refreshDices(true);
                        })
                        .build();
                md.getInputEditText().setOnEditorActionListener((textView, actionId, event) -> {
                    if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId
                            == EditorInfo.IME_ACTION_DONE)) {
                        View positiveButton = md.getActionButton(DialogAction.POSITIVE);
                        positiveButton.callOnClick();
                    }
                    return false;
                });
                md.show();
                break;
        }
    }
}
