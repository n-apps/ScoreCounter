package ua.napps.scorekeeper.View;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import com.gregacucnik.EditableSeekBar;

import butterknife.Bind;
import butterknife.ButterKnife;
import ua.com.napps.scorekeeper.R;
import ua.napps.scorekeeper.Interactors.CurrentSet;
import ua.napps.scorekeeper.Models.Counter;
import ua.napps.scorekeeper.Utils.ColorUtil;

import static android.content.DialogInterface.BUTTON_NEUTRAL;
import static ua.napps.scorekeeper.Interactors.CurrentSet.getCurrentSet;

/**
 * Created by novo on 2015-12-26.
 */
public class EditCounterFragment extends DialogFragment {
    @Bind(R.id.color_header)
    LinearLayout mColorHeader;
    @Bind(R.id.redSeekBar)
    SeekBar redBar;
    @Bind(R.id.greenSeekBar)
    SeekBar greenBar;
    @Bind(R.id.blueSeekBar)
    SeekBar blueBar;
    @Bind(R.id.counter_caption)
    EditText caption;
    @Bind(R.id.currentValue)
    EditableSeekBar mCurrentValue;
    @Bind(R.id.defaultValue)
    EditableSeekBar defValue;
    @Bind(R.id.step)
    EditableSeekBar step;
    @Bind(R.id.flipCounterSwitch)
    SwitchCompat mRotationSwitch;

    private CounterUpdateListener mCallback;

    public EditCounterFragment() {
    }

    public static EditCounterFragment newInstance(int position) {
        EditCounterFragment frag = new EditCounterFragment();
        Bundle args = new Bundle();
        args.putInt("position", position);
        frag.setArguments(args);
        return frag;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        final View view = inflater.inflate(R.layout.counter_dialog, null);
        ButterKnife.bind(this, view);

        Integer position = getArguments().getInt("position");
        final Counter mCounter = CurrentSet.getCurrentSet().getCounter(position);

        redBar.setOnSeekBarChangeListener(seekListener);
        greenBar.setOnSeekBarChangeListener(seekListener);
        blueBar.setOnSeekBarChangeListener(seekListener);
        setSeekBarProgress(mCounter.getColor());

        initValues(mCounter);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());

        alertDialogBuilder.setView(view);
        alertDialogBuilder.setPositiveButton(getContext().getString((R.string.button_positive)), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                updateCounter(mCounter);
                mCallback.onCounterUpdate();
            }
        });
        alertDialogBuilder.setNegativeButton(getContext().getString(R.string.button_negative), null);
        alertDialogBuilder.setNeutralButton(getContext().getString(R.string.button_neutral), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getCurrentSet().removeCounter(mCounter);
                mCallback.onCounterDelete();
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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (CounterUpdateListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling Fragment must implement CounterUpdateListener");
        }
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        if (manager.findFragmentByTag(tag) == null) {
            super.show(manager, tag);
        }
    }

    private void updateCounter(Counter mCounter) {
        mCounter.setColor(getProgressRGBColor());
        mCounter.setCaption(caption.getText().toString().trim());
        mCounter.setDefValue(defValue.getValue());
        mCounter.setStep(step.getValue());
        mCounter.setValue(mCurrentValue.getValue());
        mCounter.setTextColor(ColorUtil.getContrastColor(getProgressRGBColor()));
        mCounter.setRotationValue(mRotationSwitch.isChecked() ? 180 : 0);
    }

    private void initValues(Counter counter) {
        caption.append(counter.getCaption());
        defValue.setValue(counter.getDefValue());
        step.setValue(counter.getStep());
        mCurrentValue.setValue(counter.getValue());
        mRotationSwitch.setChecked(counter.getRotationValue() == 180);
    }

    private void setSeekBarProgress(int color) {
        redBar.setProgress(Color.red(color));
        greenBar.setProgress(Color.green(color));
        blueBar.setProgress(Color.blue(color));
        mColorHeader.setBackgroundColor(color);
        caption.setTextColor(ColorUtil.getContrastColor(getProgressRGBColor()));
    }

    private int getProgressRGBColor() {
        return Color.rgb(redBar.getProgress(), greenBar.getProgress(), blueBar.getProgress());
    }

    private final SeekBar.OnSeekBarChangeListener seekListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            mColorHeader.setBackgroundColor(getProgressRGBColor());
            caption.setTextColor(ColorUtil.getContrastColor(getProgressRGBColor()));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    };

    public interface CounterUpdateListener {
        void onCounterUpdate();

        void onCounterDelete();
    }
}
