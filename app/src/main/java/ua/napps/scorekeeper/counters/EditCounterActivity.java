package ua.napps.scorekeeper.counters;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.afollestad.materialdialogs.MaterialDialog.Builder;
import com.afollestad.materialdialogs.color.CircleView;
import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.google.firebase.analytics.FirebaseAnalytics.Param;
import timber.log.Timber;
import ua.com.napps.scorekeeper.R;
import ua.napps.scorekeeper.app.ScoreKeeperApp;
import ua.napps.scorekeeper.storage.DatabaseHolder;
import ua.napps.scorekeeper.utils.ColorUtil;

public class EditCounterActivity extends AppCompatActivity implements ColorChooserDialog.ColorCallback {

    public static final int REQUEST_CODE = 1;

    public static final int RESULT_DELETE = 1003;

    protected static final String ARGUMENT_COUNTER_ID = "ARGUMENT_COUNTER_ID";

    private CircleView colorPreview;

    private Counter counter;

    private TextView counterDefaultValue;

    private EditText counterName;

    private TextView counterStep;

    private TextView counterValue;

    private boolean isNameModified;

    private EditCounterViewModel viewModel;

    public static Intent getIntent(Context context, final int id) {
        Intent intent = new Intent(context, EditCounterActivity.class);
        intent.putExtra(ARGUMENT_COUNTER_ID, id);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_counter);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        final int id = getIntent().getIntExtra(ARGUMENT_COUNTER_ID, 0);

        viewModel = getViewModel(id);
        subscribeToModel();

        counterName = findViewById(R.id.et_counter_name);
        counterStep = findViewById(R.id.tv_counter_step);
        counterDefaultValue = findViewById(R.id.tv_counter_default_value);
        counterValue = findViewById(R.id.tv_counter_value);
        colorPreview = findViewById(R.id.color_preview);
        LinearLayout counterValueContainer = findViewById(R.id.counter_value);
        LinearLayout counterDefaultValueContainer = findViewById(R.id.counter_default_value);
        LinearLayout counterStepContainer = findViewById(R.id.counter_step);
        LinearLayout counterColorContainer = findViewById(R.id.counter_color);

