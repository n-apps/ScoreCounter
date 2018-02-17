package ua.napps.scorekeeper.dice;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.animation.DynamicAnimation;
import android.support.animation.SpringAnimation;
import android.support.animation.SpringForce;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

import ua.com.napps.scorekeeper.R;
import ua.napps.scorekeeper.settings.Constants;
import ua.napps.scorekeeper.storage.TinyDB;
import ua.napps.scorekeeper.utils.AndroidFirebaseAnalytics;


public class DicesFragment extends Fragment {

    private float accel;
    private float accelCurrent;
    private float accelLast;
    private int currentDiceResult;
    private ImageView dice;
    private int prevDiceValue;
    private TextView previousResultTextView;
    private SpringAnimation springAnimation;
    private SpringForce springForce;
    private DiceViewModel viewModel;
    private TinyDB settingsDB;
    private boolean shakeToRollEnabled;
    private int currentDiceVariant;

    private OnDiceFragmentInteractionListener diceFragmentInteractionListener;

    public DicesFragment() {
        // Required empty public constructor
    }

    public static DicesFragment newInstance() {
        return new DicesFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View contentView = inflater.inflate(R.layout.fragment_dices, container, false);
        previousResultTextView = contentView.findViewById(R.id.tv_previous_result);
        dice = contentView.findViewById(R.id.dice);
        dice.setOnClickListener(v -> {
            viewModel.rollDice();
            Bundle params = new Bundle();
            params.putString(FirebaseAnalytics.Param.CHARACTER, "click");
            AndroidFirebaseAnalytics.logEvent(getActivity(), "roll_dice", params);
        });
        return contentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//        ViewUtil.setLightStatusBars(getActivity(), isThemeLight, isThemeLight);
        settingsDB = new TinyDB(getContext());
        shakeToRollEnabled = settingsDB.getBoolean(Constants.SETTINGS_SHAKE_TO_ROLL, true);
        currentDiceVariant = settingsDB.getInt(Constants.SETTINGS_DICE_VARIANT, 6);
        springForce = new SpringForce()
                .setDampingRatio(SpringForce.DAMPING_RATIO_LOW_BOUNCY)
                .setStiffness(SpringForce.STIFFNESS_LOW)
                .setFinalPosition(1);
        subscribeUI();
        if (shakeToRollEnabled) {
            initSensorData();
            useSensorLiveData();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnDiceFragmentInteractionListener) {
            diceFragmentInteractionListener = (OnDiceFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnDiceFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        diceFragmentInteractionListener = null;
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
        diceFragmentInteractionListener.updateDiceNavMenuBadge(diceResult);
        springAnimation = getSpringAnimation();
        springAnimation.start();
    }

    private void subscribeUI() {
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
        viewModel.getSensorLiveData(getActivity()).observe(this, se -> {
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
                params.putString(FirebaseAnalytics.Param.CHARACTER, "sensor");
                AndroidFirebaseAnalytics.logEvent(getActivity(), "roll_dice", params);
            }
        });
    }

    public interface OnDiceFragmentInteractionListener {
        // TODO: Update argument type and name
        void updateDiceNavMenuBadge(int number);
    }
}
