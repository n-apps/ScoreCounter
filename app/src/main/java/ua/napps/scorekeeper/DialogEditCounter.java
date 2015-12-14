package ua.napps.scorekeeper;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;

import butterknife.Bind;
import butterknife.ButterKnife;
import ua.com.napps.scorekeeper.R;
import ua.napps.scorekeeper.Models.Counter;
import ua.napps.scorekeeper.View.MainActivity;

import static android.content.DialogInterface.BUTTON_NEUTRAL;

public class DialogEditCounter extends AlertDialog.Builder {
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

    public DialogEditCounter(final MainActivity context, final Counter counter, boolean isNeutralButtonEnabled) {
        super(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        @SuppressLint("InflateParams") final View view = inflater.inflate(R.layout.counter_dialog, null);
        setView(view);
        ButterKnife.bind(this, view);
        initDialogButtons(counter, context, context.getString(R.string.button_negative), context.getString(R.string.button_positive), context.getString(R.string.button_neutral));
        redBar.setOnSeekBarChangeListener(seekListener);
        greenBar.setOnSeekBarChangeListener(seekListener);
        blueBar.setOnSeekBarChangeListener(seekListener);
        setSeekBarProgress(counter.getColor());
        caption.append(counter.getCaption());
        initValues(counter);
        AlertDialog dialog = create();
        dialog.show();
        if (isNeutralButtonEnabled) {
            dialog.getButton(BUTTON_NEUTRAL).setTextColor(ContextCompat.getColor(context, R.color.accentColor));
        } else {
            dialog.getButton(BUTTON_NEUTRAL).setVisibility(View.GONE);
        }
    }

    private void setSeekBarProgress(int color) {
        redBar.setProgress(Color.red(color));
        greenBar.setProgress(Color.green(color));
        blueBar.setProgress(Color.blue(color));
        paintedView.setBackgroundColor(color);
    }

    private void initDialogButtons(final Counter counter, final MainActivity mainActivity, String negative, String positive, String neutral) {
        setPositiveButton(positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                applyChanges(counter);
            }
        });
        setNegativeButton(negative, null);
        setNeutralButton(neutral, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mainActivity.onDialogClickDeleteCounter(counter);
            }
        });
    }

    private void applyChanges(Counter counter) {
        counter.setColor(getProgressRGBColor());
        counter.setCaption(caption.getText().toString());
        counter.setDefValue(getIntValue(defValue));
        counter.setMinValue(getIntValue(minValue));
        counter.setMaxValue(getIntValue(maxValue));
        counter.setStep(getIntValue(this.step));
    }

    private int getIntValue(EditText field) {
        String text = field.getText().toString();
        return Integer.parseInt(text.isEmpty() ? "0" : text);
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

    private void initValues(Counter counter) {
        defValue.append("" + counter.getDefValue());
        minValue.append("" + counter.getMinValue());
        maxValue.append("" + counter.getMaxValue());
        step.append("" + counter.getStep());
    }

    private int getProgressRGBColor() {
        return Color.rgb(redBar.getProgress(), greenBar.getProgress(), blueBar.getProgress());
    }
}
