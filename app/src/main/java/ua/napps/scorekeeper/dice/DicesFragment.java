package ua.napps.scorekeeper.dice;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.animation.DynamicAnimation;
import android.support.animation.SpringAnimation;
import android.support.animation.SpringForce;
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
import ua.napps.scorekeeper.settings.LocalSettings;
import ua.napps.scorekeeper.utils.AndroidFirebaseAnalytics;

public class DicesFragment extends Fragment {

    private static final String ARG_CURRENT_DICE_ROLL = "ARG_CURRENT_DICE_ROLL";
    private static final String ARG_PREVIOUS_DICE_ROLL = "ARG_PREVIOUS_DICE_ROLL";

    private float accel;
    private float accelCurrent;
    private float accelLast;
    private ImageView dice;
    private TextView previousResultTextView;
    private SpringForce springForce;
    private DiceViewModel viewModel;
    private int currentDiceVariant;
    private int previousRoll;
    private int currentRoll;
    private OnDiceFragmentInteractionListener listener;

    public DicesFragment() {
        // Required empty public constructor
    }

    public static DicesFragment newInstance(int lastDiceRoll, int previousDiceRoll) {
        DicesFragment fragment = new DicesFragment();
        Bundle args = new Bundle(2);
        args.putInt(ARG_CURRENT_DICE_ROLL, lastDiceRoll);
        args.putInt(ARG_PREVIOUS_DICE_ROLL, previousDiceRoll);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentRoll = getArguments().getInt(ARG_CURRENT_DICE_ROLL);
            previousRoll = getArguments().getInt(ARG_PREVIOUS_DICE_ROLL);
        }
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
            AndroidFirebaseAnalytics.logEvent("roll_dice", params);
        });
        return contentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        currentDiceVariant = LocalSettings.getDiceMaxSide();
        springForce = new SpringForce()
                .setDampingRatio(SpringForce.DAMPING_RATIO_LOW_BOUNCY)
                .setStiffness(SpringForce.STIFFNESS_LOW)
                .setFinalPosition(1);
        subscribeUI();
        if (LocalSettings.isShakeToRollEnabled()) {
            initSensorData();
            useSensorLiveData();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnDiceFragmentInteractionListener) {
            listener = (OnDiceFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnDiceFragmentInteractionListener");
        }
    }

    private void initSensorData() {
        accel = 0.00f;
        accelCurrent = SensorManager.GRAVITY_EARTH;
        accelLast = SensorManager.GRAVITY_EARTH;
    }

    private void rollDice(@IntRange(from = 1, to = 100) int roll) {
        updateLastRollLabel();
        previousRoll = roll;
        currentRoll = roll;
        listener.updateCurrentRoll(currentRoll);
        changeDiceDrawable();

        new SpringAnimation(dice, DynamicAnimation.ROTATION)
                .setSpring(springForce)
                .setStartValue(100f)
                .setStartVelocity(100)
                .start();
    }

    private void updateLastRollLabel() {
        if (previousRoll != 0) {
            previousResultTextView.setVisibility(View.VISIBLE);
            previousResultTextView.setText(String.format(getString(R.string.dice_previous_result_label), previousRoll));
        } else {
            previousResultTextView.setVisibility(View.GONE);
        }
    }

    private void changeDiceDrawable() {
        int diceResId = getResources().getIdentifier("dice_digital_" + currentRoll, "drawable", requireActivity().getPackageName());
        dice.setImageResource(diceResId);
    }

    private void subscribeUI() {
        DiceViewModelFactory factory = new DiceViewModelFactory(currentDiceVariant);
        viewModel = ViewModelProviders.of(this, factory).get(DiceViewModel.class);
        final DiceLiveData diceLiveData = viewModel.getDiceLiveData();
        diceLiveData.observe(this, roll -> {
            if (roll != null && roll > 0) {
                rollDice(roll);
            } else if (currentRoll > 0) {
                updateLastRollLabel();
                changeDiceDrawable(); // restored after fragment recreation
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
                AndroidFirebaseAnalytics.logEvent("roll_dice", params);
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public interface OnDiceFragmentInteractionListener {

        void updateCurrentRoll(int number);
    }
}
