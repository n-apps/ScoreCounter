package ua.napps.scorekeeper.counters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.analytics.FirebaseAnalytics.Param;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import ua.com.napps.scorekeeper.R;
import ua.napps.scorekeeper.log.LogEntry;
import ua.napps.scorekeeper.log.LogType;
import ua.napps.scorekeeper.settings.LocalSettings;
import ua.napps.scorekeeper.utils.AndroidFirebaseAnalytics;
import ua.napps.scorekeeper.utils.ColorUtil;
import ua.napps.scorekeeper.utils.Singleton;
import ua.napps.scorekeeper.utils.ViewUtil;

public class EditCounterActivity extends AppCompatActivity implements ColorChooserDialog.ColorCallback, EditCounterViewModel.EditCounterViewModelCallback {

    private static final String ARGUMENT_COUNTER_ID = "ARGUMENT_COUNTER_ID";
    private static final String ARGUMENT_COUNTER_COLOR = "ARGUMENT_COUNTER_COLOR";
    private static final String STATE_IS_NAME_MODIFIED = "STATE_IS_NAME_MODIFIED";

    private Counter counter;
    private TextInputEditText counterStep;
    private TextInputLayout counterNameLayout;
    private TextInputEditText counterName;
    private TextInputEditText counterDefaultValue;
    private TextInputEditText counterPosition;
    private TextInputEditText labelChangesSaved;
    private TextInputEditText counterValue;
    private boolean isNameModified;
    private EditCounterViewModel viewModel;
    private Disposable disposable;

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

        if (savedInstanceState == null) {
            AndroidFirebaseAnalytics.trackScreen(this, "Edit Counter Screen");
        } else {
            isNameModified = savedInstanceState.getBoolean(STATE_IS_NAME_MODIFIED);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_IS_NAME_MODIFIED, isNameModified);
    }

    @Override
    protected void onStart() {
        super.onStart();
        setOnClickListeners();
        isNameModified = false;

        disposable = createTextChangeObservable()
                .subscribe(s -> {
                    viewModel.updateName(s);
                    isNameModified = true;
                });
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }

        if (isNameModified && counter != null) {
            Bundle params = new Bundle();
            params.putString(Param.CHARACTER, counter.getName());
            AndroidFirebaseAnalytics.logEvent("counter_name_submit", params);
        }
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
        counterPosition = findViewById(R.id.et_counter_position);
