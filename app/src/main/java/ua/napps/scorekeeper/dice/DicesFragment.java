package ua.napps.scorekeeper.dice;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
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
import androidx.lifecycle.ViewModelProvider;
import androidx.transition.TransitionManager;

import java.util.ArrayList;

import ua.napps.scorekeeper.R;
import ua.napps.scorekeeper.settings.LocalSettings;
import ua.napps.scorekeeper.utils.SensorHandler;
import ua.napps.scorekeeper.utils.ViewUtil;

public class DicesFragment extends Fragment {

    private static final String ARG_CURRENT_DICE_ROLL = "ARG_CURRENT_DICE_ROLL";

    private int currentRoll;
    private TextView diceVariantInfo;
    private TextView diceHint;
    private Group emptyStateGroup;
    private TextView diceTextView;
    private TextView diceCompositionTextView;
    private SpringForce springForce;
    private DiceViewModel viewModel;
    private ConstraintLayout root;
    private OnDiceFragmentInteractionListener listener;
    private MediaPlayer mp;
    private boolean soundRollEnabled;
    private int maxSide;
    private int diceCount;
    private boolean rollAnimateEnabled;
    private String rollsPrefix;
    private String sumPrefix;
    private SensorHandler sensorHandler;

    public DicesFragment() {
        // Required empty public constructor
    }

    public static DicesFragment newInstance(int lastDiceRoll) {
        DicesFragment fragment = new DicesFragment();
        Bundle args = new Bundle(1);
        args.putInt(ARG_CURRENT_DICE_ROLL, lastDiceRoll);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SensorManager sensorManager = (SensorManager) requireContext().getSystemService(Context.SENSOR_SERVICE);
        sensorHandler = new SensorHandler(sensorManager);
        // Configure sensor handler
        sensorHandler.setShakeThreshold(2.5f); // Increase to reduce accidental shakes
        sensorHandler.setShakeCooldown(1000); // Keep cooldown to prevent frequent triggers

        if (getArguments() != null) {
            currentRoll = getArguments().getInt(ARG_CURRENT_DICE_ROLL);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_dices, container, false);
        diceVariantInfo = contentView.findViewById(R.id.tv_dice_variant_info);
        emptyStateGroup = contentView.findViewById(R.id.empty_state_group);
        diceTextView = contentView.findViewById(R.id.dice);
        diceHint = contentView.findViewById(R.id.tv_dice_hint);
        diceCompositionTextView = contentView.findViewById(R.id.tv_dice_composition);
        rollsPrefix = getString(R.string.dice_roll_prefix);
        sumPrefix = getString(R.string.dice_sum_prefix);
        root = contentView.findViewById(R.id.dices_fragment);
        contentView.findViewById(R.id.iv_edit).setOnClickListener(v -> showBottomSheet());

        maxSide = LocalSettings.getDiceMaxSide();
        diceCount = LocalSettings.getDiceCount();
        diceVariantInfo.setText(diceCount + " × d" + maxSide);
        diceVariantInfo.setOnClickListener(v -> showBottomSheet());

        root.setOnClickListener(v -> viewModel.rollDice());
        root.setOnLongClickListener(v -> {
            showBottomSheet();
            return true;
        });

        soundRollEnabled = LocalSettings.isSoundRollEnabled();
        if (soundRollEnabled) {
            mp = MediaPlayer.create(requireActivity(), R.raw.dice_roll);
        }
        rollAnimateEnabled = LocalSettings.isDiceAnimated();
        if (rollAnimateEnabled) {
            springForce = new SpringForce()
                    .setDampingRatio(SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY)
                    .setStiffness(SpringForce.STIFFNESS_VERY_LOW)
                    .setFinalPosition(1);
        }
        observeData();

        // Enable shake detection if enabled in settings
        if (LocalSettings.isShakeToRollEnabled()) {
            sensorHandler.enable();
            sensorHandler.getSensorLiveData().observe(getViewLifecycleOwner(), event -> {
                if (event != null) {
                    viewModel.rollDice(); // Perform dice roll on shake event
                }
            });
        }

        return contentView;
    }

    private void showBottomSheet() {
        DiceBottomSheetFragment bottomSheet = new DiceBottomSheetFragment();
        bottomSheet.show(getParentFragmentManager(), "DiceBottomSheetFragment");
        bottomSheet.setOnDismissListener(d -> updateOnDismiss());
    }

    private void updateOnDismiss() {
        int ms = LocalSettings.getDiceMaxSide();
        int dc = LocalSettings.getDiceCount();

        if (diceCount != dc) {
            ViewUtil.shakeView(diceVariantInfo, 4, 0);
            diceCount = dc;
            viewModel.setDiceCount(diceCount);
            diceVariantInfo.setText(diceCount + " × d" + maxSide);
        }
        if (maxSide != ms) {
            ViewUtil.shakeView(diceVariantInfo, 4, 0);
            maxSide = ms;
            viewModel.setDiceMaxSide(maxSide);
            diceVariantInfo.setText(diceCount + " × d" + maxSide);
        }
        soundRollEnabled = LocalSettings.isSoundRollEnabled();
        rollAnimateEnabled = LocalSettings.isDiceAnimated();

        if (LocalSettings.isShakeToRollEnabled()) {
            sensorHandler.enable();
        } else {
            sensorHandler.disable();
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnDiceFragmentInteractionListener) {
            listener = (OnDiceFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context + " must implement OnDiceFragmentInteractionListener");
        }
    }

