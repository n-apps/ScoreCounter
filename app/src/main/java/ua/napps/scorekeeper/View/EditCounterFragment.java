package ua.napps.scorekeeper.View;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;

import butterknife.Bind;
import butterknife.ButterKnife;
import ua.com.napps.scorekeeper.R;
import ua.napps.scorekeeper.Interactors.CurrentSet;
import ua.napps.scorekeeper.Models.Counter;

import static android.content.DialogInterface.BUTTON_NEUTRAL;
import static ua.napps.scorekeeper.Interactors.CurrentSet.getCurrentSet;

/**
 * Created by novo on 2015-12-26.
 */
public class EditCounterFragment extends DialogFragment {
    @Bind(R.id.paintedView)
    View paintedView;
    @Bind(R.id.redSeekBar)
    SeekBar redBar;
    @Bind(R.id.greenSeekBar)
    SeekBar greenBar;
    @Bind(R.id.blueSeekBar)
    SeekBar blueBar;
    @Bind(R.id.caption)
    EditText caption;
    @Bind(R.id.defaultValue)
    EditText defValue;
    @Bind(R.id.minValue)
    EditText minValue;
    @Bind(R.id.maxValue)
    EditText maxValue;
    @Bind(R.id.step)
    EditText step;

    public EditCounterFragment() {
    }

    public static EditCounterFragment newInstance(int position) {
        EditCounterFragment frag = new EditCounterFragment();
        Bundle args = new Bundle();
        args.putInt("position", position);
        frag.setArguments(args);
        return frag;
    }
// TODO: add animation https://gist.github.com/orhanobut/8665372
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        final View view = inflater.inflate(R.layout.counter_dialog, null);
        ButterKnife.bind(this, view);

        Integer position = getArguments().getInt("position");
        final Counter mCounter = CurrentSet.getCurrentSet().getCounter(position);
        final MainActivity activity = (MainActivity) getActivity();

        redBar.setOnSeekBarChangeListener(seekListener);
        greenBar.setOnSeekBarChangeListener(seekListener);
        blueBar.setOnSeekBarChangeListener(seekListener);
        setSeekBarProgress(mCounter.getColor());

        initValues(mCounter);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

        alertDialogBuilder.setView(view);
        alertDialogBuilder.setPositiveButton(getContext().getString((R.string.button_positive)), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                mCounter.setColor(getProgressRGBColor());
                mCounter.setCaption(caption.getText().toString());
                mCounter.setDefValue(getIntValue(defValue));
                mCounter.setMinValue(getIntValue(minValue));
                mCounter.setMaxValue(getIntValue(maxValue));
                mCounter.setStep(getIntValue(step));
                activity.updateView();
            }
        });
        alertDialogBuilder.setNegativeButton(getContext().getString(R.string.button_negative), null);
        alertDialogBuilder.setNeutralButton(getContext().getString(R.string.button_neutral), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getCurrentSet().removeCounter(mCounter);
                activity.updateView();
            }
        });
        AlertDialog dialog = alertDialogBuilder.create();
        dialog.show();

        if (CurrentSet.getCurrentSet().getSize() > 1) {
            dialog.getButton(BUTTON_NEUTRAL).setTextColor(ContextCompat.getColor(getContext(), R.color.accentColor));
        } else {
            dialog.getButton(BUTTON_NEUTRAL).setVisibility(View.GONE);
        }
        return dialog;
    }

    private void initValues(Counter counter) {
        caption.append(counter.getCaption());
        defValue.append("" + counter.getDefValue());
        minValue.append("" + counter.getMinValue());
        maxValue.append("" + counter.getMaxValue());
        step.append("" + counter.getStep());
    }

    private void setSeekBarProgress(int color) {
        redBar.setProgress(Color.red(color));
        greenBar.setProgress(Color.green(color));
        blueBar.setProgress(Color.blue(color));
        paintedView.setBackgroundColor(color);
    }

    private int getIntValue(EditText field) {
        String text = field.getText().toString();
        return Integer.parseInt(text.isEmpty() ? "0" : text);
    }

    private int getProgressRGBColor() {
        return Color.rgb(redBar.getProgress(), greenBar.getProgress(), blueBar.getProgress());
    }

    private final SeekBar.OnSeekBarChangeListener seekListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            paintedView.setBackgroundColor(getProgressRGBColor());
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    };
}