        counterName.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(final Editable s) {
                final String newName = s.toString();
                if (counter != null && !counter.getName().equals(newName)) {
                    viewModel.updateName(newName);
                    isNameModified = true;
                }
            }

            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count,
                    final int after) {

            }

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before,
                    final int count) {

            }
        });

        counterValueContainer.setOnClickListener(v -> {
            new Builder(EditCounterActivity.this)
                    .content(R.string.dialog_current_value_title)
                    .inputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED)
                    .positiveText(R.string.common_set)
                    .negativeText(R.string.common_cancel)
                    .input(String.valueOf(counter.getValue()), null, true,
                            (dialog, input) -> {
                                if (input.length() > 0) {
                                    final String newValue = input.toString();
                                    try {
                                        viewModel.updateValue(Integer.parseInt(newValue));
                                    } catch (NumberFormatException e) {
                                        Toast.makeText(this, "Wrong value", Toast.LENGTH_SHORT).show();
                                        Timber.e(e, "value: %s", newValue);
                                    }
                                    Bundle params = new Bundle(1);
                                    params.putString(Param.VALUE, newValue);
                                    ((ScoreKeeperApp) getApplication()).getFirebaseAnalytics()
                                            .logEvent("edit_counter_value_modified", params);
                                }
                            }).show();
            ((ScoreKeeperApp) getApplication()).getFirebaseAnalytics().logEvent("edit_counter_value_click", null);
        });

        counterDefaultValueContainer.setOnClickListener(v -> {
            new Builder(EditCounterActivity.this)
                    .content(R.string.dialog_counter_default_title)
                    .inputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED)
                    .positiveText(R.string.common_set)
                    .negativeText(R.string.common_cancel)
                    .input(String.valueOf(counter.getDefaultValue()), null, true,
                            (dialog, input) -> {
                                if (input.length() > 0) {
                                    final String newDefaultValue = input.toString();
                                    try {
                                        viewModel.updateDefaultValue(Integer.parseInt(newDefaultValue));
                                    } catch (NumberFormatException e) {
                                        Timber.e(e, "value: %s", newDefaultValue);
                                    }
                                    Bundle params = new Bundle(1);
                                    params.putString(Param.VALUE, newDefaultValue);
                                    ((ScoreKeeperApp) getApplication()).getFirebaseAnalytics()
                                            .logEvent("edit_counter_default_modified", params);
                                }
                            }).show();
            ((ScoreKeeperApp) getApplication()).getFirebaseAnalytics().logEvent("edit_counter_default_click", null);
        });

        counterStepContainer.setOnClickListener(v -> {
            new Builder(EditCounterActivity.this)
                    .content(R.string.dialog_counter_step_title)
                    .inputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED)
                    .positiveText(R.string.common_set)
                    .negativeText(R.string.common_cancel)
                    .input(String.valueOf(counter.getStep()), null, true,
                            (dialog, input) -> {
                                if (input.length() > 0) {
                                    final String newStepValue = input.toString();
                                    try {
                                        viewModel.updateStep(Integer.parseInt(newStepValue));
                                    } catch (NumberFormatException e) {
                                        Timber.e(e, "value: %s", newStepValue);
                                    }
                                    Bundle params = new Bundle(1);
                                    params.putString(Param.VALUE, newStepValue);
                                    ((ScoreKeeperApp) getApplication()).getFirebaseAnalytics()
                                            .logEvent("edit_counter_step_modified", params);
                                }
                            }).show();
            ((ScoreKeeperApp) getApplication()).getFirebaseAnalytics().logEvent("edit_counter_step_click", null);
        });

        counterColorContainer.setOnClickListener(v -> {
            new ColorChooserDialog.Builder(EditCounterActivity.this,
                    R.string.counter_details_color_picker_title)
                    .doneButton(R.string.common_select)
                    .cancelButton(R.string.common_cancel)
                    .customButton(R.string.common_custom)
                    .backButton(R.string.common_back)
                    .presetsButton(R.string.dialog_color_picker_presets_button)
                    .dynamicButtonColor(false)
                    .allowUserColorInputAlpha(false)
                    .show(EditCounterActivity.this);
            ((ScoreKeeperApp) getApplication()).getFirebaseAnalytics()
                    .logEvent("edit_counter_color_click", null);
        });

        CardView btnDelete = findViewById(R.id.btn_delete);
        btnDelete.setOnClickListener(v -> {
            setResult(RESULT_DELETE);
            viewModel.deleteCounter();
            ((ScoreKeeperApp) getApplication()).getFirebaseAnalytics().logEvent("edit_counter_delete_click", null);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isNameModified && counter != null) {
            Bundle params = new Bundle(1);
            params.putString(Param.VALUE, String.valueOf(counter.getName().length()));
            ((ScoreKeeperApp) getApplication()).getFirebaseAnalytics()
                    .logEvent("edit_counter_name_length", params);
        }
    }

    @Override
    public void onColorChooserDismissed(@NonNull ColorChooserDialog dialog) {
    }

    @Override
    public void onColorSelection(@NonNull ColorChooserDialog dialog, int color) {
        colorPreview.setBackgroundColor(color);
        final String hex = ColorUtil.intColorToString(color);
        viewModel.updateColor(hex);
        Bundle params = new Bundle(1);
        params.putString(Param.VALUE, hex);
        ((ScoreKeeperApp) getApplication()).getFirebaseAnalytics().logEvent("edit_counter_color_selected", params);
    }

    private EditCounterViewModel getViewModel(int id) {
        CountersDao countersDao = DatabaseHolder.database().countersDao();
        EditCounterViewModelFactory factory = new EditCounterViewModelFactory(id, countersDao);
        return ViewModelProviders.of(this, factory).get(EditCounterViewModel.class);
    }

    private void subscribeToModel() {
        viewModel.getCounterLiveData().observe(this, c -> {
            if (c != null) {
                counter = c;
                viewModel.setCounter(c);
                counterValue.setText(String.valueOf(c.getValue()));
                counterStep.setText(String.valueOf(c.getStep()));
                counterDefaultValue.setText(String.valueOf(c.getDefaultValue()));
                counterName.setText(c.getName());
                counterName.setSelection(c.getName().length());
                colorPreview.setBackgroundColor(Color.parseColor(c.getColor()));
            } else {
                counter = null;
                setResult(RESULT_DELETE);
                finish();
            }
        });
    }
}
