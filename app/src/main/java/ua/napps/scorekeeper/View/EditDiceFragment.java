package ua.napps.scorekeeper.View;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;

import com.gregacucnik.EditableSeekBar;

import butterknife.Bind;
import butterknife.ButterKnife;
import ua.com.napps.scorekeeper.R;
import ua.napps.scorekeeper.Interactors.Dice;

import static ua.napps.scorekeeper.Interactors.Dice.getDiceInstance;

/**
 * Created by novo on 2016-01-02.
 */
public class EditDiceFragment extends DialogFragment {

    @Bind(R.id.diceAmount)
    EditableSeekBar mDiceAmount;
    @Bind(R.id.diceMinEdge)
    EditableSeekBar mDiceMinEdge;
    @Bind(R.id.diceMaxEdge)
    EditableSeekBar mDiceMaxEdge;
    @Bind(R.id.diceTotalBonus)
    EditableSeekBar mDiceTotalBonus;

    private DiceUpdateListener mListener;

    public EditDiceFragment() {
    }

    public static EditDiceFragment newInstance() {
        EditDiceFragment editDiceFragment = new EditDiceFragment();
        return editDiceFragment;
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

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        final View view = inflater.inflate(R.layout.dice_dialog, null);
        ButterKnife.bind(this, view);

        final Dice dice = getDiceInstance();
        mDiceAmount.setValue(dice.getAmount());
        mDiceMinEdge.setValue(dice.getMinEdge());
        mDiceMaxEdge.setValue(dice.getMaxEdge());
        mDiceTotalBonus.setValue(dice.getBonus());
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

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

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
        getDiceInstance().setAmount(mDiceAmount.getValue());
        getDiceInstance().setMinEdge(mDiceMinEdge.getValue());
        getDiceInstance().setMaxEdge(mDiceMaxEdge.getValue());
        getDiceInstance().setBonus(mDiceTotalBonus.getValue());
    }

    public interface DiceUpdateListener {
        void onDiceUpdate();
    }
}
