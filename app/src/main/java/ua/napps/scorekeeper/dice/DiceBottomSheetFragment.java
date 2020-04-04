package ua.napps.scorekeeper.dice;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButtonToggleGroup;

import org.jetbrains.annotations.NotNull;

import ua.napps.scorekeeper.R;
import ua.napps.scorekeeper.settings.LocalSettings;
import ua.napps.scorekeeper.utils.AndroidFirebaseAnalytics;
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
        int diceMaxSide = LocalSettings.getDiceMaxSide();
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
        diceSidesGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                switch (checkedId) {
                    case R.id.btn_1:
                        validateAndStore(6);
                        break;
                    case R.id.btn_2:
                        validateAndStore(8);
                        break;
                    case R.id.btn_3:
                        validateAndStore(20);
                        break;
                    case R.id.btn_4:
                        final MaterialDialog md = new MaterialDialog.Builder(requireActivity())
                                .content(R.string.dialog_custom_dice_title)
                                .inputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED)
                                .positiveText(R.string.common_set)
                                .alwaysCallInputCallback()
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
                                            validateAndStore(side);
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

    @Override
    public void onStart() {
        super.onStart();
        AndroidFirebaseAnalytics.logEvent("DiceBottomSheetScreenAppear");
    }

    private void validateAndStore(int diceMaxSide) {
        if (diceMaxSide <= 100) {
            LocalSettings.saveDiceMaxSide(diceMaxSide);
            if (getDialog() != null) {
                onDismiss(getDialog());
            }
        }
    }
}
