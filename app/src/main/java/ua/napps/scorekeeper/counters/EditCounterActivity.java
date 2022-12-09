package ua.napps.scorekeeper.counters;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.lifecycle.ViewModelProvider;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.util.DialogUtils;
import com.github.naz013.colorslider.ColorSlider;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.kunzisoft.androidclearchroma.ChromaDialog;
import com.kunzisoft.androidclearchroma.colormode.ColorMode;
import com.kunzisoft.androidclearchroma.listener.OnColorSelectedListener;

import ua.napps.scorekeeper.R;
import ua.napps.scorekeeper.log.LogEntry;
import ua.napps.scorekeeper.log.LogType;
import ua.napps.scorekeeper.settings.LocalSettings;
import ua.napps.scorekeeper.utils.ColorUtil;
import ua.napps.scorekeeper.utils.Singleton;
import ua.napps.scorekeeper.utils.Utilities;
import ua.napps.scorekeeper.utils.ViewUtil;

public class EditCounterActivity extends AppCompatActivity implements OnColorSelectedListener {

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
    private ImageView moreColorsButton;
    private EditCounterViewModel viewModel;
    private String newCounterColor;
    private ColorSlider colorSlider;
    private int selectedColor;

    public static void start(Activity activity, Counter counter) {
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
            ViewUtil.setLightStatusBar(this);
        } else {
            ViewUtil.clearLightStatusBar(this);
        }
        ViewUtil.setNavBarColor(this, isLightTheme);

        subscribeToModel(id);
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
            Typeface medium = null;
            Typeface regular = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                medium = getResources().getFont(R.font.ptm700);
                regular = getResources().getFont(R.font.ptm400);
            }
            new MaterialDialog.Builder(this)
                    .title(R.string.dialog_confirmation_question)
                    .onPositive((dialog, which) -> {
                        Singleton.getInstance().addLogEntry(new LogEntry(counter, LogType.RMV, 0, counter.getValue()));
                        viewModel.deleteCounter();
                    })
                    .onNegative((dialog, which) -> dialog.dismiss())
                    .showListener(dialog1 -> {
                        TextView content = ((MaterialDialog) dialog1).getContentView();
                        if (content != null) {
                            content.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                        }
                    })
                    .typeface(medium, regular)
                    .positiveText(R.string.dialog_yes)
                    .negativeText(R.string.dialog_no)
                    .show();
        }

        return super.onOptionsItemSelected(item);
    }

    private void initViews() {
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

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
            selectedColor = Color.parseColor(colorHex);

            if (selectedColor != Color.WHITE) {
                setInputsColorStateDefault();
                updateInputsColors(selectedColor);
                updateButtonColors(selectedColor);
            } else {
                updateInputsColors(Color.LTGRAY);
                btnSave.setBackgroundColor(DialogUtils.getColor(this, R.color.colorPrimary));
            }
        }

        moreColorsButton.setOnClickListener(v -> new ChromaDialog.Builder()
                .initialColor(selectedColor)
                .colorMode(ColorMode.HSL)
                .create()
                .show(getSupportFragmentManager(), "ChromaDialog"));

        colorSlider.setListener((position, color) -> applyNewColor(color));

        btnSave.setOnClickListener(v -> validateAndSave());
    }

    private void updateButtonColors(int newcolor) {
        Drawable drawable = DrawableCompat.wrap(moreColorsButton.getDrawable().mutate());
        if (ColorUtil.isDarkBackground(newcolor)) {
            btnSave.setTextColor(0xDEFFFFFF);
            DrawableCompat.setTint(drawable, 0xDEFFFFFF);
        } else {
            btnSave.setTextColor(0xDE000000);
            DrawableCompat.setTint(drawable, 0xDE000000);
        }
        moreColorsButton.setBackgroundColor(newcolor);
        btnSave.setBackgroundColor(newcolor);
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
        selectedColor = fillColor;

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
        String newName = counterNameEditText.getText().toString().trim();
        if (!counter.getName().equals(newName)) {
            viewModel.updateName(newName);

            if (newName.equalsIgnoreCase("roman") |
                    newName.equalsIgnoreCase("roma") |
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
        viewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) factory).get(EditCounterViewModel.class);
        viewModel.getCounterLiveData().observe(this, c -> {
            if (c != null) {
                counter = c;
                viewModel.setCounter(c);
                counterValueEditText.setText(String.valueOf(c.getValue()));
                counterStepEditText.setText(String.valueOf(c.getStep()));
                counterDefaultValueEditText.setText(String.valueOf(c.getDefaultValue()));
                if (!c.getName().equals(counterNameEditText.getText().toString())) {
                    counterNameEditText.setText(c.getName());
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

    @Override
    public void onPositiveButtonClick(int color) {
        applyNewColor(color);
    }

    @Override
    public void onNegativeButtonClick(int i) {

    }
}
