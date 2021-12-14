package ua.napps.scorekeeper.counters;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.lifecycle.ViewModelProvider;

import com.afollestad.materialdialogs.util.DialogUtils;
import com.github.naz013.colorslider.ColorSlider;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import ua.napps.scorekeeper.R;
import ua.napps.scorekeeper.counters.colorpicker.ColorPicker;
import ua.napps.scorekeeper.log.LogEntry;
import ua.napps.scorekeeper.log.LogType;
import ua.napps.scorekeeper.settings.LocalSettings;
import ua.napps.scorekeeper.utils.ColorUtil;
import ua.napps.scorekeeper.utils.Singleton;
import ua.napps.scorekeeper.utils.Utilities;
import ua.napps.scorekeeper.utils.ViewUtil;

public class EditCounterActivity extends AppCompatActivity {

    private static final String ARGUMENT_COUNTER_ID = "ARGUMENT_COUNTER_ID";
    private static final String ARGUMENT_COUNTER_COLOR = "ARGUMENT_COUNTER_COLOR";

    private Counter counter;
    private TextInputLayout counterNameLayout;
    private TextInputLayout counterStepLayout;
    private TextInputLayout counterValueLayout;
    private TextInputLayout counterDefaultValueLayout;
    private TextInputEditText counterStepEditText;
    private TextInputEditText counterNameEditText;
    private TextInputEditText counterDefaultValueEditText;
    private TextInputEditText counterValueEditText;
    private Button btnSave;
    private View moreColorsButton;
    private EditCounterViewModel viewModel;
    private String newCounterColor;
    private ColorSlider colorSlider;

    public static void start(Activity activity, Counter counter) {
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity);
        Intent intent = new Intent(activity, EditCounterActivity.class);
        intent.putExtra(ARGUMENT_COUNTER_ID, counter.getId());
        intent.putExtra(ARGUMENT_COUNTER_COLOR, counter.getColor());
        activity.startActivity(intent, options.toBundle());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if (extras != null && !extras.containsKey(ARGUMENT_COUNTER_ID)) {
            throw new UnsupportedOperationException("Activity should be started using the static start method");
        }
        setContentView(R.layout.activity_edit_counter);
        ViewUtil.setNavBarColor(EditCounterActivity.this, LocalSettings.isLightTheme());

        final int id = getIntent().getIntExtra(ARGUMENT_COUNTER_ID, 0);

        initViews();

        boolean isLightTheme = LocalSettings.isLightTheme();
        if (isLightTheme) {
            ViewUtil.setLightStatusBar(this);
        } else {
            ViewUtil.clearLightStatusBar(this);
        }
        ViewUtil.setNavBarColor(this, isLightTheme);

        subscribeToModel(id);

        moreColorsButton.setOnClickListener(view -> {
            ColorPicker colorPicker = new ColorPicker(EditCounterActivity.this);
            colorPicker.setColorButtonSize(72, 72);
            colorPicker.setColumns(3);
            colorPicker.setColors(R.array.bright_palette);
            colorPicker.setOnFastChooseColorListener((position, color) -> applyNewColor(color));
            colorPicker.setDefaultColor(Color.parseColor(counter.getColor()));
            colorPicker.show();
        });

        colorSlider.setListener((position, color) -> applyNewColor(color));

