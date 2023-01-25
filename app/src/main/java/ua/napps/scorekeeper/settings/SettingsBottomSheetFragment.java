package ua.napps.scorekeeper.settings;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bitvale.switcher.SwitcherX;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.jetbrains.annotations.NotNull;

import ua.napps.scorekeeper.R;
import ua.napps.scorekeeper.utils.Utilities;
import ua.napps.scorekeeper.utils.ViewUtil;

public class SettingsBottomSheetFragment extends BottomSheetDialogFragment implements View.OnClickListener {

    private TextView btn_c_1, btn_c_2, btn_c_3, btn_c_4;
    private SwitcherX keepScreenOn;
    private SwitcherX darkTheme;
    private SwitcherX vibrate;
    private SwitcherX pressLogic;

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @org.jetbrains.annotations.Nullable ViewGroup container, @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View contentView = inflater.inflate(R.layout.fragment_settings_sheet, container, false);
        keepScreenOn = contentView.findViewById(R.id.sw_keep_screen_on);
        darkTheme = contentView.findViewById(R.id.sw_dark_theme);
        vibrate = contentView.findViewById(R.id.sw_vibrate);
        pressLogic = contentView.findViewById(R.id.sw_swap_press);
        btn_c_1 = contentView.findViewById(R.id.btn_1_text);
        btn_c_2 = contentView.findViewById(R.id.btn_2_text);
        btn_c_3 = contentView.findViewById(R.id.btn_3_text);
        btn_c_4 = contentView.findViewById(R.id.btn_4_text);

        contentView.findViewById(R.id.settings_keep_screen_on).setOnClickListener(this);
        contentView.findViewById(R.id.settings_dark_theme).setOnClickListener(this);
        contentView.findViewById(R.id.settings_swap_press).setOnClickListener(this);
        contentView.findViewById(R.id.settings_vibrate).setOnClickListener(this);
        contentView.findViewById(R.id.btn_close).setOnClickListener(this);

        keepScreenOn.setChecked(LocalSettings.isKeepScreenOnEnabled(), false);
        keepScreenOn.setClickable(false);
        darkTheme.setChecked(!LocalSettings.isLightTheme(), false);
        darkTheme.setClickable(false);
        vibrate.setChecked(LocalSettings.isCountersVibrate(), false);
        vibrate.setClickable(false);
        pressLogic.setChecked(LocalSettings.isSwapPressLogicEnabled(), false);
        pressLogic.setClickable(false);

        btn_c_1.setOnClickListener(this);
        btn_c_2.setOnClickListener(this);
        btn_c_3.setOnClickListener(this);
        btn_c_4.setOnClickListener(this);

        btn_c_1.setText(String.valueOf(LocalSettings.getCustomCounter(1)));
        btn_c_2.setText(String.valueOf(LocalSettings.getCustomCounter(2)));
        btn_c_3.setText(String.valueOf(LocalSettings.getCustomCounter(3)));
        btn_c_4.setText(String.valueOf(LocalSettings.getCustomCounter(4)));

        if (Utilities.hasQ()) {
            contentView.findViewById(R.id.settings_dark_theme).setVisibility(View.GONE);
        }
        return contentView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.settings_keep_screen_on:
                boolean newStateKeepScreenOn = !keepScreenOn.isChecked();
                LocalSettings.saveKeepScreenOn(newStateKeepScreenOn);
                keepScreenOn.setChecked(newStateKeepScreenOn, true);
                break;
            case R.id.settings_dark_theme:
                boolean newStateDarkTheme = !darkTheme.isChecked();
                LocalSettings.saveDarkTheme(newStateDarkTheme);
                darkTheme.setChecked(newStateDarkTheme, true);
                break;

            case R.id.settings_vibrate:
                boolean newStateVibrate = !vibrate.isChecked();
                LocalSettings.saveCountersVibrate(newStateVibrate);
                vibrate.setChecked(newStateVibrate, true);
                if (newStateVibrate) {
                    ViewUtil.shakeView(v, 2, 0);
                }
                break;

            case R.id.btn_1_text:
                openCustomCounterDialog(1, ((TextView) v).getText());
                break;
            case R.id.btn_2_text:
                openCustomCounterDialog(2, ((TextView) v).getText());
                break;
            case R.id.btn_3_text:
                openCustomCounterDialog(3, ((TextView) v).getText());
                break;
            case R.id.btn_4_text:
                openCustomCounterDialog(4, ((TextView) v).getText());
                break;
            case R.id.settings_swap_press:
                boolean newStateSwapPress = !pressLogic.isChecked();
                LocalSettings.saveSwapPressLogic(newStateSwapPress);
                pressLogic.setChecked(newStateSwapPress, true);
                break;

            case R.id.btn_close:
                dismiss();
                break;
        }

    }

    private void openCustomCounterDialog(final int id, CharSequence oldValue) {
        Typeface medium = getResources().getFont(R.font.ptm700);
        Typeface regular = getResources().getFont(R.font.icm400);

        final MaterialDialog md = new MaterialDialog.Builder(requireActivity())
                .title(R.string.settings_counter_title)
                .inputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD)
                .positiveText(R.string.common_set)
                .contentColorRes(R.color.textColorPrimary)
                .buttonRippleColorRes(R.color.btn_text_btn_bg_color_selector)
                .widgetColorRes(R.color.colorSecondary)
                .positiveColorRes(R.color.colorSecondary)
                .alwaysCallInputCallback()
                .typeface(medium, regular)
                .input(oldValue, null, false, (dialog, input) -> {
                })
                .showListener(dialogInterface -> {
                    EditText inputEditText = ((MaterialDialog) dialogInterface).getInputEditText();
                    if (inputEditText != null) {
                        inputEditText.requestFocus();
                        inputEditText.setTransformationMethod(null);
                        inputEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
                        inputEditText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32);
                    }
                })
                .onPositive((dialog, which) -> {
                    EditText editText = dialog.getInputEditText();
                    if (editText != null) {
                        String value = editText.getText().toString();
                        Integer parseInt = Utilities.parseInt(value, 0);
                        if (parseInt <= 999 && parseInt > 1) {
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

    private void setCustomCounter(int id, int value) {
        LocalSettings.saveCustomCounter(id, value);
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
        }
    }

}