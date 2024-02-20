package ua.napps.scorekeeper.dice;

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
import androidx.lifecycle.ViewModelProvider;
import androidx.transition.TransitionManager;

import java.util.ArrayList;

import ua.napps.scorekeeper.R;
import ua.napps.scorekeeper.settings.LocalSettings;
import ua.napps.scorekeeper.utils.ViewUtil;

public class DicesFragment extends Fragment {

    private static final String ARG_CURRENT_DICE_ROLL = "ARG_CURRENT_DICE_ROLL";
    private static final String ARG_PREVIOUS_DICE_ROLL = "ARG_PREVIOUS_DICE_ROLL";

    private float myAccel = 0f; // acceleration apart from gravity
    private float myAccelCurrent = SensorManager.GRAVITY_EARTH; // current acceleration including gravity
    private float myAccelLast = SensorManager.GRAVITY_EARTH; // last acceleration including gravity
    private long myLastShake;
    private int previousRoll;
    private int currentRoll;
    private TextView previousRollTextView;
    private TextView diceVariantInfo;
    private TextView previousRollTextViewLabel;
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
    private TextView emojiTextView;

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
        diceCompositionTextView = contentView.findViewById(R.id.tv_dice_composition);
        emojiTextView = contentView.findViewById(R.id.tv_emoji);
        root = contentView.findViewById(R.id.dices_fragment);
        contentView.findViewById(R.id.iv_dice_menu).setOnClickListener(v -> showBottomSheet());

        maxSide = LocalSettings.getDiceMaxSide();
        diceCount = LocalSettings.getDiceCount();
        diceVariantInfo.setText(diceCount + "d" + maxSide);

        root.setOnClickListener(v -> viewModel.rollDice());
        root.setOnLongClickListener(v -> {
            showBottomSheet();
            return true;
        });

        diceVariantInfo.setOnClickListener(v -> showBottomSheet());

        soundRollEnabled = LocalSettings.isSoundRollEnabled();
        if (soundRollEnabled) {
            mp = MediaPlayer.create(requireActivity(), R.raw.dice_roll);
        }

        springForce = new SpringForce()
                .setDampingRatio(SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY)
                .setStiffness(SpringForce.STIFFNESS_VERY_LOW)
                .setFinalPosition(1);

