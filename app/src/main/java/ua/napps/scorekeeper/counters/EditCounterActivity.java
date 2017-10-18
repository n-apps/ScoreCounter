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
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.color.CircleView;
import com.afollestad.materialdialogs.color.ColorChooserDialog;
import ua.com.napps.scorekeeper.R;
import ua.napps.scorekeeper.utils.ColorUtil;

public class EditCounterActivity extends AppCompatActivity implements ColorChooserDialog.ColorCallback {

    public static final int REQUEST_CODE = 1;

    public static final int RESULT_DELETE = 1003;

    public static final int RESULT_EDITED = 1004;

    protected static final String ARGUMENT_COUNTER_ID = "ARGUMENT_COUNTER_ID";

    private CircleView colorPreview;

    private Counter counter;

    private TextView counterDefaultValue;

    private EditText counterName;

    private TextView counterStep;

    private TextView counterValue;

    private boolean firstLoad;

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

        counterValueContainer.setOnClickListener(v -> new MaterialDialog.Builder(EditCounterActivity.this)
                .content("Counter value")
                .inputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED)
                .positiveText("Set new value")
                .negativeText("Cancel")
                .input("Set value", counter != null ? String.valueOf(counter.getValue()) : "", false,
                        (dialog, input) -> viewModel.updateValue(Integer.parseInt(input.toString()))).show());

        counterDefaultValueContainer.setOnClickListener(v -> new MaterialDialog.Builder(EditCounterActivity.this)
                .content("Counter default value (it will be set after resetting)")
                .inputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED)
                .positiveText("Set new default value")
                .negativeText("Cancel")
                .input("Set value", counter != null ? String.valueOf(counter.getDefaultValue()) : "", false,
                        (dialog, input) -> viewModel.updateDefaultValue(Integer.parseInt(input.toString()))).show());

        counterStepContainer.setOnClickListener(v -> new MaterialDialog.Builder(EditCounterActivity.this)
                .content("Counter step")
                .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
                        | InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE)
                .positiveText("Set new step")
                .negativeText("Cancel")
                .input("Set value", counter != null ? String.valueOf(counter.getStep()) : "", false,
                        (dialog, input) -> viewModel.updateStep(Integer.parseInt(input.toString()))).show());

        counterColorContainer.setOnClickListener(
                v -> new ColorChooserDialog.Builder(EditCounterActivity.this, R.string.dialog_select_color_title)
                        .doneButton(
                                R.string.action_select)
                        .cancelButton(R.string.action_cancel)
                        .dynamicButtonColor(false)
                        .allowUserColorInputAlpha(false)
                        .show(EditCounterActivity.this));

        CardView btnDelete = findViewById(R.id.btn_delete);
        btnDelete.setOnClickListener(v -> {
            setResult(RESULT_DELETE);
            viewModel.deleteCounter();
            finish();
        });
    }

    @Override
    public void onColorChooserDismissed(@NonNull ColorChooserDialog dialog) {
    }

    @Override
    public void onColorSelection(@NonNull ColorChooserDialog dialog, int color) {
        colorPreview.setBackgroundColor(color);
        final String hex = ColorUtil.intColorToString(color);
        viewModel.updateColor(hex);
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
                if (!firstLoad) {
                    firstLoad = true;
                    counterName.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void afterTextChanged(final Editable s) {
                            viewModel.updateName(s.toString());
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
                }
                setResult(RESULT_EDITED);
            } else {
                setResult(RESULT_DELETE);
                finish();
            }
        });
    }
}
