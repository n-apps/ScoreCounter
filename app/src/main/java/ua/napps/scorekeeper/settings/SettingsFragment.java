package ua.napps.scorekeeper.settings;

import android.app.Activity;
import android.content.ActivityNotFoundException;
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

import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import ua.com.napps.scorekeeper.R;
import ua.napps.scorekeeper.app.App;
import ua.napps.scorekeeper.utils.FirebaseAnalytics;
import ua.napps.scorekeeper.utils.Utilities;


public class SettingsFragment extends Fragment implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    private ToggleButton diceSix,diceEight,diceTwenty,diceCustom;
    private Button btn_c_1,btn_c_2,btn_c_3,btn_c_4;
    private int diceMaxSide;

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
        SwitchCompat keepScreenOn = contentView.findViewById(R.id.sw_keep_screen_on);
        SwitchCompat darkTheme = contentView.findViewById(R.id.sw_dark_theme);
        SwitchCompat shakeToRoll = contentView.findViewById(R.id.sw_shake_roll);

        diceSix = contentView.findViewById(R.id.tb_dice_6);
        diceEight = contentView.findViewById(R.id.tb_dice_8);
        diceTwenty = contentView.findViewById(R.id.tb_dice_20);
        diceCustom = contentView.findViewById(R.id.tb_dice_x);

        btn_c_1 = contentView.findViewById(R.id.btn_settings_counter_1);
        btn_c_2 = contentView.findViewById(R.id.btn_settings_counter_2);
        btn_c_3 = contentView.findViewById(R.id.btn_settings_counter_3);
        btn_c_4 = contentView.findViewById(R.id.btn_settings_counter_4);

        keepScreenOn.setChecked(LocalSettings.isKeepScreenOnEnabled());
        darkTheme.setChecked(LocalSettings.isDarkTheme());
        shakeToRoll.setChecked(LocalSettings.isShakeToRollEnabled());
        diceMaxSide = LocalSettings.getDiceMaxSide();
        keepScreenOn.setOnCheckedChangeListener(this);
        shakeToRoll.setOnCheckedChangeListener(this);
        darkTheme.setOnCheckedChangeListener(this);
        contentView.findViewById(R.id.tv_request_feature).setOnClickListener(this);
        contentView.findViewById(R.id.tv_rate_app).setOnClickListener(this);

        diceSix.setOnClickListener(this);
        diceEight.setOnClickListener(this);
        diceTwenty.setOnClickListener(this);
        diceCustom.setOnClickListener(this);

        btn_c_1.setOnClickListener(this);
        btn_c_2.setOnClickListener(this);
        btn_c_3.setOnClickListener(this);
        btn_c_4.setOnClickListener(this);

        btn_c_1.setText(String.valueOf(LocalSettings.getCustomCounter(1)));
        btn_c_2.setText(String.valueOf(LocalSettings.getCustomCounter(2)));
        btn_c_3.setText(String.valueOf(LocalSettings.getCustomCounter(3)));
        btn_c_4.setText(String.valueOf(LocalSettings.getCustomCounter(4)));

        refreshDices(false);
        return contentView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseAnalytics.trackScreen(getActivity(), "Settings");
    }

    @Override
    public void onCheckedChanged(CompoundButton v, boolean isChecked) {
        switch (v.getId()) {
            case R.id.sw_shake_roll:
                LocalSettings.saveShakeToRoll(isChecked);
                break;
            case R.id.sw_keep_screen_on:
                LocalSettings.saveKeepScreenOn(isChecked);
                break;
            case R.id.sw_dark_theme:
                LocalSettings.saveDarkTheme(isChecked);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_request_feature:
                FirebaseAnalytics.logEvent("request_a_feature_click");
                startEmailClient();
                break;
            case R.id.tv_rate_app:
                FirebaseAnalytics.logEvent("rate_app__click");
                rateApp();
                break;
            case R.id.tb_dice_6:
                diceMaxSide = 6;
                refreshDices(true);
                break;
            case R.id.tb_dice_8:
                diceMaxSide = 8;
                refreshDices(true);
                break;
            case R.id.tb_dice_20:
                diceMaxSide = 20;
                refreshDices(true);
                break;
            case R.id.tb_dice_x:
                final MaterialDialog md = new MaterialDialog.Builder(requireActivity())
                        .content(R.string.dialog_custom_dice_title)
                        .inputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED)
                        .positiveText(R.string.common_set)
                        .negativeColorRes(R.color.primaryColor)
                        .negativeText(R.string.common_cancel)
                        .alwaysCallInputCallback()
                        .cancelListener(dialog -> refreshDices(false))
                        .input(getString(R.string.dialog_custom_dice_hint), null, false,
                                (dialog, input) -> {
                                    int parseInt = Utilities.parseInt(input.toString());
                                    if (parseInt <= 100 && parseInt > 1) {
                                        dialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);
                                    } else {
                                        dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
                                    }
                                })
                        .onPositive((dialog, which) -> {
                            EditText editText = dialog.getInputEditText();
                            if (editText != null) {
                                Integer side = Utilities.parseInt(editText.getText().toString());
                                if (side > 0) {
                                    diceMaxSide = side;
                                    refreshDices(true);
                                }
                                dialog.dismiss();
                            }
                        })
                        .build();
                EditText editText = md.getInputEditText();
                if (editText != null) {
                    editText.setOnEditorActionListener((textView, actionId, event) -> {
                        if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                            View positiveButton = md.getActionButton(DialogAction.POSITIVE);
                            positiveButton.callOnClick();
                        }
                        return false;
                    });
                }
                md.show();
                break;
            case R.id.btn_settings_counter_1:
                openCustomCounterDialog(1);
                break;
            case R.id.btn_settings_counter_2:
                openCustomCounterDialog(2);
                break;
            case R.id.btn_settings_counter_3:
                openCustomCounterDialog(3);
                break;
            case R.id.btn_settings_counter_4:
                openCustomCounterDialog(4);
                break;
        }
    }

    private void openCustomCounterDialog(final int id){
        final MaterialDialog customCounterDialog = new MaterialDialog.Builder(requireActivity())
                .content(R.string.dialog_custom_counter_title)
                .inputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED)
                .positiveText(R.string.common_set)
                .negativeColorRes(R.color.primaryColor)
                .negativeText(R.string.common_cancel)
                .alwaysCallInputCallback()
                .input(getString(R.string.dialog_custom_counter_hint), null, false,
                        (dialog, input) -> {
                            int parseInt = Utilities.parseInt(input.toString());
                            if (parseInt <= 999 && parseInt > 1) {
                                dialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);
                            } else {
                                dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
                            }
                        })
                .onPositive((dialog, which) -> {
                    EditText editText = dialog.getInputEditText();
                    if (editText != null) {
                        Integer parseInt = Utilities.parseInt(editText.getText().toString());
                        if (parseInt <= 999 && parseInt > 1) {
                            setCustomCounter(id,parseInt);
                        }
                        dialog.dismiss();
                        FirebaseAnalytics.logEvent("set_custom_counter_value_for_dialog");
                    }
                })
                .build();

        EditText editText = customCounterDialog.getInputEditText();
        if (editText != null) {
            editText.setOnEditorActionListener((textView, actionId, event) -> {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    View positiveButton = customCounterDialog.getActionButton(DialogAction.POSITIVE);
                    positiveButton.callOnClick();
                }
                return false;
            });
        }
        customCounterDialog.show();
    }

    private void setCustomCounter(int id, int value){
        LocalSettings.saveCustomCounter(id,value);
        switch (id){
            case 1:
                btn_c_1.setText(String.valueOf(value));
                break;
            case 2:
                btn_c_2.setText(String.valueOf(value));
                break;
            case 3:
                btn_c_3.setText(String.valueOf(value));
                break;
            case 4:
                btn_c_4.setText(String.valueOf(value));
                break;
        }
    }

    private void rateApp() {
        Activity activity = requireActivity();
        Uri uri = Uri.parse("market://details?id=" + activity.getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            activity.startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            Uri playStoreUri = Uri.parse("http://play.google.com/store/apps/details?id=" + activity.getPackageName());
            activity.startActivity(new Intent(Intent.ACTION_VIEW, playStoreUri));
        }
    }

    private void refreshDices(boolean storeInDB) {
        if (storeInDB && diceMaxSide <= 100) {
            LocalSettings.saveDiceMaxSide(diceMaxSide);
        }
        switch (diceMaxSide) {
            case 6:
                diceSix.setChecked(true);
                diceEight.setChecked(false);
                diceTwenty.setChecked(false);
                diceCustom.setChecked(false);
                diceCustom.setTextOff("?");
                diceCustom.setTextOn("?");
                break;
            case 8:
                diceSix.setChecked(false);
                diceEight.setChecked(true);
                diceTwenty.setChecked(false);
                diceCustom.setChecked(false);
                diceCustom.setTextOff("?");
                diceCustom.setTextOn("?");
                break;
            case 20:
                diceSix.setChecked(false);
                diceEight.setChecked(false);
                diceTwenty.setChecked(true);
                diceCustom.setChecked(false);
                diceCustom.setTextOff("?");
                diceCustom.setTextOn("?");
                diceCustom.setText("?");
                break;
            default:
                diceSix.setChecked(false);
                diceEight.setChecked(false);
                diceTwenty.setChecked(false);
                diceCustom.setChecked(true);
                String label = "" + diceMaxSide;
                diceCustom.setTextOff(label);
                diceCustom.setTextOn(label);
                diceCustom.setText(label);
                break;
        }
    }

    private void startEmailClient() {
        final String title = getString(R.string.app_name);

        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "scorekeeper.feedback@gmail.com", null));
        intent.putExtra(Intent.EXTRA_EMAIL, "scorekeeper.feedback@gmail.com");
        intent.putExtra(Intent.EXTRA_SUBJECT, title);
        if (intent.resolveActivity(App.getInstance().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(getContext(), R.string.error_no_email_client, Toast.LENGTH_SHORT).show();
        }
    }
}
