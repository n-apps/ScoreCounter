package ua.napps.scorekeeper.dice;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
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
import android.support.constraint.ConstraintLayout;
import android.support.constraint.Group;
import android.support.transition.TransitionManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ua.com.napps.scorekeeper.R;
import ua.napps.scorekeeper.settings.LocalSettings;
import ua.napps.scorekeeper.utils.AndroidFirebaseAnalytics;

public class DicesFragment extends Fragment {

    private static final String ARG_CURRENT_DICE_ROLL = "ARG_CURRENT_DICE_ROLL";
    private static final String ARG_PREVIOUS_DICE_ROLL = "ARG_PREVIOUS_DICE_ROLL";

    private float accel;
    private float accelCurrent;
    private float accelLast;
    private TextView previousRollTextView;
    private TextView previousRollTextViewLabel;
    private Group emptyStateGroup;
    private TextView diceTextView;
    private SpringForce springForce;
    private DiceViewModel viewModel;
    private int currentDiceVariant;
    private int previousRoll;
    private int currentRoll;
    private OnDiceFragmentInteractionListener listener;
    private ConstraintLayout root;

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
        previousRollTextView = contentView.findViewById(R.id.tv_previous_roll);
        previousRollTextViewLabel = contentView.findViewById(R.id.tv_previous_roll_label);
        emptyStateGroup = contentView.findViewById(R.id.empty_state_group);
        diceTextView = contentView.findViewById(R.id.dice);
        root = contentView.findViewById(R.id.container);
        root.setOnClickListener(v -> {
            viewModel.rollDice();
            Bundle params = new Bundle();
            params.putString(com.google.firebase.analytics.FirebaseAnalytics.Param.CHARACTER, "click");
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

    @Override
    public void onStart() {
        super.onStart();
        AndroidFirebaseAnalytics.trackScreen(getActivity(), "Dice");
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
        diceTextView.setText("" + roll);

        new SpringAnimation(diceTextView, DynamicAnimation.ROTATION)
                .setSpring(springForce)
                .setStartValue(100f)
                .setStartVelocity(100)
                .start();
        new SpringAnimation(diceTextView, DynamicAnimation.SCALE_X)
                .setStartValue(0.8f)
                .setSpring(springForce)
                .start();
        new SpringAnimation(diceTextView, DynamicAnimation.SCALE_Y)
                .setStartValue(0.8f)
                .setSpring(springForce)
                .start();
    }

    @SuppressLint("SetTextI18n")
    private void updateLastRollLabel() {
        TransitionManager.beginDelayedTransition(root);
        emptyStateGroup.setVisibility(View.GONE);
        diceTextView.setVisibility(View.VISIBLE);
        if (previousRoll != 0) {
            previousRollTextViewLabel.setVisibility(View.VISIBLE);
            previousRollTextView.setVisibility(View.VISIBLE);
            previousRollTextView.setText("" + previousRoll);
            ObjectAnimator scaleArrowAnimator =
                    ObjectAnimator.ofPropertyValuesHolder(previousRollTextView,
                            PropertyValuesHolder.ofFloat("scaleX", 1.0f, 1.54f, 1.0f),
                            PropertyValuesHolder.ofFloat("scaleY", 1.0f, 1.54f, 1.0f));
            scaleArrowAnimator.start();
        }
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
                diceTextView.setText("" + currentRoll);
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
                params.putString(com.google.firebase.analytics.FirebaseAnalytics.Param.CHARACTER, "sensor");
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
