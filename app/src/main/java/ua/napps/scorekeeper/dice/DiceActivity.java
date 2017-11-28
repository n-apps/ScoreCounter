package ua.napps.scorekeeper.dice;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Animatable;
import android.hardware.SensorManager;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.animation.DynamicAnimation;
import android.support.animation.SpringAnimation;
import android.support.animation.SpringForce;
import android.support.annotation.DrawableRes;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.firebase.analytics.FirebaseAnalytics.Param;
import timber.log.Timber;
import ua.com.napps.scorekeeper.R;
import ua.napps.scorekeeper.app.ScoreKeeperApp;
import ua.napps.scorekeeper.settings.SettingsUtil;
import ua.napps.scorekeeper.storage.TinyDB;

public class DiceActivity extends AppCompatActivity {

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
        final TinyDB settingsDB = new TinyDB(getApplication());
        isThemeLight = settingsDB.getBoolean(SettingsUtil.SETTINGS_DICE_THEME_LIGHT);
        setTheme(isThemeLight ? R.style.AppTheme : R.style.AppTheme_Dark);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dice);
        dice = findViewById(R.id.dice);
        previousResultTextView = findViewById(R.id.tv_previous_result);

        springForce = new SpringForce()
                .setDampingRatio(SpringForce.DAMPING_RATIO_LOW_BOUNCY)
                .setStiffness(SpringForce.STIFFNESS_LOW)
                .setFinalPosition(1);

        dice.setOnClickListener(v -> {
            viewModel.rollDice();
            Bundle params = new Bundle();
            params.putString(Param.CHARACTER, "click");
            ((ScoreKeeperApp) getApplication()).getFirebaseAnalytics().logEvent("roll_dice", params);
        });
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_switch_theme).setOnClickListener(v -> {
            settingsDB.putBoolean(SettingsUtil.SETTINGS_DICE_THEME_LIGHT, !isThemeLight);
            ((ScoreKeeperApp) getApplication()).getFirebaseAnalytics().logEvent("switch_dice_theme", null);
            finish();
            startActivity(getIntentForRestoreState(DiceActivity.this, currentDiceResult, prevDiceValue));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        currentDiceResult = getIntent().getIntExtra(ARGUMENT_ACTUAL_DICE_VALUE, 0);
        prevDiceValue = getIntent().getIntExtra(ARGUMENT_PREVIOUS_DICE_VALUE, 0);

        viewModel = ViewModelProviders.of(this).get(DiceViewModel.class);

        subscribeToModel();
        initSensorData();
        colorStatusBar();

        if (currentDiceResult > 0) {
            rollDice(currentDiceResult, prevDiceValue);
        }

        Bundle params = new Bundle();
        params.putLong(Param.SCORE, isThemeLight ? 1 : 0);
        ((ScoreKeeperApp) getApplication()).getFirebaseAnalytics().logEvent("settings_dice_light_theme", params);
    }

    private void colorStatusBar() {
        if (isThemeLight) {
            if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.light_status_bar));
                if (VERSION.SDK_INT >= VERSION_CODES.M) {
                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                }
            }
        } else {
            if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.dark_status_bar));
            }
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
            prevValue = ((int) (Math.random() * 6)) + 1;
        } else {
            prevValue = previousResult;
            previousResultTextView.setVisibility(View.VISIBLE);
            previousResultTextView
                    .setText(String.format(getString(R.string.dice_previous_result_label), previousResult));
            ((Animatable) dice.getDrawable()).stop();
            if (springAnimation != null) {
                springAnimation.cancel();
            }
        }

        currentDiceResult = diceResult;
        prevDiceValue = prevValue;

        @DrawableRes int diceResId = 0;

        switch (diceResult) {
            case 1: {
                switch (prevValue) {
                    case 2: {
                        diceResId = R.drawable.avd_two_to_one;
                        break;
                    }
                    case 3: {
                        diceResId = R.drawable.avd_three_to_one;
                        break;
                    }
                    case 4: {
                        diceResId = R.drawable.avd_four_to_one;
                        break;
                    }
                    case 5: {
                        diceResId = R.drawable.avd_five_to_one;
                        break;
                    }
                    case 6: {
                        diceResId = R.drawable.avd_six_to_one;
                        break;
                    }
                    default:
                        diceResId = R.drawable.avd_zero_to_one;
                        break;
                }
                break;
            }
            case 2: {
                switch (prevValue) {
                    case 1: {
                        diceResId = R.drawable.avd_one_to_two;
                        break;
                    }
                    case 3: {
                        diceResId = R.drawable.avd_three_to_two;
                        break;
                    }
                    case 4: {
                        diceResId = R.drawable.avd_four_to_two;
                        break;
                    }
                    case 5: {
                        diceResId = R.drawable.avd_five_to_two;
                        break;
                    }
                    case 6: {
                        diceResId = R.drawable.avd_six_to_two;
                        break;
                    }
                    default:
                        diceResId = R.drawable.avd_zero_to_two;
                        break;
                }
                break;
            }
            case 3: {
                switch (prevValue) {
                    case 1: {
                        diceResId = R.drawable.avd_one_to_three;
                        break;
                    }
                    case 2: {
                        diceResId = R.drawable.avd_two_to_three;
                        break;
                    }
                    case 4: {
                        diceResId = R.drawable.avd_four_to_three;
                        break;
                    }
                    case 5: {
                        diceResId = R.drawable.avd_five_to_three;
                        break;
                    }
                    case 6: {
                        diceResId = R.drawable.avd_six_to_three;
                        break;
                    }
                    default:
                        diceResId = R.drawable.avd_zero_to_three;
                        break;
                }
                break;
            }
            case 4: {
                switch (prevValue) {
                    case 1: {
                        diceResId = R.drawable.avd_one_to_four;
                        break;
                    }
                    case 2: {
                        diceResId = R.drawable.avd_two_to_four;
                        break;
                    }
                    case 3: {
                        diceResId = R.drawable.avd_three_to_four;
                        break;
                    }
                    case 5: {
                        diceResId = R.drawable.avd_five_to_four;
                        break;
                    }
                    case 6: {
                        diceResId = R.drawable.avd_six_to_four;
                        break;
                    }
                    default:
                        diceResId = R.drawable.avd_zero_to_four;
                        break;

                }
                break;
            }
            case 5: {
                switch (prevValue) {
                    case 1: {
                        diceResId = R.drawable.avd_one_to_five;
                        break;
                    }
                    case 2: {
                        diceResId = R.drawable.avd_two_to_five;
                        break;
                    }
                    case 3: {
                        diceResId = R.drawable.avd_three_to_five;
                        break;
                    }
                    case 4: {
                        diceResId = R.drawable.avd_four_to_five;
                        break;
                    }
                    case 6: {
                        diceResId = R.drawable.avd_six_to_five;
                        break;
                    }
                    default:
                        diceResId = R.drawable.avd_zero_to_five;
                        break;
                }
                break;
            }
            case 6: {
                switch (prevValue) {
                    case 1: {
                        diceResId = R.drawable.avd_one_to_six;
                        break;
                    }
                    case 2: {
                        diceResId = R.drawable.avd_two_to_six;
                        break;
                    }
                    case 3: {
                        diceResId = R.drawable.avd_three_to_six;
                        break;
                    }
                    case 4: {
                        diceResId = R.drawable.avd_four_to_six;
                        break;
                    }
                    case 5: {
                        diceResId = R.drawable.avd_five_to_six;
                        break;
                    }
                    default:
                        diceResId = R.drawable.avd_zero_to_six;
                        break;
                }
            }
        }
        springAnimation = getSpringAnimation();
        springAnimation.start();
        if (diceResId != 0) {
            AnimatedVectorDrawableCompat animatedVectorDrawableCompat = AnimatedVectorDrawableCompat
                    .create(this, diceResId);
            dice.setImageDrawable(animatedVectorDrawableCompat);
            ((Animatable) dice.getDrawable()).start();
        } else {
            Timber.e("dice result: %d prevValue: %d", diceResult, prevValue);
        }
    }

    private void subscribeToModel() {
        final DiceLiveData diceLiveData = viewModel.getDiceLiveData();
        diceLiveData.observe(this, roll -> {
            if (roll != null && roll > 0) {
                final int previousValue = diceLiveData.getPreviousValue();
                rollDice(roll, previousValue);
            }
        });

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
                ((ScoreKeeperApp) getApplication()).getFirebaseAnalytics().logEvent("roll_dice", params);
            }
        });

    }
}
