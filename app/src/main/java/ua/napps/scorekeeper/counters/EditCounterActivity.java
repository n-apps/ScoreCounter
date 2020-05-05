package ua.napps.scorekeeper.counters;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.naz013.colorslider.ColorSlider;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

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
    private EditCounterViewModel viewModel;
    private String newCounterColorHex;
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
        String colorHex = getIntent().getStringExtra(ARGUMENT_COUNTER_COLOR);
        if (colorHex != null) {
            counterNameLayout.setBoxStrokeColor(Color.parseColor(colorHex));
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

        colorSlider.setListener((position, color) -> {
            counterNameLayout.setBoxStrokeColor(color);
            newCounterColorHex = ColorUtil.intColorToString(color);
        });

        btnSave.setOnClickListener(v -> validateAndSave());
    }

    private void validateAndSave() {
        int newValue = Utilities.parseInt(counterValue.getText().toString());

        viewModel.updateName(counterName.getText().toString());
        viewModel.updateColor(newCounterColorHex);
        viewModel.updateValue(newValue);
        viewModel.updateDefaultValue(Utilities.parseInt(counterDefaultValue.getText().toString()));
        viewModel.updateStep(Utilities.parseInt(counterStep.getText().toString()));

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
        viewModel.getCounters().observe(this, c -> {
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