        observeData();
        if (LocalSettings.isShakeToRollEnabled()) {
            initSensorData();
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
            diceVariantInfo.setText(diceCount + "d" + maxSide);

            if (diceCount < 2) {
                diceCompositionTextView.setVisibility(View.INVISIBLE);
            }
        }
        if (maxSide != ms) {
            ViewUtil.shakeView(diceVariantInfo, 4, 0);
            maxSide = ms;
            viewModel.setDiceMaxSide(maxSide);
            diceVariantInfo.setText(diceCount + "d" + maxSide);
        }
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
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnDiceFragmentInteractionListener) {
            listener = (OnDiceFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnDiceFragmentInteractionListener");
        }
    }

    private void initSensorData() {
        viewModel.getSensorLiveData(getActivity()).observe(getViewLifecycleOwner(), se -> {
            if (se == null) {
                return;
            }

            float x = se.values[0];
            float y = se.values[1];
            float z = se.values[2];
            myAccelLast = myAccelCurrent;
            myAccelCurrent = (float) Math.sqrt((double) (x * x + y * y + z * z));
            float delta = myAccelCurrent - myAccelLast;
            myAccel = myAccel * 0.9f + delta; // perform low-cut filter

            if (myAccel > 1.8 && viewModel != null && (System.currentTimeMillis() - myLastShake > 1000)) {
                myLastShake = System.currentTimeMillis();
                viewModel.rollDice();
            }
        });
    }

    private void rollDice(@IntRange(from = 1, to = 100) int roll, ArrayList<Integer> rolls) {
        if (soundRollEnabled && mp != null) {
            mp.start();
        }
        updateCompositionLabel(rolls, true);
        diceTextView.setText("");
        updateLastRoll(roll);
        currentRoll = roll;
        listener.updateCurrentRoll(currentRoll);

        new SpringAnimation(diceTextView, DynamicAnimation.ROTATION)
                .setSpring(springForce)
                .setStartValue(200f)
                .setStartVelocity(500f)
                .start();
        new SpringAnimation(diceTextView, DynamicAnimation.SCALE_X)
                .setStartValue(-1.0f)
                .setStartVelocity(10f)
                .setSpring(springForce)
                .start();
        new SpringAnimation(diceTextView, DynamicAnimation.SCALE_Y)
                .setStartValue(-1.0f)
                .setStartVelocity(10f)
                .setSpring(springForce)
                .addEndListener((animation, canceled, value, velocity) -> {
                    diceTextView.setText("" + roll);
                    updateCompositionLabel(rolls, false);
                })
                .start();
    }

    private void updateCompositionLabel(ArrayList<Integer> rolls, boolean rollInProgress) {
        int rollsize = rolls.size();
        if (rollsize == 1) {
            diceCompositionTextView.setText("");
            diceCompositionTextView.setVisibility(View.INVISIBLE);
            emojiTextView.setText(rollInProgress ? " " : getRandomEmoji());
        } else if (rollsize >= 9) {
            diceCompositionTextView.setText("\uD83E\uDD2F \uD83E\uDD2F \uD83E\uDD2F");
            diceCompositionTextView.setVisibility(View.VISIBLE);
        } else {
            StringBuilder composition = new StringBuilder();
            composition.append(rollInProgress ? "?: " : currentRoll + ": ");

            for (int i = 0; i < rollsize; i++) {
                composition.append(rollInProgress ? "×" : rolls.get(i));
                if (i < (rollsize - 1)) {
                    composition.append(" + ");
                }
            }
            diceCompositionTextView.setText(composition);
            diceCompositionTextView.setVisibility(View.VISIBLE);
            StringBuilder emojis = new StringBuilder();
            for (int i = 0; i < rollsize; i++) {
                emojis.append(rollInProgress ? " " : getRandomEmoji());
            }
            emojiTextView.setText(emojis);
        }

    }
    private String getRandomEmoji() {
        int[] unicodes = new int[] {
                // Emoticons
                0x1F601,
                0x1F602,
                0x1F603,
                0x1F604,
                0x1F605,
                0x1F606,
                0x1F609,
                0x1F60A,
                0x1F60B,
                0x1F60C,
                0x1F60D,
                0x1F60F,
                0x1F612,
                0x1F613,
                0x1F614,
                0x1F616,
                0x1F618,
                0x1F61A,
                0x1F61C,
                0x1F61D,
                0x1F61E,
                0x1F620,
                0x1F621,
                0x1F622,
                0x1F623,
                0x1F624,
                0x1F625,
                0x1F628,
                0x1F629,
                0x1F62A,
                0x1F62B,
                0x1F62D,
                0x1F630,
                0x1F631,
                0x1F632,
                0x1F633,
                0x1F635,
                0x1F637,
                0x1F638,
                0x1F639,
                0x1F63A,
                0x1F63B,
                0x1F63C,
                0x1F63D,
                0x1F63E,
                0x1F63F,
                0x1F640,
                0x1F645,
                0x1F646,
                0x1F647,
                0x1F648,
                0x1F649,
                0x1F64A,
                0x1F64B,
                0x1F64C,
                0x1F64D,
                0x1F64E,
                0x1F64F,

                // Uncategorized
                0x1F40C,
                0x1F40D,
                0x1F40E,
                0x1F411,
                0x1F412,
                0x1F414,
                0x1F418,
                0x1F419,
                0x1F41A,
                0x1F41B,
                0x1F41C,
                0x1F41D,
                0x1F41E,
                0x1F41F,
                0x1F420,
                0x1F421,
                0x1F422,
                0x1F423,
                0x1F424,
                0x1F425,
                0x1F426,
                0x1F427,
                0x1F428,
        };
        int randomIndex = (int) (Math.random() * unicodes.length);
        int unicode = unicodes[randomIndex];
        String emoji = getEmijoByUnicode(unicode);
        return emoji;
    }
    private String getEmijoByUnicode(int unicode) {
        return new String(Character.toChars(unicode));
    }

    @SuppressLint("SetTextI18n")
    private void updateLastRoll(int newLastRoll) {

        TransitionManager.beginDelayedTransition(root);
        emptyStateGroup.setVisibility(View.GONE);
        diceTextView.setVisibility(View.VISIBLE);
        if (previousRoll != 0) {
            previousRollTextViewLabel.setVisibility(View.VISIBLE);
            previousRollTextView.setVisibility(View.VISIBLE);
            previousRollTextView.setText("" + previousRoll);
            ViewUtil.shakeView(previousRollTextView, 4, 0);
        }

        previousRoll = newLastRoll;
    }

    private void observeData() {
        DiceViewModelFactory factory = new DiceViewModelFactory(maxSide, diceCount);
        viewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) factory).get(DiceViewModel.class);
        final DiceLiveData diceLiveData = viewModel.getDiceLiveData();
        diceLiveData.observe(getViewLifecycleOwner(), roll -> {
            if (roll != null && roll > 0) {
                rollDice(roll, diceLiveData.getRolls());
            } else if (currentRoll > 0) {
                updateLastRoll(currentRoll);
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
