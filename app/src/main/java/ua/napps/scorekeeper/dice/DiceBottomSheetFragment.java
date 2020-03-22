package ua.napps.scorekeeper.dice;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.jetbrains.annotations.NotNull;

import ua.napps.scorekeeper.R;
import ua.napps.scorekeeper.settings.LocalSettings;
import ua.napps.scorekeeper.utils.AndroidFirebaseAnalytics;
import ua.napps.scorekeeper.utils.Utilities;

public class DiceBottomSheetFragment extends BottomSheetDialogFragment implements View.OnClickListener {

    private DialogInterface.OnDismissListener onDismissListener;
    private int diceMaxSide;
    private ToggleButton diceSix, diceEight, diceTwenty, diceCustom;

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

        diceMaxSide = LocalSettings.getDiceMaxSide();
        diceSix = contentView.findViewById(R.id.tb_dice_6);
        diceEight = contentView.findViewById(R.id.tb_dice_8);
        diceTwenty = contentView.findViewById(R.id.tb_dice_20);
        diceCustom = contentView.findViewById(R.id.tb_dice_x);

        diceSix.setOnClickListener(this);
        diceEight.setOnClickListener(this);
        diceTwenty.setOnClickListener(this);
        diceCustom.setOnClickListener(this);
        refreshDices(false);
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
        if (getDialog() != null && getDialog().getWindow() != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int oldFlags = getDialog().getWindow().getDecorView().getSystemUiVisibility();
            // Apply the state flags in priority order
            int newFlags = oldFlags;
            if (LocalSettings.isLightTheme()) {
                newFlags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                getDialog().getWindow().setNavigationBarColor(Color.WHITE);
            } else {
                newFlags &= ~(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
                getDialog().getWindow().setNavigationBarColor(ContextCompat.getColor(requireActivity(), R.color.primaryBackground));
            }
            if (newFlags != oldFlags) {
                getDialog().getWindow().getDecorView().setSystemUiVisibility(newFlags);
            }
        }
        AndroidFirebaseAnalytics.logEvent("DiceBottomSheetScreenAppear");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
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
                        .alwaysCallInputCallback()
                        .dismissListener(d -> refreshDices(false))
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
            if (getDialog() != null) {
                onDismiss(getDialog());
            }
            return;
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
}
