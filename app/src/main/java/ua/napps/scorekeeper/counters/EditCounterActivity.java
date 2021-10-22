package ua.napps.scorekeeper.counters;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.util.DialogUtils;
import com.github.naz013.colorslider.ColorSlider;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import petrov.kristiyan.colorpicker.ColorPicker;
import ua.napps.scorekeeper.R;
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
    private TextInputEditText counterStep;
    private TextInputLayout counterNameLayout;
    private TextInputEditText counterName;
    private TextInputEditText counterDefaultValue;
    private TextInputEditText counterValue;
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
    }

    @Override
    protected void onStart() {
        super.onStart();
        setOnClickListeners();
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

        counterName = findViewById(R.id.et_counter_name);
        counterStep = findViewById(R.id.et_counter_step);
        counterDefaultValue = findViewById(R.id.et_counter_default_value);
        counterValue = findViewById(R.id.et_counter_value);
        counterNameLayout = findViewById(R.id.til_counter_name);
        btnSave = findViewById(R.id.btn_save);
        colorSlider = findViewById(R.id.color_slider);
        moreColorsButton = findViewById(R.id.btn_more_colors);


        String colorHex = getIntent().getStringExtra(ARGUMENT_COUNTER_COLOR);
        if (colorHex != null) {
            int boxStrokeColor = Color.parseColor(colorHex);
            if (boxStrokeColor != Color.WHITE) {
                counterNameLayout.setBoxStrokeColor(boxStrokeColor);
                if (ColorUtil.isDarkBackground(boxStrokeColor)){
                    btnSave.setTextColor(0xDEFFFFFF);
                } else {
                    btnSave.setTextColor(0xDE000000);
                }
                btnSave.setBackgroundColor(boxStrokeColor);
            } else {
                counterNameLayout.setBoxStrokeColor(Color.LTGRAY);
                btnSave.setBackgroundColor(DialogUtils.getColor(this,R.color.colorPrimary));
            }
            counterNameLayout.requestFocus();
        }
    }

    private void setOnClickListeners() {
        ((TextInputLayout) findViewById(R.id.til_counter_step)).setEndIconOnClickListener(v -> new MaterialDialog.Builder(EditCounterActivity.this)
                .content(R.string.dialog_step_info_content)
                .positiveText(R.string.common_got_it)
                .show());

        ((TextInputLayout) findViewById(R.id.til_counter_default_value)).setEndIconOnClickListener(v -> new MaterialDialog.Builder(EditCounterActivity.this)
                .content(R.string.dialog_default_info_content)
                .positiveText(R.string.common_got_it)
                .show());

        ((TextInputLayout) findViewById(R.id.til_counter_value)).setEndIconOnClickListener(v -> new MaterialDialog.Builder(EditCounterActivity.this)
                .content(R.string.message_you_can_use_long_press)
                .positiveText(R.string.common_got_it)
                .show());

        moreColorsButton.setOnClickListener(view -> {
            ColorPicker colorPicker = new ColorPicker(EditCounterActivity.this);
            colorPicker.disableDefaultButtons(true);
            colorPicker.setColors(R.array.bright_palette);
            colorPicker.setTitle(null);
            colorPicker.setColorButtonSize(72, 72);
            colorPicker.setColumns(3);
            colorPicker.setOnFastChooseColorListener(new ColorPicker.OnFastChooseColorListener() {
                @Override
                public void setOnFastChooseColorListener(int position, int color) {
                    applyNewColor(color);
                }

                @Override
                public void onCancel() {
                    // put code
                }
            });
            colorPicker.show();
        });

        colorSlider.setListener((position, color) -> {
            applyNewColor(color);
        });

        btnSave.setOnClickListener(v -> validateAndSave());
    }

    private void applyNewColor(int color) {
        if (color != Color.WHITE) {
            counterNameLayout.setBoxStrokeColor(color);
            if (ColorUtil.isDarkBackground(color)){
                btnSave.setTextColor(0xDEFFFFFF);
            } else {
                btnSave.setTextColor(0xDE000000);
            }
            btnSave.setBackgroundColor(color);
        } else {
            counterNameLayout.setBoxStrokeColor(Color.LTGRAY);
            btnSave.setBackgroundColor(DialogUtils.getColor(this,R.color.colorPrimary));
            btnSave.setTextColor(0xDE000000);
        }
        newCounterColor = ColorUtil.intColorToString(color);
        counterName.requestFocus();
    }

    private void validateAndSave() {
        String newName = counterName.getText().toString();
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
        int newValue = Utilities.parseInt(counterValue.getText().toString());
        if (counter.getValue() != newValue) {
            viewModel.updateValue(newValue);
        }
        int defaultValue = Utilities.parseInt(counterDefaultValue.getText().toString());
        if (counter.getDefaultValue() != defaultValue) {
            viewModel.updateDefaultValue(defaultValue);
        }
        int step = Utilities.parseInt(counterStep.getText().toString());
        if (counter.getStep() != step) {
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
                counterValue.setText(String.valueOf(c.getValue()));
                counterStep.setText(String.valueOf(c.getStep()));
                counterDefaultValue.setText(String.valueOf(c.getDefaultValue()));
                if (!c.getName().equals(counterName.getText().toString())) {
                    counterName.setText(c.getName());
                    counterName.setSelection(c.getName().length());
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
