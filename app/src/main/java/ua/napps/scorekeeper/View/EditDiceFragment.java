package ua.napps.scorekeeper.View;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.widget.SeekBar;
import com.gregacucnik.EditableSeekBar;
import ua.com.napps.scorekeeper.R;
import ua.com.napps.scorekeeper.databinding.DiceDialogBinding;
import ua.napps.scorekeeper.Models.Dice;

import static ua.napps.scorekeeper.Models.Dice.getDice;

/**
 * Created by novo on 2016-01-02.
 */
@SuppressWarnings({ "WeakerAccess", "unused" }) public class EditDiceFragment
    extends DialogFragment {

  EditableSeekBar diceNumber;
  EditableSeekBar diceMinEdge;
  EditableSeekBar diceMaxEdge;
  EditableSeekBar diceTotalBonus;

  private DiceUpdateListener listener;

  public EditDiceFragment() {
  }

  public static EditDiceFragment newInstance() {
    return new EditDiceFragment();
  }

  @Override public void onAttach(Context context) {
    super.onAttach(context);

    try {
      listener = (DiceUpdateListener) context;
    } catch (ClassCastException e) {
      throw new ClassCastException(context.toString() + " must implement DiceUpdateListener");
    }
  }

  @NonNull @Override public Dialog onCreateDialog(Bundle savedInstanceState) {
    DiceDialogBinding binding = DataBindingUtil.inflate(getActivity().getLayoutInflater(), R.layout.dice_dialog, null, false);

    final Dice dice = getDice();

    diceNumber = binding.diceNumber;
    diceMinEdge = binding.diceMinEdge;
    diceMaxEdge = binding.diceMaxEdge;
    diceTotalBonus = binding.diceTotalBonus;

    diceNumber.setValue(dice.getDiceNumber());
    diceMinEdge.setValue(dice.getMinSide());
    diceMaxEdge.setValue(dice.getMaxSide());
    diceTotalBonus.setValue(dice.getTotalBonus());

    diceMaxEdge.setOnEditableSeekBarChangeListener(
        new EditableSeekBar.OnEditableSeekBarChangeListener() {
          @Override public void onEditableSeekBarProgressChanged(SeekBar seekBar, int progress,
              boolean fromUser) {

          }

          @Override public void onStartTrackingTouch(SeekBar seekBar) {

          }

          @Override public void onStopTrackingTouch(SeekBar seekBar) {

          }

          @Override public void onEnteredValueTooHigh() {

          }

          @Override public void onEnteredValueTooLow() {

          }

          @Override public void onEditableSeekBarValueChanged(int value) {

            diceMinEdge.setMaxValue(value - 1);
          }
        });
    diceMinEdge.setOnEditableSeekBarChangeListener(
        new EditableSeekBar.OnEditableSeekBarChangeListener() {
          @Override public void onEditableSeekBarProgressChanged(SeekBar seekBar, int progress,
              boolean fromUser) {

          }

          @Override public void onStartTrackingTouch(SeekBar seekBar) {

          }

          @Override public void onStopTrackingTouch(SeekBar seekBar) {

          }

          @Override public void onEnteredValueTooHigh() {

          }

          @Override public void onEnteredValueTooLow() {

          }

          @Override public void onEditableSeekBarValueChanged(int value) {
            diceMaxEdge.setMinValue(value + 1);
          }
        });
    diceNumber.setOnEditableSeekBarChangeListener(
        new EditableSeekBar.OnEditableSeekBarChangeListener() {
          @Override public void onEditableSeekBarProgressChanged(SeekBar seekBar, int progress,
              boolean fromUser) {

          }

          @Override public void onStartTrackingTouch(SeekBar seekBar) {

          }

          @Override public void onStopTrackingTouch(SeekBar seekBar) {

          }

          @Override public void onEnteredValueTooHigh() {
            diceNumber.setValue(1);
          }

          @Override public void onEnteredValueTooLow() {
            diceNumber.setValue(1);
          }

          @Override public void onEditableSeekBarValueChanged(int value) {

          }
        });

    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());

    alertDialogBuilder.setView(binding.getRoot());
    alertDialogBuilder.setPositiveButton(getContext().getString((R.string.button_positive)),
        new DialogInterface.OnClickListener() {
          @Override public void onClick(DialogInterface paramDialogInterface, int paramInt) {
            updateDice();
            listener.onDiceUpdate();
          }
        });
    alertDialogBuilder.setNegativeButton(getContext().getString(R.string.button_negative), null);
    AlertDialog dialog = alertDialogBuilder.create();
    dialog.show();

    return dialog;
  }

  @Override public void show(FragmentManager manager, String tag) {
    if (manager.findFragmentByTag(tag) == null) {
      super.show(manager, tag);
    }
  }

  private void updateDice() {
    getDice().setDiceNumber(diceNumber.getValue());
    getDice().setMinSide(diceMinEdge.getValue());
    getDice().setMaxSide(diceMaxEdge.getValue());
    getDice().setTotalBonus(diceTotalBonus.getValue());
  }

  public interface DiceUpdateListener {
    void onDiceUpdate();
  }
}
