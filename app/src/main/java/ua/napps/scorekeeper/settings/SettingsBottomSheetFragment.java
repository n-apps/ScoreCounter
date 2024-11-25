package ua.napps.scorekeeper.settings;


import static ua.napps.scorekeeper.settings.LocalSettings.THEME_DARK;
import static ua.napps.scorekeeper.settings.LocalSettings.THEME_LIGHT;
import static ua.napps.scorekeeper.settings.LocalSettings.THEME_SYSTEM;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.Settings;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bitvale.switcher.SwitcherX;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.jetbrains.annotations.NotNull;

import ua.napps.scorekeeper.R;
import ua.napps.scorekeeper.utils.Utilities;
import ua.napps.scorekeeper.utils.ViewUtil;

public class SettingsBottomSheetFragment extends BottomSheetDialogFragment implements View.OnClickListener {

    private TextView btn_c_1, btn_c_2, btn_c_3, btn_c_4, btn_c_5, btn_c_6, btn_c_7;
    private SwitcherX keepScreenOn, vibrate, pressLogic;
    private Vibrator vibrator;
    private boolean isCountersVibrate;

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @org.jetbrains.annotations.Nullable ViewGroup container, @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View contentView = inflater.inflate(R.layout.fragment_settings_sheet, container, false);
        keepScreenOn = contentView.findViewById(R.id.sw_keep_screen_on);
        vibrate = contentView.findViewById(R.id.sw_vibrate);
        pressLogic = contentView.findViewById(R.id.sw_swap_press);

        btn_c_1 = contentView.findViewById(R.id.btn_1_text);
        btn_c_2 = contentView.findViewById(R.id.btn_2_text);
        btn_c_3 = contentView.findViewById(R.id.btn_3_text);
        btn_c_4 = contentView.findViewById(R.id.btn_4_text);
        btn_c_5 = contentView.findViewById(R.id.btn_5_text);
        btn_c_6 = contentView.findViewById(R.id.btn_6_text);
        btn_c_7 = contentView.findViewById(R.id.btn_7_text);

        contentView.findViewById(R.id.settings_keep_screen_on).setOnClickListener(this);
        contentView.findViewById(R.id.tv_app_theme).setOnClickListener(this);
        contentView.findViewById(R.id.settings_swap_press).setOnClickListener(this);
        contentView.findViewById(R.id.settings_vibrate).setOnClickListener(this);
        contentView.findViewById(R.id.btn_close).setOnClickListener(this);

        keepScreenOn.setChecked(LocalSettings.isKeepScreenOnEnabled(), false);
        keepScreenOn.setClickable(false);
        isCountersVibrate = LocalSettings.isCountersVibrate();

        vibrator = (Vibrator) requireActivity().getSystemService(Context.VIBRATOR_SERVICE);
        vibrate.setChecked(isCountersVibrate, false);
        vibrate.setClickable(false);

        pressLogic.setChecked(LocalSettings.isSwapPressLogicEnabled(), false);
        pressLogic.setClickable(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !Utilities.isMiuiRom()) {
            TextView openAppInfo = contentView.findViewById(R.id.tv_open_app_info);
            openAppInfo.setVisibility(View.VISIBLE);
            openAppInfo.setOnClickListener(this);
        }

        contentView.findViewById(R.id.btn_one).setOnClickListener(this);
        contentView.findViewById(R.id.btn_two).setOnClickListener(this);
        contentView.findViewById(R.id.btn_three).setOnClickListener(this);
        contentView.findViewById(R.id.btn_four).setOnClickListener(this);
        contentView.findViewById(R.id.btn_five).setOnClickListener(this);
        contentView.findViewById(R.id.btn_six).setOnClickListener(this);
        contentView.findViewById(R.id.btn_seven).setOnClickListener(this);

        btn_c_1.setText(String.valueOf(LocalSettings.getCustomCounter(1)));
        btn_c_2.setText(String.valueOf(LocalSettings.getCustomCounter(2)));
        btn_c_3.setText(String.valueOf(LocalSettings.getCustomCounter(3)));
        btn_c_4.setText(String.valueOf(LocalSettings.getCustomCounter(4)));
        btn_c_5.setText(String.valueOf(LocalSettings.getCustomCounter(5)));
        btn_c_6.setText(String.valueOf(LocalSettings.getCustomCounter(6)));
        btn_c_7.setText(String.valueOf(LocalSettings.getCustomCounter(7)));

