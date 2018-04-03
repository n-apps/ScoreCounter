package ua.napps.scorekeeper.settings;

import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.analytics.FirebaseAnalytics;

import ua.com.napps.scorekeeper.BuildConfig;
import ua.com.napps.scorekeeper.R;
import ua.napps.scorekeeper.app.App;
import ua.napps.scorekeeper.utils.AndroidFirebaseAnalytics;
import ua.napps.scorekeeper.utils.Utilities;


public class SettingsFragment extends Fragment implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    private ToggleButton diceSix;
    private ToggleButton diceEight;
    private ToggleButton diceTwenty;
    private ToggleButton diceCustom;
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
        keepScreenOn.setChecked(LocalSettings.isKeepScreenOnEnabled());
        darkTheme.setChecked(LocalSettings.isDarkTheme());
        shakeToRoll.setChecked(LocalSettings.isShakeToRollEnabled());
        diceMaxSide = LocalSettings.getDiceMaxSide();
        keepScreenOn.setOnCheckedChangeListener(this);
        shakeToRoll.setOnCheckedChangeListener(this);
        darkTheme.setOnCheckedChangeListener(this);
        contentView.findViewById(R.id.tv_request_feature).setOnClickListener(this);
        contentView.findViewById(R.id.tv_have_a_problem).setOnClickListener(this);
        contentView.findViewById(R.id.tv_telegram_direct).setOnClickListener(this);
        diceSix.setOnClickListener(this);
        diceEight.setOnClickListener(this);
        diceTwenty.setOnClickListener(this);
        diceCustom.setOnClickListener(this);
        refreshDices(false);
        return contentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        AndroidFirebaseAnalytics.trackScreen(getActivity(), "Settings");
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
                AndroidFirebaseAnalytics.logEvent("request_a_feature_click");
                startEmailClient();
                break;
            case R.id.tv_have_a_problem:
                AndroidFirebaseAnalytics.logEvent("i_have_a_problem_click");
                startEmailClient();
                break;
            case R.id.tv_telegram_direct:
                openTelegram();
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
                        .inputRange(1, 3)
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
        final String title = getString(R.string.app_name) + " " + BuildConfig.VERSION_NAME;

        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "scorekeeper.feedback@gmail.com", null));
        intent.putExtra(Intent.EXTRA_EMAIL, "scorekeeper.feedback@gmail.com");
        intent.putExtra(Intent.EXTRA_SUBJECT, title);
        if (intent.resolveActivity(App.getInstance().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(getContext(), R.string.error_no_email_client, Toast.LENGTH_SHORT).show();
        }
    }

    private void openTelegram() {
        Bundle params = new Bundle();
        if (isPackageInstalled("org.telegram.messenger", requireContext().getPackageManager()) ||
                isPackageInstalled("org.telegram.plus", requireContext().getPackageManager()) ||
                isPackageInstalled("org.thunderdog.challegram", requireContext().getPackageManager())) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.setData(Uri.parse("https://t.me/artificially_busy"));
            startActivity(intent);
            params.putLong(FirebaseAnalytics.Param.SCORE, 1);
        } else {
            Toast.makeText(requireContext(), R.string.message_telegram_app_not_installed, Toast.LENGTH_SHORT).show();
            params.putLong(FirebaseAnalytics.Param.SCORE, 0);
        }

        AndroidFirebaseAnalytics.logEvent("telegram_click", params);
    }

    private boolean isPackageInstalled(String packagename, PackageManager packageManager) {
        try {
            packageManager.getPackageInfo(packagename, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
