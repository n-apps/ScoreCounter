package ua.napps.scorekeeper.dice;

import android.content.DialogInterface;
import android.os.Bundle;
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
import androidx.appcompat.widget.SwitchCompat;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.util.DialogUtils;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButtonToggleGroup;

import org.jetbrains.annotations.NotNull;

import ua.napps.scorekeeper.R;
import ua.napps.scorekeeper.settings.LocalSettings;
import ua.napps.scorekeeper.utils.Utilities;

public class DiceBottomSheetFragment extends BottomSheetDialogFragment {

    private DialogInterface.OnDismissListener onDismissListener;

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @org.jetbrains.annotations.Nullable ViewGroup container, @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View contentView = inflater.inflate(R.layout.fragment_dice_sheet, container, false);
        SwitchCompat shakeToRoll = contentView.findViewById(R.id.sw_shake_roll);
        shakeToRoll.setOnCheckedChangeListener((b, isChecked) -> LocalSettings.saveShakeToRoll(isChecked));
        shakeToRoll.setChecked(LocalSettings.isShakeToRollEnabled());

        SwitchCompat soundRoll = contentView.findViewById(R.id.sw_sound_roll);
        soundRoll.setOnCheckedChangeListener((b, isChecked) -> LocalSettings.saveSoundRoll(isChecked));
        soundRoll.setChecked(LocalSettings.isSoundRollEnabled());

        MaterialButtonToggleGroup diceSidesGroup = contentView.findViewById(R.id.dice_sides_group);
        MaterialButtonToggleGroup diceCountGroup = contentView.findViewById(R.id.dice_count_group);

        int diceMaxSide = LocalSettings.getDiceMaxSide();
        int diceCount = LocalSettings.getDiceCount();

        switch (diceMaxSide) {
            case 6:
                diceSidesGroup.check(R.id.btn_1);
                break;
            case 8:
                diceSidesGroup.check(R.id.btn_2);
                break;
            case 20:
                diceSidesGroup.check(R.id.btn_3);
                break;
            default:
                diceSidesGroup.check(R.id.btn_4);
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
        diceSidesGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                switch (checkedId) {
                    case R.id.btn_1:
                        validateAndStoreDiceSide(6);
                        break;
                    case R.id.btn_2:
                        validateAndStoreDiceSide(8);
                        break;
                    case R.id.btn_3:
                        validateAndStoreDiceSide(20);
                        break;
                    case R.id.btn_4:
                        final MaterialDialog md = new MaterialDialog.Builder(requireActivity())
                                .content(R.string.dialog_custom_dice_title)
                                .inputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED)
                                .positiveText(R.string.common_set)
                                .contentColor(DialogUtils.getColor(requireContext(), R.color.textColorPrimary))
                                .alwaysCallInputCallback()
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
                                            validateAndStoreDiceSide(side);
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
            } else if (-1 == group.getCheckedButtonId()) {
                group.check(R.id.btn_4);
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
                        final MaterialDialog md = new MaterialDialog.Builder(requireActivity())
                                .content(R.string.dialog_custom_dice_title)
                                .inputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED)
                                .positiveText(R.string.common_set)
                                .alwaysCallInputCallback()
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
                                            ((Button) diceCountGroup.findViewById(R.id.btn_x4)).setText("" + side);
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
        }
    }

    private void validateAndStoreDiceCount(int diceCount) {
        if (diceCount <= 100) {
            LocalSettings.saveDiceCount(diceCount);
        }
    }
}