        btnSave.setOnClickListener(v -> validateAndSave());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_delete, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_delete) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.dialog_confirmation_question)
                    .setPositiveButton(R.string.dialog_yes, (dialog, l1) -> {
                        Singleton.getInstance().addLogEntry(new LogEntry(counter, LogType.RMV, 0, counter.getValue()));
                        viewModel.deleteCounter();
                    })
                    .setNegativeButton(R.string.dialog_no, (dialog, l2) -> dialog.dismiss());
            builder.create().show();

        }

        return super.onOptionsItemSelected(item);
    }

    private void initViews() {
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("");

        counterNameEditText = findViewById(R.id.et_counter_name);
        counterStepEditText = findViewById(R.id.et_counter_step);
        counterDefaultValueEditText = findViewById(R.id.et_counter_default_value);
        counterValueEditText = findViewById(R.id.et_counter_value);
        counterNameLayout = findViewById(R.id.til_counter_name);
        counterValueLayout = findViewById(R.id.til_counter_value);
        counterDefaultValueLayout = findViewById(R.id.til_counter_default_value);
        counterStepLayout = findViewById(R.id.til_counter_step);
        btnSave = findViewById(R.id.btn_save);
        colorSlider = findViewById(R.id.color_slider);
        moreColorsButton = findViewById(R.id.btn_more_colors);

        String colorHex = getIntent().getStringExtra(ARGUMENT_COUNTER_COLOR);
        if (colorHex != null) {
            int boxStrokeColor = Color.parseColor(colorHex);
            if (boxStrokeColor != Color.WHITE) {
                setInputsColorStateDefault();
                updateInputsColors(boxStrokeColor);
                updateButtonColors(boxStrokeColor);
            } else {
                updateInputsColors(Color.LTGRAY);
                btnSave.setBackgroundColor(DialogUtils.getColor(this, R.color.colorPrimary));
            }
            counterNameLayout.requestFocus();
        }
    }

    private void updateButtonColors(int boxStrokeColor) {
        if (ColorUtil.isDarkBackground(boxStrokeColor)) {
            btnSave.setTextColor(0xDEFFFFFF);
        } else {
            btnSave.setTextColor(0xDE000000);
        }
        btnSave.setBackgroundColor(boxStrokeColor);
    }

    private void setInputsColorStateDefault() {
        ColorStateList colorStateList = ContextCompat.getColorStateList(this, R.color.box_stroke_selector);
        if (colorStateList != null) {
            counterNameLayout.setBoxStrokeColorStateList(colorStateList);
            counterValueLayout.setBoxStrokeColorStateList(colorStateList);
            counterDefaultValueLayout.setBoxStrokeColorStateList(colorStateList);
            counterStepLayout.setBoxStrokeColorStateList(colorStateList);
        }
    }

    private void updateInputsColors(@ColorInt int fillColor) {
        counterNameLayout.setBoxStrokeColor(fillColor);
        counterValueLayout.setBoxStrokeColor(fillColor);
        counterDefaultValueLayout.setBoxStrokeColor(fillColor);
        counterStepLayout.setBoxStrokeColor(fillColor);
        counterNameLayout.setBoxBackgroundColor(ColorUtils.setAlphaComponent(fillColor, 20));
        counterValueLayout.setBoxBackgroundColor(ColorUtils.setAlphaComponent(fillColor, 20));
        counterDefaultValueLayout.setBoxBackgroundColor(ColorUtils.setAlphaComponent(fillColor, 20));
        counterStepLayout.setBoxBackgroundColor(ColorUtils.setAlphaComponent(fillColor, 20));
    }

    private void applyNewColor(@ColorInt int newColor) {
        if (newColor != Color.WHITE) {
            updateInputsColors(newColor);
            updateButtonColors(newColor);
        } else {
            updateInputsColors(Color.LTGRAY);
            updateButtonColors(Color.LTGRAY);
        }
        newCounterColor = ColorUtil.intColorToString(newColor);
    }

    private void validateAndSave() {
        String newName = counterNameEditText.getText().toString();
        if (!counter.getName().equals(newName)) {
            viewModel.updateName(newName);

            if (newName.equalsIgnoreCase("roman") |
                    newName.equalsIgnoreCase("роман") |
                    newName.equalsIgnoreCase("рома")) {
                Toast.makeText(getApplicationContext(), R.string.easter_wave, Toast.LENGTH_SHORT).show();
            }
        }
        if (!counter.getColor().equals(newCounterColor) && newCounterColor != null) {
            viewModel.updateColor(newCounterColor);
        }
        int counterValue = counter.getValue();
        int newValue = Utilities.parseInt(counterValueEditText.getText().toString(), counterValue);
        if (counterValue != newValue) {
            viewModel.updateValue(newValue);
        }
        int counterDefValue = counter.getDefaultValue();
        int defaultValue = Utilities.parseInt(counterDefaultValueEditText.getText().toString(), counterDefValue);
        if (counterDefValue != defaultValue) {
            viewModel.updateDefaultValue(defaultValue);
        }
        int counterStep = counter.getStep();
        int step = Utilities.parseInt(counterStepEditText.getText().toString(), counterStep);
        if (counterStep != step) {
            viewModel.updateStep(step);
        }

        supportFinishAfterTransition();
    }

    private void subscribeToModel(int id) {
        EditCounterViewModelFactory factory = new EditCounterViewModelFactory(id);
        viewModel = new ViewModelProvider(this, factory).get(EditCounterViewModel.class);
        viewModel.getCounterLiveData().observe(this, c -> {
            if (c != null) {
                counter = c;
                viewModel.setCounter(c);
                counterValueEditText.setText(String.valueOf(c.getValue()));
                counterStepEditText.setText(String.valueOf(c.getStep()));
                counterDefaultValueEditText.setText(String.valueOf(c.getDefaultValue()));
                if (!c.getName().equals(counterNameEditText.getText().toString())) {
                    counterNameEditText.setText(c.getName());
                    counterNameEditText.setSelection(c.getName().length());
                }
                colorSlider.selectColor(Color.parseColor(c.getColor()));
            } else {
                counter = null;
                finish();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
