package ua.napps.scorekeeper.dice;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Group;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.transition.TransitionManager;

import com.google.firebase.analytics.FirebaseAnalytics;

import ua.napps.scorekeeper.R;
import ua.napps.scorekeeper.settings.LocalSettings;
import ua.napps.scorekeeper.utils.AndroidFirebaseAnalytics;

public class DicesFragment extends Fragment {

    private static final String ARG_CURRENT_DICE_ROLL = "ARG_CURRENT_DICE_ROLL";
    private static final String ARG_PREVIOUS_DICE_ROLL = "ARG_PREVIOUS_DICE_ROLL";

    private float accel;
    private float accelCurrent;
    private float accelLast;
    private int previousRoll;
    private int currentRoll;
    private TextView previousRollTextView;
    private TextView diceVariantInfo;
    private TextView previousRollTextViewLabel;
    private Group emptyStateGroup;
    private TextView diceTextView;
    private SpringForce springForce;
    private DiceViewModel viewModel;
    private ConstraintLayout root;
    private OnDiceFragmentInteractionListener listener;
    private MediaPlayer mp;
    private boolean soundRollEnabled;
    private int maxSide;
    private TextView diceHintTextView;

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
        diceVariantInfo = contentView.findViewById(R.id.tv_dice_variant_info);
        emptyStateGroup = contentView.findViewById(R.id.empty_state_group);
        diceTextView = contentView.findViewById(R.id.dice);
        diceHintTextView = contentView.findViewById(R.id.tv_dice_hint);
        root = contentView.findViewById(R.id.container);
        contentView.findViewById(R.id.iv_dice_menu).setOnClickListener(v -> {
            showBottomSheet();
        });

        maxSide = LocalSettings.getDiceMaxSide();
        diceVariantInfo.setText("d" + maxSide);

        root.setOnClickListener(v -> {
            viewModel.rollDice();
            Bundle params = new Bundle();
            params.putString(FirebaseAnalytics.Param.CHARACTER, "by_touch");
            params.putString(FirebaseAnalytics.Param.ITEM_VARIANT, "d" + maxSide);
            AndroidFirebaseAnalytics.logEvent("DiceScreenDiceRolled", params);
        });
        root.setOnLongClickListener(v -> {
            showBottomSheet();
            return true;
        });

        diceVariantInfo.setOnClickListener(v -> showBottomSheet());

        soundRollEnabled = LocalSettings.isSoundRollEnabled();
        if (soundRollEnabled) {
            mp = MediaPlayer.create(requireActivity(), R.raw.dice_roll);
        }


        return contentView;
    }

    private void showBottomSheet() {
        DiceBottomSheetFragment bottomSheet = new DiceBottomSheetFragment();
        bottomSheet.show(requireFragmentManager(), "DiceBottomSheetFragment");
        bottomSheet.setOnDismissListener(d -> updateOnDismiss());
    }

    private void updateOnDismiss() {
        maxSide = LocalSettings.getDiceMaxSide();
        diceVariantInfo.setText("d" + maxSide);
        viewModel.setDiceVariant(maxSide);
        soundRollEnabled = LocalSettings.isSoundRollEnabled();
        if (!LocalSettings.isShakeToRollEnabled()) {
            viewModel.getSensorLiveData(getActivity()).removeObservers(getViewLifecycleOwner());
        } else {
            if (viewModel.getSensorLiveData(getActivity()) == null) {
                initSensorData();
            }
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        springForce = new SpringForce()
                .setDampingRatio(SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY)
                .setStiffness(SpringForce.STIFFNESS_VERY_LOW)
                .setFinalPosition(1);
        subscribeUI();
        if (LocalSettings.isShakeToRollEnabled()) {
            initSensorData();
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnDiceFragmentInteractionListener) {
            listener = (OnDiceFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnDiceFragmentInteractionListener");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        AndroidFirebaseAnalytics.trackScreen(requireActivity(), "Dice", getClass().getSimpleName());
    }

    private void initSensorData() {
        accel = 0.00f;
        accelCurrent = SensorManager.GRAVITY_EARTH;
        accelLast = SensorManager.GRAVITY_EARTH;
        viewModel.getSensorLiveData(getActivity()).observe(getViewLifecycleOwner(), se -> {
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
                params.putString(FirebaseAnalytics.Param.ITEM_VARIANT, "d" + maxSide);
                params.putString(FirebaseAnalytics.Param.CHARACTER, "by_shake");
                AndroidFirebaseAnalytics.logEvent("DiceScreenDiceRolled", params);
            }
        });
    }

    private void rollDice(@IntRange(from = 1, to = 100) int roll) {
        if (soundRollEnabled && mp != null) {
            mp.start();
        }

        updateLastRollLabel();
        previousRoll = roll;
        currentRoll = roll;
        listener.updateCurrentRoll(currentRoll);
        diceTextView.setText("" + roll);

        new SpringAnimation(diceTextView, DynamicAnimation.ROTATION)
                .setSpring(springForce)
                .setStartValue(1000f)
                .setStartVelocity(1000)
                .start();
        new SpringAnimation(diceTextView, DynamicAnimation.SCALE_X)
                .setStartValue(0.5f)
                .setSpring(springForce)
                .start();
        new SpringAnimation(diceTextView, DynamicAnimation.ALPHA)
                .setStartValue(0.1f)
                .setSpring(springForce)
                .start();
        new SpringAnimation(diceTextView, DynamicAnimation.SCALE_Y)
                .setStartValue(0.5f)
                .setSpring(springForce)
                .start();
    }

    @SuppressLint("SetTextI18n")
    private void updateLastRollLabel() {
        TransitionManager.beginDelayedTransition(root);
        emptyStateGroup.setVisibility(View.GONE);
        diceTextView.setVisibility(View.VISIBLE);
        diceHintTextView.setVisibility(View.VISIBLE);
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
        DiceViewModelFactory factory = new DiceViewModelFactory(LocalSettings.getDiceMaxSide());
        viewModel = ViewModelProviders.of(this, factory).get(DiceViewModel.class);
        final DiceLiveData diceLiveData = viewModel.getDiceLiveData();
        diceLiveData.observe(getViewLifecycleOwner(), roll -> {
            if (roll != null && roll > 0) {
                rollDice(roll);
            } else if (currentRoll > 0) {
                updateLastRollLabel();
                diceTextView.setText("" + currentRoll);
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
        if (mp != null) {
            mp.release();
        }
    }

}
