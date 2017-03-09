package ua.napps.scorekeeper.counters;

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
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.gregacucnik.EditableSeekBar;
import ua.com.napps.scorekeeper.R;
import ua.napps.scorekeeper.data.CurrentSet;
import ua.napps.scorekeeper.utils.ColorUtil;

import static android.content.DialogInterface.BUTTON_NEUTRAL;
import static ua.napps.scorekeeper.data.CurrentSet.getInstance;

/**
 * Created by novo on 2015-12-26.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class EditCounterFragment extends DialogFragment {
    @Bind(R.id.color_header)
    LinearLayout mColorHeader;
    @Bind(R.id.red_seekbar)
    SeekBar redBar;
    @Bind(R.id.green_seekbar)
    SeekBar greenBar;
    @Bind(R.id.blue_seekbar)
    SeekBar blueBar;
    @Bind(R.id.counter_caption)
    EditText caption;
    @Bind(R.id.counter_value)
    EditableSeekBar mCurrentValue;
    @Bind(R.id.counter_default_value)
    EditableSeekBar defValue;
    @Bind(R.id.counter_step)
    EditableSeekBar step;
    @Bind(R.id.flip_counter)
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
        final View view = View.inflate(getContext(), R.layout.counter_dialog, null);

        ButterKnife.bind(this, view);

        Integer position = getArguments().getInt("position");
        final Counter mCounter = CurrentSet.getInstance().getCounter(position);

        redBar.setOnSeekBarChangeListener(seekListener);
        greenBar.setOnSeekBarChangeListener(seekListener);
        blueBar.setOnSeekBarChangeListener(seekListener);
        setSeekBarProgress(mCounter.getBackgroundColor());

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
                getInstance().removeCounter(mCounter);
                mCallback.onCounterDelete();
            }
        });
        AlertDialog dialog = alertDialogBuilder.create();
        dialog.show();

        if (CurrentSet.getInstance().getSize() > 1) {
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

    private void updateCounter(Counter counter) {
        counter.setBackgroundColor(getProgressRGBColor());
        counter.setName(caption.getText().toString().trim());
        counter.setDefaultValue(defValue.getValue());
        counter.setStep(step.getValue());
        counter.setValue(mCurrentValue.getValue());
        counter.setTextColor(ColorUtil.getContrastColor(getProgressRGBColor()));
        counter.setRotation(mRotationSwitch.isChecked() ? 180 : 0);
    }

    private void initValues(Counter counter) {
        caption.append(counter.getName());
        defValue.setValue(counter.getDefaultValue());
        step.setValue(counter.getStep());
        mCurrentValue.setValue(counter.getValue());
        mRotationSwitch.setChecked(counter.getRotation() == 180);
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