//        labelChangesSaved = findViewById(R.id.et_label_saved);
        counterNameLayout = findViewById(R.id.til_counter_name);
        int color = Color.parseColor(getIntent().getStringExtra(ARGUMENT_COUNTER_COLOR));
        counterNameLayout.setBoxStrokeColor(color);
    }

    private void setOnClickListeners() {
//        findViewById(R.id.fab).setOnClickListener(v -> new ColorChooserDialog.Builder(EditCounterActivity.this,
//                R.string.counter_details_color_picker_title)
//                .doneButton(R.string.common_select)
//                .cancelButton(R.string.common_cancel)
//                .customButton(R.string.common_custom)
//                .backButton(R.string.common_back)
//                .presetsButton(R.string.dialog_color_picker_presets_button)
//                .dynamicButtonColor(false)
//                .allowUserColorInputAlpha(false)
//                .show(EditCounterActivity.this));
//        findViewById(R.id.counter_value).setOnClickListener(v -> {
//            final MaterialDialog md = new MaterialDialog.Builder(EditCounterActivity.this)
//                    .content(R.string.dialog_current_value_title)
//                    .inputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED)
//                    .positiveText(R.string.common_set)
//                    .negativeColorRes(R.color.primaryColor)
//                    .negativeText(R.string.common_cancel)
//                    .input(String.valueOf(counter.getValue()), null, false,
//                            (dialog, input) -> {
//                                int intValue = Utilities.parseInt(input.toString());
//                                Singleton.getInstance().addLogEntry(new LogEntry(counter, LogType.SET, intValue, counter.getValue()));
//                                viewModel.updateValue(intValue);
//                            })
//                    .build();
//            EditText inputEditText = md.getInputEditText();
//            if (inputEditText != null) {
//                inputEditText.setOnEditorActionListener((textView, actionId, event) -> {
//                    if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
//                        View positiveButton = md.getActionButton(DialogAction.POSITIVE);
//                        positiveButton.callOnClick();
//                    }
//                    return false;
//                });
//            }
//            md.show();
//        });
//        findViewById(R.id.counter_default_value).setOnClickListener(v -> {
//            final MaterialDialog md = new MaterialDialog.Builder(EditCounterActivity.this)
//                    .content(R.string.dialog_counter_default_title)
//                    .inputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED)
//                    .positiveText(R.string.common_set)
//                    .negativeText(R.string.common_cancel)
//                    .negativeColorRes(R.color.primaryColor)
//                    .input(String.valueOf(counter.getDefaultValue()), null, false,
//                            (dialog, input) -> {
//                                String value = input.toString();
//                                viewModel.updateDefaultValue(Utilities.parseInt(value));
//                                Bundle params = new Bundle();
//                                params.putString(Param.CHARACTER, value);
//                                AndroidFirebaseAnalytics.logEvent("counter_default_value_submit", params);
//                            })
//                    .build();
//            EditText editText = md.getInputEditText();
//            if (editText != null) {
//                editText.setOnEditorActionListener((textView, actionId, event) -> {
//                    if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
//                        View positiveButton = md.getActionButton(DialogAction.POSITIVE);
//                        positiveButton.callOnClick();
//                    }
//                    return false;
//                });
//            }
//            md.show();
//        });
//        findViewById(R.id.counter_step).setOnClickListener(v -> {
//            final MaterialDialog md = new MaterialDialog.Builder(EditCounterActivity.this)
//                    .content(R.string.dialog_counter_step_title)
//                    .inputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED)
//                    .positiveText(R.string.common_set)
//                    .negativeText(R.string.common_cancel)
//                    .negativeColorRes(R.color.primaryColor)
//                    .input(String.valueOf(counter.getStep()), null, false,
//                            (dialog, input) -> {
//                                final String value = input.toString();
//                                viewModel.updateStep(Utilities.parseInt(value));
//                                Bundle params = new Bundle();
//                                params.putString(Param.CHARACTER, value);
//                                AndroidFirebaseAnalytics.logEvent("counter_step_submit", params);
//                            })
//                    .build();
//            EditText editText = md.getInputEditText();
//            if (editText != null) {
//                editText.setOnEditorActionListener((textView, actionId, event) -> {
//                    if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
//                        View positiveButton = md.getActionButton(DialogAction.POSITIVE);
//                        positiveButton.callOnClick();
//                    }
//                    return false;
//                });
//            }
//            md.show();
//        });

        final Context context = getApplicationContext();
//        findViewById(R.id.counter_position).setOnClickListener(v -> {
//            final MaterialDialog md = new MaterialDialog.Builder(EditCounterActivity.this)
//                    .content(R.string.counter_details_position)
//                    .inputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL)
//                    .positiveText(R.string.common_set)
//                    .negativeText(R.string.common_cancel)
//                    .negativeColorRes(R.color.primaryColor)
//                    .input(String.valueOf(counter.getStep()), null, false,
//                            (dialog, input) -> {
//                                final String value = input.toString();
//                                int position = Utilities.parseInt(value);
//                                if (position == 0) {
//                                    Toast.makeText(context, R.string.counter_position_zero, Toast.LENGTH_SHORT).show();
//                                } else if (position == counter.getPosition() + 1) {
//                                    Toast.makeText(context, R.string.counter_position_current, Toast.LENGTH_SHORT).show();
//                                } else {
//                                    viewModel.updatePosition(position - 1);
//                                    Bundle params = new Bundle();
//                                    params.putString(Param.CHARACTER, value);
//                                    AndroidFirebaseAnalytics.logEvent("counter_position_submit", params);
//                                }
//                            })
//                    .build();
//            EditText editText = md.getInputEditText();
//            if (editText != null) {
//                editText.setOnEditorActionListener((textView, actionId, event) -> {
//                    if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
//                        View positiveButton = md.getActionButton(DialogAction.POSITIVE);
//                        positiveButton.callOnClick();
//                    }
//                    return false;
//                });
//            }
//            md.show();
//        });

//        findViewById(R.id.iv_step_info).setOnClickListener(v -> {
//            AndroidFirebaseAnalytics.logEvent("help_tooltip_click");
//            new MaterialDialog.Builder(EditCounterActivity.this)
//                    .content(R.string.dialog_step_info_content)
//                    .positiveText(R.string.common_got_it)
//                    .show();
//        });

//        findViewById(R.id.iv_default_value_info).setOnClickListener(v -> {
//            AndroidFirebaseAnalytics.logEvent("help_tooltip_click");
//            new MaterialDialog.Builder(EditCounterActivity.this)
//                    .content(R.string.dialog_default_info_content)
//                    .positiveText(R.string.common_got_it)
//                    .show();
//        });

//        findViewById(R.id.iv_position_info).setOnClickListener(v -> {
//            AndroidFirebaseAnalytics.logEvent("help_tooltip_click");
//            new MaterialDialog.Builder(EditCounterActivity.this)
//                    .content(R.string.dialog_position_info_content)
//                    .positiveText(R.string.common_got_it)
//                    .show();
//        });
    }

    @Override
    public void onColorChooserDismissed(@NonNull ColorChooserDialog dialog) {
    }

    @Override
    public void onColorSelection(@NonNull ColorChooserDialog dialog, int color) {

        counterNameLayout.setBoxStrokeColor(color);
        final String hex = ColorUtil.intColorToString(color);
        viewModel.updateColor(hex);
        Bundle params = new Bundle();
        params.putString(Param.CHARACTER, hex);
        AndroidFirebaseAnalytics.logEvent("edit_counter_color_selected", params);
    }

    private void subscribeToModel(int id) {
        EditCounterViewModelFactory factory = new EditCounterViewModelFactory(id, this);
        viewModel = ViewModelProviders.of(this, factory).get(EditCounterViewModel.class);
        viewModel.getCounterLiveData().observe(this, c -> {
            if (c != null) {
                counter = c;
                viewModel.setCounter(c);
                counterValue.setText(String.valueOf(c.getValue()));
                counterStep.setText(String.valueOf(c.getStep()));
                counterDefaultValue.setText(String.valueOf(c.getDefaultValue()));
                counterPosition.setText(String.valueOf(c.getPosition() + 1));
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

    @Override
    public void showSavedState() {
        labelChangesSaved.setVisibility(View.VISIBLE);
    }

    private Observable<String> createTextChangeObservable() {
        Observable<String> textChangeObservable =
                Observable.create(e -> {
                    final TextWatcher textWatcher = new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            e.onNext(s.toString());
                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                        }
                    };
                    counterName.addTextChangedListener(textWatcher);
                    e.setCancellable(() -> counterName.removeTextChangedListener(textWatcher));
                });
        return textChangeObservable
                .skip(1)
                .debounce(1000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .filter(s -> s.length() > 0);
    }
}