    private void rollDice(@IntRange(from = 1, to = 100) int roll, @NonNull ArrayList<Integer> rolls, boolean skipAnimation) {
        TransitionManager.beginDelayedTransition(root);
        diceTextView.setVisibility(View.VISIBLE);
        diceHint.setVisibility(View.VISIBLE);
        emptyStateGroup.setVisibility(View.GONE);
        diceTextView.setText("");
        currentRoll = roll;
        listener.updateCurrentRoll(currentRoll);

        if (!skipAnimation && rollAnimateEnabled) {
            if (springForce == null) {
                springForce = new SpringForce()
                        .setDampingRatio(SpringForce.DAMPING_RATIO_LOW_BOUNCY)
                        .setStiffness(SpringForce.STIFFNESS_LOW)
                        .setFinalPosition(1);
            }
            new SpringAnimation(diceTextView, DynamicAnimation.ROTATION)
                    .setSpring(springForce)
                    .setStartValue(100f)
                    .setStartVelocity(200f)
                    .start();
            new SpringAnimation(diceTextView, DynamicAnimation.SCALE_X)
                    .setStartValue(-0.5f)
                    .setStartVelocity(2f)
                    .setSpring(springForce)
                    .start();
            new SpringAnimation(diceTextView, DynamicAnimation.SCALE_Y)
                    .setStartValue(-0.5f)
                    .setStartVelocity(2f)
                    .setSpring(springForce)
                    .addEndListener((animation, canceled, value, velocity) -> {
                        diceTextView.setText("" + roll);
                        updateCompositionLabel(rolls);
                        if (soundRollEnabled && mp != null) {
                            mp.start();
                        }
                    })
                    .start();
        } else {
            diceTextView.setText("?");
            ViewUtil.shakeView(diceTextView, 2, 3);
            diceTextView.setText("" + roll);
            updateCompositionLabel(rolls);
        }
    }

    private void updateCompositionLabel(@NonNull ArrayList<Integer> rolls) {
        if (rolls.isEmpty()) return;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<b>");
        stringBuilder.append(rollsPrefix);
        stringBuilder.append(" </b>");

        long sum = 0;
        for (int i = 0; i < rolls.size(); i++) {
            if (i != 0) {
                stringBuilder.append(", ");
            }
            stringBuilder.append(rolls.get(i));

            sum += rolls.get(i);
        }

        stringBuilder.append("<br><b>");
        stringBuilder.append(sumPrefix);
        stringBuilder.append(" </b>");
        stringBuilder.append(sum);
        animateResults(diceCompositionTextView, Html.fromHtml(stringBuilder.toString(), Html.FROM_HTML_MODE_LEGACY), 150);
    }

    public static void animateResults(final TextView resultsText, final CharSequence newText, final int animLength) {
        if (resultsText.getAnimation() == null || resultsText.getAnimation().hasEnded()) {
            ObjectAnimator animX = ObjectAnimator.ofFloat(resultsText, "scaleX", 0.75f);
            ObjectAnimator animY = ObjectAnimator.ofFloat(resultsText, "scaleY", 0.75f);
            ObjectAnimator fadeOut = ObjectAnimator.ofFloat(resultsText, "alpha", 0.0f);
            AnimatorSet shrink = new AnimatorSet();
            shrink.playTogether(animX, animY, fadeOut);
            shrink.setDuration(animLength);
            shrink.setInterpolator(new AccelerateInterpolator());
            shrink.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(@NonNull Animator animation) {
                }

                @Override
                public void onAnimationEnd(@NonNull Animator animation) {
                    resultsText.setText(newText);

                    ObjectAnimator animX = ObjectAnimator.ofFloat(resultsText, "scaleX", 1.0f);
                    ObjectAnimator animY = ObjectAnimator.ofFloat(resultsText, "scaleY", 1.0f);
                    ObjectAnimator fadeIn = ObjectAnimator.ofFloat(resultsText, "alpha", 1.0f);
                    AnimatorSet grow = new AnimatorSet();
                    grow.playTogether(animX, animY, fadeIn);
                    grow.setDuration(animLength);
                    grow.setInterpolator(new AnticipateOvershootInterpolator());
                    grow.start();
                }

                @Override
                public void onAnimationCancel(@NonNull Animator animation) {
                }

                @Override
                public void onAnimationRepeat(@NonNull Animator animation) {
                }
            });
            shrink.start();
        }
    }

    private void observeData() {
        DiceViewModelFactory factory = new DiceViewModelFactory(maxSide, diceCount);
        viewModel = new ViewModelProvider(this, factory).get(DiceViewModel.class);
        final DiceLiveData diceLiveData = viewModel.getDiceLiveData();
        diceLiveData.observe(getViewLifecycleOwner(), roll -> {
            if (roll != null && roll > 0) {
                rollDice(roll, diceLiveData.getRolls(), false);
            } else if (currentRoll > 0) {
                rollDice(currentRoll, new ArrayList<>(), true);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (sensorHandler != null) {
            sensorHandler.disable(); // Clean up sensor registration
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
        if (mp != null) {
            mp.stop();
            mp.release();
            mp = null;
        }
    }
}
