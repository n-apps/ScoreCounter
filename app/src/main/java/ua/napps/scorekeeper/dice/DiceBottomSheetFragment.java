package ua.napps.scorekeeper.dice;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bitvale.switcher.SwitcherX;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButtonToggleGroup;

import org.jetbrains.annotations.NotNull;

import ua.napps.scorekeeper.R;
import ua.napps.scorekeeper.settings.LocalSettings;
import ua.napps.scorekeeper.utils.Utilities;
import ua.napps.scorekeeper.utils.ViewUtil;

public class DiceBottomSheetFragment extends BottomSheetDialogFragment implements View.OnClickListener {

    private DialogInterface.OnDismissListener onDismissListener;
    private SwitcherX shakeToRoll;
    private SwitcherX animateRoll;
    private SwitcherX soundRoll;

    private Vibrator vibrator;
    private boolean isCountersVibrate;

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @org.jetbrains.annotations.Nullable ViewGroup container, @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View contentView = inflater.inflate(R.layout.fragment_dice_sheet, container, false);
        shakeToRoll = contentView.findViewById(R.id.sw_shake_to_roll);
        animateRoll = contentView.findViewById(R.id.sw_animation);
        animateRoll.setChecked(LocalSettings.isDiceAnimated(), false);
        animateRoll.setClickable(false);
        shakeToRoll.setChecked(LocalSettings.isShakeToRollEnabled(), false);
        shakeToRoll.setClickable(false);

        soundRoll = contentView.findViewById(R.id.sw_sound);
        soundRoll.setChecked(LocalSettings.isSoundRollEnabled(), false);
        soundRoll.setClickable(false);

        contentView.findViewById(R.id.settings_sound).setOnClickListener(this);
        contentView.findViewById(R.id.settings_shake).setOnClickListener(this);
        contentView.findViewById(R.id.settings_animate).setOnClickListener(this);

        MaterialButtonToggleGroup diceSidesGroup1 = contentView.findViewById(R.id.dice_sides_group_1);
        MaterialButtonToggleGroup diceSidesGroup2 = contentView.findViewById(R.id.dice_sides_group_2);
        MaterialButtonToggleGroup diceCountGroup = contentView.findViewById(R.id.dice_count_group);

        int diceCount = LocalSettings.getDiceCount();
        int diceMaxSide = LocalSettings.getDiceMaxSide();
        isCountersVibrate = LocalSettings.isCountersVibrate();

        switch (diceMaxSide) {
            case 4:
                diceSidesGroup1.check(R.id.btn_1);
                break;
            case 6:
                diceSidesGroup1.check(R.id.btn_2);
                break;
            case 8:
                diceSidesGroup1.check(R.id.btn_3);
                break;
            case 10:
                diceSidesGroup1.check(R.id.btn_4);
                break;
            case 12:
                diceSidesGroup2.check(R.id.btn_5);
                break;
            case 20:
                diceSidesGroup2.check(R.id.btn_6);
                break;
            default:
                diceSidesGroup2.check(R.id.btn_7);
                break;
        }

        switch (diceCount) {
            case 1:
                diceCountGroup.check(R.id.btn_x1);
                break;
            case 2:
                diceCountGroup.check(R.id.btn_x2);
                break;
            case 4:
                diceCountGroup.check(R.id.btn_x3);
                break;
            default:
                diceCountGroup.check(R.id.btn_x4);
                break;
        }

