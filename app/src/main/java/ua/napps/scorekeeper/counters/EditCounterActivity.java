package ua.napps.scorekeeper.counters;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import ua.com.napps.scorekeeper.R;
import ua.napps.scorekeeper.log.LogEntry;
import ua.napps.scorekeeper.log.LogType;
import ua.napps.scorekeeper.settings.LocalSettings;
import ua.napps.scorekeeper.utils.AndroidFirebaseAnalytics;
import ua.napps.scorekeeper.utils.ColorUtil;
import ua.napps.scorekeeper.utils.Singleton;
import ua.napps.scorekeeper.utils.Utilities;
import ua.napps.scorekeeper.utils.ViewUtil;

public class EditCounterActivity extends AppCompatActivity implements ColorChooserDialog.ColorCallback {

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

    public static void start(Activity activity, Counter counter, View view) {
        Intent intent = new Intent(activity, EditCounterActivity.class);
        intent.putExtra(ARGUMENT_COUNTER_ID, counter.getId());
        intent.putExtra(ARGUMENT_COUNTER_COLOR, counter.getColor());
        activity.startActivity(intent);
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
            ViewUtil.setLightStatusBar(this, Color.WHITE);
        } else {
            ViewUtil.clearLightStatusBar(this, Color.BLACK);
        }
        ViewUtil.setNavBarColor(this, isLightTheme);

        subscribeToModel(id);
    }


    @Override
    public void onResume() {
        super.onResume();
        AndroidFirebaseAnalytics.trackScreen(this, "Edit Counter", getClass().getSimpleName());
    }

    @Override
    protected void onStart() {
        super.onStart();
        setOnClickListeners();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.remove_counter, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_remove_counter){

            Singleton.getInstance().addLogEntry(new LogEntry(counter, LogType.RMV, 0, counter.getValue()));
            viewModel.deleteCounter();
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
        int color = Color.parseColor(getIntent().getStringExtra(ARGUMENT_COUNTER_COLOR));
        counterNameLayout.setBoxStrokeColor(color);
    }

    private void setOnClickListeners() {
        counterNameLayout.setEndIconOnClickListener(v -> new ColorChooserDialog.Builder(EditCounterActivity.this,
                R.string.counter_details_color_picker_title)
                .doneButton(R.string.common_select)
                .cancelButton(R.string.common_cancel)
                .customButton(R.string.common_custom)
                .backButton(R.string.common_back)
                .presetsButton(R.string.dialog_color_picker_presets_button)
                .dynamicButtonColor(false)
                .allowUserColorInputAlpha(false)
                .show(EditCounterActivity.this));

        ((TextInputLayout)findViewById(R.id.til_counter_step)).setEndIconOnClickListener(v -> {
            new MaterialDialog.Builder(EditCounterActivity.this)
                    .content(R.string.dialog_step_info_content)
                    .positiveText(R.string.common_got_it)
                    .show();
        });

        ((TextInputLayout)findViewById(R.id.til_counter_default_value)).setEndIconOnClickListener(v -> {
            new MaterialDialog.Builder(EditCounterActivity.this)
                    .content(R.string.dialog_default_info_content)
                    .positiveText(R.string.common_got_it)
                    .show();
        });

        btnSave.setOnClickListener(v -> validateAndSave());
    }

    private void validateAndSave() {
        viewModel.updateName(counterName.getText().toString());
        viewModel.updateColor(newCounterColorHex);
        viewModel.updateValue(Utilities.parseInt(counterValue.getText().toString()));
        viewModel.updateDefaultValue(Utilities.parseInt(counterDefaultValue.getText().toString()));
        viewModel.updateStep(Utilities.parseInt(counterStep.getText().toString()));
        supportFinishAfterTransition();
    }

    @Override
    public void onColorChooserDismissed(@NonNull ColorChooserDialog dialog) {
    }

    @Override
    public void onColorSelection(@NonNull ColorChooserDialog dialog, int color) {
        counterNameLayout.setBoxStrokeColor(color);
        newCounterColorHex = ColorUtil.intColorToString(color);
    }

    private void subscribeToModel(int id) {
        EditCounterViewModelFactory factory = new EditCounterViewModelFactory(id);
        viewModel = ViewModelProviders.of(this, factory).get(EditCounterViewModel.class);
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
            } else {
                counter = null;
                finish();
            }
        });
        viewModel.getCounters().observe(this, c -> {
        });
    }

}
