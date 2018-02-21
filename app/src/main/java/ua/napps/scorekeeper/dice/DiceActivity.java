package ua.napps.scorekeeper.dice;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.animation.DynamicAnimation;
import android.support.animation.SpringAnimation;
import android.support.animation.SpringForce;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics.Param;

import ua.com.napps.scorekeeper.R;
import ua.napps.scorekeeper.app.Constants;
import ua.napps.scorekeeper.storage.TinyDB;
import ua.napps.scorekeeper.utils.AndroidFirebaseAnalytics;
import ua.napps.scorekeeper.utils.ViewUtil;

public class DiceActivity extends AppCompatActivity implements DrawerLayout.DrawerListener {

    private static final String ARGUMENT_ACTUAL_DICE_VALUE = "ARGUMENT_ACTUAL_DICE_VALUE";

    private static final String ARGUMENT_PREVIOUS_DICE_VALUE = "ARGUMENT_PREVIOUS_DICE_VALUE";

    private float accel;

    private float accelCurrent;

    private float accelLast;

    private int currentDiceResult;

    private ImageView dice;

    private boolean isThemeLight;

    private int prevDiceValue;

    private TextView previousResultTextView;

    private SpringAnimation springAnimation;

    private SpringForce springForce;

    private DiceViewModel viewModel;

    private DrawerLayout drawer;

    private TinyDB settingsDB;

    private boolean shakeToRollEnabled;

    private int currentDiceVariant;

    public static Intent getIntent(Context context) {
        return new Intent(context, DiceActivity.class);
    }

    public static Intent getIntentForRestoreState(Context context, int diceValue, int prevDiceValue) {
        Intent intent = new Intent(context, DiceActivity.class);
        intent.putExtra(ARGUMENT_ACTUAL_DICE_VALUE, diceValue);
        intent.putExtra(ARGUMENT_PREVIOUS_DICE_VALUE, prevDiceValue);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        settingsDB = new TinyDB(getApplication());
        isThemeLight = settingsDB.getBoolean(Constants.SETTINGS_DICE_THEME_LIGHT, true);
        setTheme(isThemeLight ? R.style.AppTheme : R.style.AppTheme_Dark);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dice);
        dice = findViewById(R.id.dice);
        previousResultTextView = findViewById(R.id.tv_previous_result);
        drawer = findViewById(R.id.drawer_layout);
        FrameLayout sixDiceButton = findViewById(R.id.six_sides);
        FrameLayout eightDiceButton = findViewById(R.id.eight_sides);
        FrameLayout twelveDiceButton = findViewById(R.id.twelve_sides);
        LinearLayout drawerContent = findViewById(R.id.drawer_content);
        SwitchCompat switchShakeToRoll = findViewById(R.id.sw_shake_roll);
        SwitchCompat switchDarkTheme = findViewById(R.id.sw_dark_theme);
        switchDarkTheme.setChecked(!isThemeLight);
        switchDarkTheme.setOnCheckedChangeListener((buttonView, isChecked) -> isThemeLight = !isChecked);
        switchShakeToRoll.setOnCheckedChangeListener((buttonView, isChecked) -> shakeToRollEnabled = isChecked);
        drawerContent.setOnTouchListener((v, event) -> true);

        sixDiceButton.setOnClickListener(v -> {
            currentDiceVariant = 6;
            updateSelectedDiceVariant();
            settingsDB.putInt(Constants.SETTINGS_DICE_VARIANT, 6);
        });
        eightDiceButton.setOnClickListener(v -> {
            currentDiceVariant = 8;
            updateSelectedDiceVariant();
            settingsDB.putInt(Constants.SETTINGS_DICE_VARIANT, 8);
        });
        twelveDiceButton.setOnClickListener(v -> {
            currentDiceVariant = 20;
            updateSelectedDiceVariant();
            settingsDB.putInt(Constants.SETTINGS_DICE_VARIANT, 20);
        });