        vibrator = (Vibrator) requireActivity().getSystemService(Context.VIBRATOR_SERVICE);
        diceSidesGroup1.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                validateDiceToggle(group, checkedId, diceMaxSide);
                diceSidesGroup2.clearChecked();
            }
        });
        diceSidesGroup2.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                validateDiceToggle(group, checkedId, diceMaxSide);
                diceSidesGroup1.clearChecked();
            }
        });
        diceCountGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                switch (checkedId) {
                    case R.id.btn_x1:
                        validateAndStoreDiceCount(1);
                        break;
                    case R.id.btn_x2:
                        validateAndStoreDiceCount(2);
                        break;
                    case R.id.btn_x3:
                        validateAndStoreDiceCount(4);
                        break;
                    case R.id.btn_x4:
                        Typeface mono = getResources().getFont(R.font.mono);
                        Typeface regular = getResources().getFont(R.font.o400);

                        final MaterialDialog md = new MaterialDialog.Builder(requireActivity())
                                .title(R.string.dice_sides)
                                .inputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED)
                                .positiveText(R.string.common_set)
                                .contentColorRes(R.color.textColorPrimary)
                                .buttonRippleColorRes(R.color.rippleColor)
                                .widgetColorRes(R.color.colorSecondary)
                                .positiveColorRes(R.color.colorSecondary)
                                .alwaysCallInputCallback()
                                .typeface(regular, mono)
                                .showListener(dialogInterface -> {
                                    TextView titleTextView = ((MaterialDialog) dialogInterface).getContentView();
                                    if (titleTextView != null) {
                                        titleTextView.setLines(1);
                                        titleTextView.setEllipsize(TextUtils.TruncateAt.END);
                                        titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
                                    }
                                    EditText inputEditText = ((MaterialDialog) dialogInterface).getInputEditText();
                                    if (inputEditText != null) {
                                        inputEditText.requestFocus();
                                        inputEditText.setTransformationMethod(null);
                                        inputEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
                                        inputEditText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 48);
                                    }
                                })
                                .input("1-99", null, false,
                                        (dialog, input) -> {
                                            int parseInt = Utilities.parseInt(input.toString(), diceCount);
                                            dialog.getActionButton(DialogAction.POSITIVE).setEnabled(parseInt <= 100 && parseInt > 1);
                                        })
                                .onPositive((dialog, which) -> {
                                    EditText editText = dialog.getInputEditText();
                                    if (editText != null) {
                                        Integer side = Utilities.parseInt(editText.getText().toString(), diceCount);
                                        if (side > 0) {
                                            validateAndStoreDiceCount(side);
                                            ((Button) group.findViewById(checkedId)).setText("" + side);
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
                        break;
                }
            } else if (-1 == group.getCheckedButtonId()) {
                group.check(R.id.btn_x4);
            }
        });
        return contentView;
    }

    private void validateDiceToggle(MaterialButtonToggleGroup group, int checkedId, int diceMaxSide) {
        switch (checkedId) {
            case R.id.btn_1:
                validateAndStoreDiceSide(4);
                break;
            case R.id.btn_2:
                validateAndStoreDiceSide(6);
                break;
            case R.id.btn_3:
                validateAndStoreDiceSide(8);
                break;
            case R.id.btn_4:
                validateAndStoreDiceSide(10);
                break;
            case R.id.btn_5:
                validateAndStoreDiceSide(12);
                break;
            case R.id.btn_6:
                validateAndStoreDiceSide(20);
                break;
            case R.id.btn_7:
                Typeface mono = DiceBottomSheetFragment.this.getResources().getFont(R.font.mono);
                Typeface regular = DiceBottomSheetFragment.this.getResources().getFont(R.font.o400);

                final MaterialDialog md = new MaterialDialog.Builder(DiceBottomSheetFragment.this.requireActivity())
                        .title(R.string.dice_sides)
                        .inputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED)
                        .positiveText(R.string.common_set)
                        .contentColorRes(R.color.textColorPrimary)
                        .buttonRippleColorRes(R.color.rippleColor)
                        .widgetColorRes(R.color.colorPrimary)
                        .positiveColorRes(R.color.colorPrimary)
                        .alwaysCallInputCallback()
                        .typeface(regular, mono)
                        .showListener(dialogInterface -> {
                            TextView titleTextView = ((MaterialDialog) dialogInterface).getContentView();
                            if (titleTextView != null) {
                                titleTextView.setLines(1);
                                titleTextView.setEllipsize(TextUtils.TruncateAt.END);
                                titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
                            }
                            EditText inputEditText = ((MaterialDialog) dialogInterface).getInputEditText();
                            if (inputEditText != null) {
                                inputEditText.requestFocus();
                                inputEditText.setTransformationMethod(null);
                                inputEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
                                inputEditText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 48);
                            }
                        })
                        .input("2-99", null, false,
                                (dialog, input) -> {
                                    int parseInt = Utilities.parseInt(input.toString(), diceMaxSide);
                                    dialog.getActionButton(DialogAction.POSITIVE).setEnabled(parseInt < 100 && parseInt > 1);
                                })
                        .onPositive((dialog, which) -> {
                            EditText editText = dialog.getInputEditText();
                            if (editText != null) {
                                Integer side = Utilities.parseInt(editText.getText().toString(), diceMaxSide);
                                if (side > 0) {
                                    DiceBottomSheetFragment.this.validateAndStoreDiceSide(side);
                                }
                                ((Button) group.findViewById(checkedId)).setText("D" + side);
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
                break;
        }
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);

        if (onDismissListener != null) {
            onDismissListener.onDismiss(dialog);
        }
    }

    void setOnDismissListener(DialogInterface.OnDismissListener listener) {
        onDismissListener = listener;
    }

    private void validateAndStoreDiceSide(int diceMaxSide) {
        if (diceMaxSide <= 100) {
            LocalSettings.saveDiceMaxSide(diceMaxSide);
            tryVibrate();
        }
    }

    private void validateAndStoreDiceCount(int diceCount) {
        if (diceCount <= 100) {
            LocalSettings.saveDiceCount(diceCount);
            tryVibrate();
        }
    }

    private void tryVibrate() {
        if (isCountersVibrate) {
            Utilities.vibrate(requireContext(), vibrator);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.settings_sound:
                boolean newStateSound = !soundRoll.isChecked();
                LocalSettings.saveSoundRoll(newStateSound);
                soundRoll.setChecked(newStateSound, true);
                tryVibrate();
                break;
            case R.id.settings_shake:
                boolean newStateShake = !shakeToRoll.isChecked();
                LocalSettings.saveShakeToRoll(newStateShake);
                shakeToRoll.setChecked(newStateShake, true);
                tryVibrate();
                if (newStateShake) {
                    ViewUtil.shakeView(v, 2, 0);
                }
                break;
            case R.id.settings_animate:
                boolean newStateAnimate = !animateRoll.isChecked();
                LocalSettings.saveDiceAnimate(newStateAnimate);
                animateRoll.setChecked(newStateAnimate, true);
                tryVibrate();
                if (newStateAnimate) {
                    ViewUtil.shakeView(v, 2, 0);
                }
                break;
        }
    }
}