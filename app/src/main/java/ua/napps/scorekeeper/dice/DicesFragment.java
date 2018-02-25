package ua.napps.scorekeeper.dice;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.animation.DynamicAnimation;
import android.support.animation.SpringAnimation;
import android.support.animation.SpringForce;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

import ua.com.napps.scorekeeper.R;
import ua.napps.scorekeeper.app.Constants;
import ua.napps.scorekeeper.storage.TinyDB;
import ua.napps.scorekeeper.utils.AndroidFirebaseAnalytics;


public class DicesFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private float accel;
    private float accelCurrent;
    private float accelLast;
    private ImageView dice;
    private TextView previousResultTextView;
    private SpringAnimation springAnimation;
    private SpringForce springForce;
    private DiceViewModel viewModel;
    private TinyDB settingsDB;
    private int currentDiceVariant;

    private OnDiceFragmentInteractionListener diceFragmentInteractionListener;
    private boolean shakeToRoll;

    public DicesFragment() {
        // Required empty public constructor
    }

    public static DicesFragment newInstance() {
        return new DicesFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
        settingsDB = new TinyDB(getContext());
        currentDiceVariant = settingsDB.getInt(Constants.SETTINGS_DICE_VARIANT, 6);
        shakeToRoll = settingsDB.getBoolean(Constants.SETTINGS_SHAKE_TO_ROLL, true);
        settingsDB.registerOnSharedPreferenceChangeListener(this);
        springForce = new SpringForce()
                .setDampingRatio(SpringForce.DAMPING_RATIO_LOW_BOUNCY)
                .setStiffness(SpringForce.STIFFNESS_LOW)
                .setFinalPosition(1);
        subscribeUI();
        if (shakeToRoll) {
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
            throw new RuntimeException(context.toString() + " must implement OnDiceFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        diceFragmentInteractionListener = null;
        settingsDB.unregisterOnSharedPreferenceChangeListener(this);
        settingsDB = null;
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

    private void rollDice(@IntRange(from = 0, to = 100) int rollResult, int previousResult) {
        if (previousResult == 0) {
            previousResultTextView.setVisibility(View.GONE);
        } else {
            previousResultTextView.setVisibility(View.VISIBLE);
            previousResultTextView
                    .setText(String.format(getString(R.string.dice_previous_result_label), previousResult));
            if (springAnimation != null) {
                springAnimation.cancel();
            }
        }

        @DrawableRes int diceResId = getResources().getIdentifier("dice_digital_" + rollResult, "drawable", getActivity().getPackageName());

        dice.setImageResource(diceResId);
        diceFragmentInteractionListener.updateDiceNavMenuBadge(rollResult);
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

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
        switch (key) {
            case Constants.SETTINGS_DICE_VARIANT:
                currentDiceVariant = settingsDB.getInt(Constants.SETTINGS_DICE_VARIANT, 6);
                viewModel.updateDiceVariant(currentDiceVariant);
                break;
            case Constants.SETTINGS_SHAKE_TO_ROLL:
                shakeToRoll = settingsDB.getBoolean(Constants.SETTINGS_SHAKE_TO_ROLL, true);
                if (!shakeToRoll) {
                    viewModel.disableSensor();
                } else {
                    viewModel.enableLiveSensor(getActivity());
                }
                break;
        }
    }

    public interface OnDiceFragmentInteractionListener {
        void updateDiceNavMenuBadge(int number);
    }
}