        dice.setOnClickListener(v -> {
            viewModel.rollDice();
            Bundle params = new Bundle();
            params.putString(Param.CHARACTER, "click");
            AndroidFirebaseAnalytics.logEvent(getApplicationContext(), "roll_dice", params);
        });
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_open_drawer).setOnClickListener(v -> drawer.openDrawer(GravityCompat.END));

        switchShakeToRoll.setChecked(shakeToRollEnabled);
        shakeToRollEnabled = settingsDB.getBoolean(Constants.SETTINGS_SHAKE_TO_ROLL, true);
        currentDiceVariant = settingsDB.getInt(Constants.SETTINGS_DICE_VARIANT, 6);
        drawer.addDrawerListener(this);

        trackSelectedDiceVariant();
        trackSelectedTheme();
        trackShakeToRoll();

        springForce = new SpringForce()
                .setDampingRatio(SpringForce.DAMPING_RATIO_LOW_BOUNCY)
                .setStiffness(SpringForce.STIFFNESS_LOW)
                .setFinalPosition(1);

        currentDiceResult = getIntent().getIntExtra(ARGUMENT_ACTUAL_DICE_VALUE, 0);
        prevDiceValue = getIntent().getIntExtra(ARGUMENT_PREVIOUS_DICE_VALUE, 0);

        subscribeToModel();
        if (shakeToRollEnabled) {
            initSensorData();
            useSensorLiveData();
        }
        updateSelectedDiceVariant();

        ViewUtil.setLightStatusBars(this, isThemeLight, isThemeLight);

        if (currentDiceResult > 0) {
            rollDice(currentDiceResult, prevDiceValue);
        }
    }

    private void trackSelectedDiceVariant() {
        Bundle params = new Bundle();
        params.putString(Param.CHARACTER, String.valueOf(currentDiceVariant));
        AndroidFirebaseAnalytics.logEvent(getApplicationContext(), "dice_variant", params);
    }

    private void trackSelectedTheme() {
        if (!getIntent().hasExtra(ARGUMENT_ACTUAL_DICE_VALUE)) {
            Bundle params = new Bundle();
            params.putLong(Param.SCORE, isThemeLight ? 1 : 0);
            AndroidFirebaseAnalytics.logEvent(getApplicationContext(), "settings_dice_theme_light", params);
        }
    }

    private void trackShakeToRoll() {
        Bundle params = new Bundle();
        params.putLong(Param.SCORE, shakeToRollEnabled ? 1 : 0);
        AndroidFirebaseAnalytics.logEvent(getApplicationContext(), "settings_dice_shake_to_roll", params);
    }

    private void updateSelectedDiceVariant() {
        switch (currentDiceVariant) {
            case 8:
                findViewById(R.id.six_sides_checkmark).setVisibility(View.INVISIBLE);
                findViewById(R.id.eight_sides_checkmark).setVisibility(View.VISIBLE);
                findViewById(R.id.twelve_sides_checkmark).setVisibility(View.INVISIBLE);
                viewModel.getDiceLiveData().setDiceVariant(8);
                break;
            case 20:
                findViewById(R.id.six_sides_checkmark).setVisibility(View.INVISIBLE);
                findViewById(R.id.eight_sides_checkmark).setVisibility(View.INVISIBLE);
                findViewById(R.id.twelve_sides_checkmark).setVisibility(View.VISIBLE);
                viewModel.getDiceLiveData().setDiceVariant(20);
                break;
            case 6:
            default:
                findViewById(R.id.six_sides_checkmark).setVisibility(View.VISIBLE);
                findViewById(R.id.eight_sides_checkmark).setVisibility(View.INVISIBLE);
                findViewById(R.id.twelve_sides_checkmark).setVisibility(View.INVISIBLE);
                viewModel.getDiceLiveData().setDiceVariant(6);
                break;

        }
    }

    private SpringAnimation getSpringAnimation() {
        return new SpringAnimation(dice, DynamicAnimation.ROTATION).setSpring(springForce)
                .setStartValue(100f).setStartVelocity(100);
    }

    private void initSensorData() {
        accel = 0.00f;
        accelCurrent = SensorManager.GRAVITY_EARTH;
        accelLast = SensorManager.GRAVITY_EARTH;
    }

    private void rollDice(int diceResult, int previousResult) {
        int prevValue;
        if (previousResult == 0) {
            previousResultTextView.setVisibility(View.GONE);
            prevValue = ((int) (Math.random() * currentDiceVariant)) + 1;
        } else {
            prevValue = previousResult;
            previousResultTextView.setVisibility(View.VISIBLE);
            previousResultTextView
                    .setText(String.format(getString(R.string.dice_previous_result_label), previousResult));
            if (springAnimation != null) {
                springAnimation.cancel();
            }
        }

        currentDiceResult = diceResult;
        prevDiceValue = prevValue;

        @DrawableRes int diceResId = 0;

        switch (diceResult) {
            case 1: {
                diceResId = R.drawable.dice_digital_1;
                break;
            }
            case 2: {
                diceResId = R.drawable.dice_digital_2;
                break;
            }
            case 3: {
                diceResId = R.drawable.dice_digital_3;
                break;
            }
            case 4: {
                diceResId = R.drawable.dice_digital_4;
                break;
            }
            case 5: {
                diceResId = R.drawable.dice_digital_5;
                break;
            }
            case 6: {
                diceResId = R.drawable.dice_digital_6;
                break;
            }
            case 7: {
                diceResId = R.drawable.dice_digital_7;
                break;
            }
            case 8: {
                diceResId = R.drawable.dice_digital_8;
                break;
            }
            case 9: {
                diceResId = R.drawable.dice_digital_9;
                break;
            }
            case 10: {
                diceResId = R.drawable.dice_digital_10;
                break;
            }
            case 11: {
                diceResId = R.drawable.dice_digital_11;
                break;
            }
            case 12: {
                diceResId = R.drawable.dice_digital_12;
                break;
            }
            case 13: {
                diceResId = R.drawable.dice_digital_13;
                break;
            }
            case 14: {
                diceResId = R.drawable.dice_digital_14;
                break;
            }
            case 15: {
                diceResId = R.drawable.dice_digital_15;
                break;
            }
            case 16: {
                diceResId = R.drawable.dice_digital_16;
                break;
            }
            case 17: {
                diceResId = R.drawable.dice_digital_17;
                break;
            }
            case 18: {
                diceResId = R.drawable.dice_digital_18;
                break;
            }
            case 19: {
                diceResId = R.drawable.dice_digital_19;
                break;
            }
            case 20: {
                diceResId = R.drawable.dice_digital_20;
                break;
            }
        }

        dice.setImageResource(diceResId);

        springAnimation = getSpringAnimation();
        springAnimation.start();
    }

    private void subscribeToModel() {
        DiceViewModelFactory factory = new DiceViewModelFactory(currentDiceVariant);
        viewModel = ViewModelProviders.of(this, factory).get(DiceViewModel.class);
        final DiceLiveData diceLiveData = viewModel.getDiceLiveData();
        diceLiveData.observe(this, roll -> {
            if (roll != null && roll > 0) {
                final int previousValue = diceLiveData.getPreviousValue();
                rollDice(roll, previousValue);
            }
        });

    }

    private void useSensorLiveData() {
        viewModel.getSensorLiveData(this).observe(this, se -> {
            if (se == null) {
                return;
            }

            float x = se.values[0];
            float y = se.values[1];
            float z = se.values[2];
            accelLast = accelCurrent;
            accelCurrent = (float) Math.sqrt((double) (x * x + y * y + z * z));
            float delta = accelCurrent - accelLast;
            accel = accel * 0.9f + delta; // perform low-cut filter
            if (accel > 5.0) {
                viewModel.rollDice();
                Bundle params = new Bundle();
                params.putString(Param.CHARACTER, "sensor");
                AndroidFirebaseAnalytics.logEvent(getApplicationContext(), "roll_dice", params);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.END)) {
            drawer.closeDrawer(GravityCompat.END);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

    }

    @Override
    public void onDrawerOpened(@NonNull View drawerView) {
        AndroidFirebaseAnalytics.logEvent(getApplicationContext(), "open_dice_drawer");
    }

    @Override
    public void onDrawerClosed(@NonNull View drawerView) {
        settingsDB.putBoolean(Constants.SETTINGS_SHAKE_TO_ROLL, shakeToRollEnabled);
        if (!shakeToRollEnabled) {
            viewModel.disableSensor();
        } else {
            initSensorData();
            useSensorLiveData();
        }
        if (settingsDB.getBoolean(Constants.SETTINGS_DICE_THEME_LIGHT, true) != isThemeLight) {
            settingsDB.putBoolean(Constants.SETTINGS_DICE_THEME_LIGHT, isThemeLight);
            finish();
            startActivity(getIntentForRestoreState(DiceActivity.this, currentDiceResult, prevDiceValue));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        } else {
            settingsDB.putBoolean(Constants.SETTINGS_DICE_THEME_LIGHT, isThemeLight);
        }
    }

    @Override
    public void onDrawerStateChanged(int newState) {

    }
}
