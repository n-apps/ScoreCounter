package ua.napps.scorekeeper.dices;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.SeekBar;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.gregacucnik.EditableSeekBar;
import ua.com.napps.scorekeeper.R;

import static ua.napps.scorekeeper.dices.Dice.getDice;

/**
 * Created by novo on 2016-01-02.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class EditDiceFragment extends DialogFragment {

    @Bind(R.id.dice_number)
    EditableSeekBar mDiceAmount;
    @Bind(R.id.dice_min_edge)
    EditableSeekBar mDiceMinEdge;
    @Bind(R.id.dice_max_edge)
    EditableSeekBar mDiceMaxEdge;
    @Bind(R.id.dice_total_bonus)
    EditableSeekBar mDiceTotalBonus;

    private DiceUpdateListener mListener;

    public EditDiceFragment() {
    }

    public static EditDiceFragment newInstance() {
        return new EditDiceFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mListener = (DiceUpdateListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement DiceUpdateListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final View view = View.inflate(getContext(), R.layout.dice_dialog, null);
        ButterKnife.bind(this, view);

        final Dice dice = getDice();
        mDiceAmount.setValue(dice.getDiceNumber());
        mDiceMinEdge.setValue(dice.getMinSide());
        mDiceMaxEdge.setValue(dice.getMaxSide());
        mDiceTotalBonus.setValue(dice.getTotalBonus());
        mDiceMaxEdge.setOnEditableSeekBarChangeListener(new EditableSeekBar.OnEditableSeekBarChangeListener() {
            @Override
            public void onEditableSeekBarProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onEnteredValueTooHigh() {

            }

            @Override
            public void onEnteredValueTooLow() {

            }

            @Override
            public void onEditableSeekBarValueChanged(int value) {

                mDiceMinEdge.setMaxValue(value - 1);
            }
        });
        mDiceMinEdge.setOnEditableSeekBarChangeListener(new EditableSeekBar.OnEditableSeekBarChangeListener() {
            @Override
            public void onEditableSeekBarProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onEnteredValueTooHigh() {

            }

            @Override
            public void onEnteredValueTooLow() {

            }

            @Override
            public void onEditableSeekBarValueChanged(int value) {
                mDiceMaxEdge.setMinValue(value + 1);
            }
        });
        mDiceAmount.setOnEditableSeekBarChangeListener(new EditableSeekBar.OnEditableSeekBarChangeListener() {
            @Override
            public void onEditableSeekBarProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onEnteredValueTooHigh() {
                mDiceAmount.setValue(1);
            }

            @Override
            public void onEnteredValueTooLow() {
                mDiceAmount.setValue(1);
            }

            @Override
            public void onEditableSeekBarValueChanged(int value) {

            }
        });

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());

        alertDialogBuilder.setView(view);
        alertDialogBuilder.setPositiveButton(getContext().getString((R.string.button_positive)), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                updateDice();
                mListener.onDiceUpdate();
            }
        });
        alertDialogBuilder.setNegativeButton(getContext().getString(R.string.button_negative), null);
        AlertDialog dialog = alertDialogBuilder.create();
        dialog.show();

        return dialog;
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        if (manager.findFragmentByTag(tag) == null) {
            super.show(manager, tag);
        }
    }

    private void updateDice() {
        getDice().setDiceNumber(mDiceAmount.getValue());
        getDice().setMinSide(mDiceMinEdge.getValue());
        getDice().setMaxSide(mDiceMaxEdge.getValue());
        getDice().setTotalBonus(mDiceTotalBonus.getValue());
    }

    public interface DiceUpdateListener {
        void onDiceUpdate();
    }
}
