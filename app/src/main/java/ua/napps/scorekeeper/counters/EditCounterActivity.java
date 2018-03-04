package ua.napps.scorekeeper.counters;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.MaterialDialog.Builder;
import com.afollestad.materialdialogs.color.CircleView;
import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.google.firebase.analytics.FirebaseAnalytics.Param;

import timber.log.Timber;
import ua.com.napps.scorekeeper.R;
import ua.napps.scorekeeper.app.App;
import ua.napps.scorekeeper.app.Constants;
import ua.napps.scorekeeper.storage.DatabaseHolder;
import ua.napps.scorekeeper.utils.AndroidFirebaseAnalytics;
import ua.napps.scorekeeper.utils.ColorUtil;
import ua.napps.scorekeeper.utils.ViewUtil;

public class EditCounterActivity extends AppCompatActivity implements ColorChooserDialog.ColorCallback, EditCounterViewModel.EditCounterViewModelCallback {

    public static final int RESULT_DELETE = 1003;
    private static final String ARGUMENT_COUNTER_ID = "ARGUMENT_COUNTER_ID";

    private CircleView colorPreview;
    private Counter counter;
    private TextView counterDefaultValue;
    private EditText counterName;
    private TextView counterStep;
    private TextView labelChangesSaved;
    private TextView counterValue;
    private boolean isNameModified;
    private EditCounterViewModel viewModel;