        return contentView;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(dialog1 -> {
            BottomSheetDialog d = (BottomSheetDialog) dialog1;

            FrameLayout bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        return dialog;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.settings_keep_screen_on:
                boolean newStateKeepScreenOn = !keepScreenOn.isChecked();
                LocalSettings.saveKeepScreenOn(newStateKeepScreenOn);
                tryVibrate();
                keepScreenOn.setChecked(newStateKeepScreenOn, true);
                break;
            case R.id.tv_app_theme:
                Typeface regular = getResources().getFont(R.font.o400);
                Typeface medium = getResources().getFont(R.font.o600);

                int theme = LocalSettings.getDefaultTheme();
                final int currentMode;
                switch (theme) {
                    case THEME_LIGHT:
                        currentMode = 1;
                        break;
                    case THEME_DARK:
                        currentMode = 2;
                        break;
                    default:
                        currentMode = 0;
                        break;
                }

                new MaterialDialog.Builder(requireActivity())
                        .title(R.string.settings_appearance)
                        .itemsCallbackSingleChoice(currentMode, (dialog, itemView, which, text) -> {
                            final int newMode;
                            switch (which) {
                                case 1:
                                    newMode = THEME_LIGHT;
                                    break;
                                case 2:
                                    newMode = THEME_DARK;
                                    break;
                                default:
                                    newMode = THEME_SYSTEM;
                                    break;
                            }
                            final int nightMode;
                            switch (newMode) {
                                case THEME_LIGHT:
                                    nightMode = AppCompatDelegate.MODE_NIGHT_NO;
                                    break;
                                case THEME_DARK:
                                    nightMode = AppCompatDelegate.MODE_NIGHT_YES;
                                    break;
                                default:
                                    nightMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                                    break;
                            }
                            AppCompatDelegate.setDefaultNightMode(nightMode);
                            LocalSettings.saveDefaultTheme(newMode);
                            tryVibrate();
                            dismiss();
                            return true;
                        })
                        .items(getResources().getStringArray(R.array.app_theme_options))
                        .typeface(regular, medium)
                        .show();

                break;
            case R.id.settings_vibrate:
                boolean newStateVibrate = !vibrate.isChecked();
                LocalSettings.saveCountersVibrate(newStateVibrate);
                tryVibrate();
                vibrate.setChecked(newStateVibrate, true);
                if (newStateVibrate) {
                    ViewUtil.shakeView(v, 2, 0);
                }
                break;
            case R.id.btn_one:
                openCustomCounterDialog(1, btn_c_1.getText());
                break;
            case R.id.btn_two:
                openCustomCounterDialog(2, btn_c_2.getText());
                break;
            case R.id.btn_three:
                openCustomCounterDialog(3, btn_c_3.getText());
                break;
            case R.id.btn_four:
                openCustomCounterDialog(4, btn_c_4.getText());
                break;
            case R.id.btn_five:
                openCustomCounterDialog(5, btn_c_5.getText());
                break;
            case R.id.btn_six:
                openCustomCounterDialog(6, btn_c_6.getText());
                break;
            case R.id.btn_seven:
                openCustomCounterDialog(7, btn_c_7.getText());
                break;
            case R.id.settings_swap_press:
                boolean newStateSwapPress = !pressLogic.isChecked();
                LocalSettings.saveSwapPressLogic(newStateSwapPress);
                tryVibrate();
                pressLogic.setChecked(newStateSwapPress, true);
                break;
            case R.id.btn_close:
                dismiss();
                break;
            case R.id.tv_open_app_info:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Intent intent = new Intent(Settings.ACTION_APP_LOCALE_SETTINGS);
                    intent.setData(Uri.fromParts("package", requireActivity().getPackageName(), null));
                    requireActivity().startActivity(intent);
                }
                break;
        }
    }

    private void openCustomCounterDialog(@IntRange(from = 1, to = 7) final int id, CharSequence oldValue) {
        Typeface mono = getResources().getFont(R.font.mono);
        Typeface regular = getResources().getFont(R.font.o400);

        String title = getString(R.string.settings_counter_title) + ": " + id;

        final MaterialDialog md = new MaterialDialog.Builder(requireActivity())
                .title(title)
                .inputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED)
                .positiveText(R.string.common_set)
                .contentColorRes(R.color.textColorPrimary)
                .buttonRippleColorRes(R.color.rippleColor)
                .widgetColorRes(R.color.colorPrimary)
                .positiveColorRes(R.color.colorPrimary)
                .alwaysCallInputCallback()
                .typeface(regular, mono)
                .input(oldValue, null, false, (dialog, input) -> {
                })

                .showListener(dialogInterface -> {
                    EditText inputEditText = ((MaterialDialog) dialogInterface).getInputEditText();
                    if (inputEditText != null) {
                        inputEditText.requestFocus();
                        inputEditText.setTransformationMethod(null);
                        inputEditText.setGravity(Gravity.CENTER);
                        inputEditText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 48);
                    }
                })
                .onPositive((dialog, which) -> {
                    EditText editText = dialog.getInputEditText();
                    if (editText != null) {
                        String value = editText.getText().toString();
                        Integer parseInt = Utilities.parseInt(value, 0);
                        if (parseInt > 0) {
                            setCustomCounter(id, parseInt);
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
        md.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    private void setCustomCounter(@IntRange(from = 1, to = 7) int id, int value) {
        LocalSettings.saveCustomCounter(id, value);
        tryVibrate();
        switch (id) {
            case 1:
                btn_c_1.setText(String.valueOf(value));
                ViewUtil.shakeView(btn_c_1, 4, 0);
                break;
            case 2:
                btn_c_2.setText(String.valueOf(value));
                ViewUtil.shakeView(btn_c_2, 4, 0);
                break;
            case 3:
                btn_c_3.setText(String.valueOf(value));
                ViewUtil.shakeView(btn_c_3, 4, 0);
                break;
            case 4:
                btn_c_4.setText(String.valueOf(value));
                ViewUtil.shakeView(btn_c_4, 4, 0);
                break;
            case 5:
                btn_c_5.setText(String.valueOf(value));
                ViewUtil.shakeView(btn_c_5, 4, 0);
                break;
            case 6:
                btn_c_6.setText(String.valueOf(value));
                ViewUtil.shakeView(btn_c_6, 4, 0);
                break;
            case 7:
                btn_c_7.setText(String.valueOf(value));
                ViewUtil.shakeView(btn_c_7, 4, 0);
                break;
        }
    }

    private void tryVibrate() {
        if (isCountersVibrate) {
            Utilities.vibrate(requireContext(), vibrator);
        }
    }

}