    public static void start(Context context, int counterId) {
        Intent intent = new Intent(context, EditCounterActivity.class);
        intent.putExtra(ARGUMENT_COUNTER_ID, counterId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if (extras != null && !extras.containsKey(ARGUMENT_COUNTER_ID)) {
            throw new UnsupportedOperationException("Activity should be started using the static start method");
        }
        setContentView(R.layout.activity_edit_counter);
        ViewUtil.setLightStatusBar(this, App.getTinyDB().getBoolean(Constants.SETTINGS_DICE_THEME_LIGHT, true));
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
        labelChangesSaved = findViewById(R.id.tv_label_saved);
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
            final MaterialDialog md = new Builder(EditCounterActivity.this)
                    .content(R.string.dialog_current_value_title)
                    .inputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED)
                    .positiveText(R.string.common_set)
                    .negativeColorRes(R.color.primaryColor)
                    .negativeText(R.string.common_cancel)
                    .input(String.valueOf(counter.getValue()), null, true,
                            (dialog, input) -> {
                                if (input.length() > 0) {
                                    final String newValue = input.toString();
                                    try {
                                        final int value = Integer.parseInt(newValue);
                                        viewModel.updateValue(value);
                                        Bundle params = new Bundle();
                                        params.putLong(Param.SCORE, value);
                                        AndroidFirebaseAnalytics
                                                .logEvent("edit_counter_value_modified",
                                                        params);
                                    } catch (NumberFormatException e) {
                                        Timber.e(e, "value: %s", newValue);
                                    }
                                }
                            })
                    .build();
            EditText inputEditText = md.getInputEditText();
            if (inputEditText != null) {
                inputEditText.setOnEditorActionListener((textView, actionId, event) -> {
                    if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId
                            == EditorInfo.IME_ACTION_DONE)) {
                        View positiveButton = md.getActionButton(DialogAction.POSITIVE);
                        positiveButton.callOnClick();
                    }
                    return false;
                });
            }
            md.show();
            AndroidFirebaseAnalytics.logEvent("edit_counter_value_click");
        });

        counterDefaultValueContainer.setOnClickListener(v -> {
            final MaterialDialog md = new Builder(EditCounterActivity.this)
                    .content(R.string.dialog_counter_default_title)
                    .inputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED)
                    .positiveText(R.string.common_set)
                    .negativeText(R.string.common_cancel)
                    .negativeColorRes(R.color.primaryColor)
                    .input(String.valueOf(counter.getDefaultValue()), null, true,
                            (dialog, input) -> {
                                if (input.length() > 0) {
                                    final String newDefaultValue = input.toString();
                                    try {
                                        final int value = Integer.parseInt(newDefaultValue);
                                        viewModel.updateDefaultValue(value);
                                        Bundle params = new Bundle();
                                        params.putLong(Param.SCORE, value);
                                        AndroidFirebaseAnalytics
                                                .logEvent("edit_counter_default_modified",
                                                        params);
                                    } catch (NumberFormatException e) {
                                        Timber.e(e, "value: %s", newDefaultValue);
                                    }

                                }
                            })
                    .build();
            EditText editText = md.getInputEditText();
            if (editText != null) {
                editText.setOnEditorActionListener((textView, actionId, event) -> {
                    if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId
                            == EditorInfo.IME_ACTION_DONE)) {
                        View positiveButton = md.getActionButton(DialogAction.POSITIVE);
                        positiveButton.callOnClick();
                    }
                    return false;
                });
            }
            md.show();
            AndroidFirebaseAnalytics.logEvent("edit_counter_default_click");
        });

        counterStepContainer.setOnClickListener(v -> {
            final MaterialDialog md = new Builder(EditCounterActivity.this)
                    .content(R.string.dialog_counter_step_title)
                    .inputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED)
                    .positiveText(R.string.common_set)
                    .negativeText(R.string.common_cancel)
                    .negativeColorRes(R.color.primaryColor)
                    .input(String.valueOf(counter.getStep()), null, true,
                            (dialog, input) -> {
                                if (input.length() > 0) {
                                    final String newStepValue = input.toString();
                                    try {
                                        final int value = Integer.parseInt(newStepValue);
                                        viewModel.updateStep(value);
                                        Bundle params = new Bundle();
                                        params.putLong(Param.SCORE, value);
                                        AndroidFirebaseAnalytics
                                                .logEvent("edit_counter_step_modified",
                                                        params);
                                    } catch (NumberFormatException e) {
                                        Timber.e(e, "value: %s", newStepValue);
                                    }

                                }
                            })
                    .build();
            EditText editText = md.getInputEditText();
            if (editText != null) {
                editText.setOnEditorActionListener((textView, actionId, event) -> {
                    if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId
                            == EditorInfo.IME_ACTION_DONE)) {
                        View positiveButton = md.getActionButton(DialogAction.POSITIVE);
                        positiveButton.callOnClick();
                    }
                    return false;
                });
            }
            md.show();
            AndroidFirebaseAnalytics.logEvent("edit_counter_step_click");
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
            AndroidFirebaseAnalytics.logEvent("edit_counter_color_click");
        });

        Button btnDelete = findViewById(R.id.btn_delete);
        btnDelete.setOnClickListener(v -> {
            setResult(RESULT_DELETE);
            viewModel.deleteCounter();
            AndroidFirebaseAnalytics.logEvent("edit_counter_delete_click");
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isNameModified && counter != null) {
            Bundle params = new Bundle();
            params.putLong(Param.SCORE, counter.getName().length());
            AndroidFirebaseAnalytics.logEvent("edit_counter_name_length", params);
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
        Bundle params = new Bundle();
        params.putString(Param.CHARACTER, hex);
        AndroidFirebaseAnalytics.logEvent("edit_counter_color_selected", params);
    }

    private EditCounterViewModel getViewModel(int id) {
        CountersDao countersDao = DatabaseHolder.database().countersDao();
        EditCounterViewModelFactory factory = new EditCounterViewModelFactory(id, countersDao, this);
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
                colorPreview.setBackgroundColor(Color.parseColor(c.getColor()));
                if (!c.getName().equals(counterName.getText().toString())) {
                    counterName.setText(c.getName());
                    counterName.setSelection(c.getName().length());
                }
            } else {
                counter = null;
                setResult(RESULT_DELETE);
                finish();
            }
        });
    }

    @Override
    public void showSavedState() {
        labelChangesSaved.setVisibility(View.VISIBLE);
    }
}
