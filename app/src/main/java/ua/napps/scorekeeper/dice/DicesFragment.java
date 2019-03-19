package ua.napps.scorekeeper.dice;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Group;
import androidx.core.content.ContextCompat;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.transition.TransitionManager;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.andrefrsousa.superbottomsheet.SuperBottomSheetFragment;

import org.jetbrains.annotations.NotNull;

import ua.com.napps.scorekeeper.R;
import ua.napps.scorekeeper.settings.LocalSettings;
import ua.napps.scorekeeper.utils.AndroidFirebaseAnalytics;
import ua.napps.scorekeeper.utils.Utilities;

public class DicesFragment extends Fragment {

    private static final String ARG_CURRENT_DICE_ROLL = "ARG_CURRENT_DICE_ROLL";
    private static final String ARG_PREVIOUS_DICE_ROLL = "ARG_PREVIOUS_DICE_ROLL";

    private float accel;
    private float accelCurrent;
    private float accelLast;
    private TextView previousRollTextView;
    private TextView diceVariantInfo;
    private TextView previousRollTextViewLabel;
    private Group emptyStateGroup;
    private TextView diceTextView;
    private SpringForce springForce;
    private DiceViewModel viewModel;
    private int previousRoll;
    private int currentRoll;
    private OnDiceFragmentInteractionListener listener;
    private ConstraintLayout root;
    private DiceBottomSheetFragment diceBottomSheetFragment;

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
        root = contentView.findViewById(R.id.container);
        root.setOnClickListener(v -> {
            viewModel.rollDice();
            Bundle params = new Bundle();
            params.putString(com.google.firebase.analytics.FirebaseAnalytics.Param.CHARACTER, "click");
            AndroidFirebaseAnalytics.logEvent("roll_dice", params);
        });
        ImageView diceMenu = contentView.findViewById(R.id.iv_dice_menu);
        diceMenu.setOnClickListener(v -> {
            AndroidFirebaseAnalytics.logEvent("dice_menu_click");
            diceBottomSheetFragment = new DiceBottomSheetFragment();
            diceBottomSheetFragment.show(getFragmentManager(), "DiceBottomSheetFragment");
            diceBottomSheetFragment.setOnDismissListener(d -> updateDiceVariant());
        });

        int maxSide = LocalSettings.getDiceMaxSide();
        diceVariantInfo.setText("d" + maxSide);

        return contentView;
    }

    private void updateDiceVariant() {
        int maxSide = LocalSettings.getDiceMaxSide();
        diceVariantInfo.setText("d" + maxSide);
        viewModel.setDiceVariant(maxSide);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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

    private void useSensorLiveData() {
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

    static class DiceBottomSheetFragment extends SuperBottomSheetFragment implements View.OnClickListener {

        private DialogInterface.OnDismissListener onDismissListener;
        private int diceMaxSide;
        private ToggleButton diceSix, diceEight, diceTwenty, diceCustom;

        @org.jetbrains.annotations.Nullable
        @Override
        public View onCreateView(@NotNull LayoutInflater inflater, @org.jetbrains.annotations.Nullable ViewGroup container, @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
            super.onCreateView(inflater, container, savedInstanceState);
            View contentView = inflater.inflate(R.layout.fragment_dice_sheet, container, false);
            SwitchCompat shakeToRoll = contentView.findViewById(R.id.sw_shake_roll);
            shakeToRoll.setOnCheckedChangeListener((b, isChecked) -> LocalSettings.saveShakeToRoll(isChecked));
            shakeToRoll.setChecked(LocalSettings.isShakeToRollEnabled());

            diceMaxSide = LocalSettings.getDiceMaxSide();
            diceSix = contentView.findViewById(R.id.tb_dice_6);
            diceEight = contentView.findViewById(R.id.tb_dice_8);
            diceTwenty = contentView.findViewById(R.id.tb_dice_20);
            diceCustom = contentView.findViewById(R.id.tb_dice_x);

            diceSix.setOnClickListener(this);
            diceEight.setOnClickListener(this);
            diceTwenty.setOnClickListener(this);
            diceCustom.setOnClickListener(this);
            refreshDices(false);
            return contentView;
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            super.onDismiss(dialog);

            if (onDismissListener != null) {
                onDismissListener.onDismiss(dialog);
            }
        }

        void setOnDismissListener(DialogInterface.OnDismissListener listener) {
            onDismissListener = listener;
        }

        @Override
        public void onStart() {
            super.onStart();
            if (getDialog() != null && getDialog().getWindow() != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                int oldFlags = getDialog().getWindow().getDecorView().getSystemUiVisibility();
                // Apply the state flags in priority order
                int newFlags = oldFlags;
                if (LocalSettings.isLightTheme()) {
                    newFlags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                    getDialog().getWindow().setNavigationBarColor(Color.WHITE);
                } else {
                    newFlags &= ~(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
                    getDialog().getWindow().setNavigationBarColor(ContextCompat.getColor(requireActivity(), R.color.dark_theme_bg));
                }
                if (newFlags != oldFlags) {
                    getDialog().getWindow().getDecorView().setSystemUiVisibility(newFlags);
                }
            }
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.tb_dice_6:
                    diceMaxSide = 6;
                    refreshDices(true);
                    break;
                case R.id.tb_dice_8:
                    diceMaxSide = 8;
                    refreshDices(true);
                    break;
                case R.id.tb_dice_20:
                    diceMaxSide = 20;
                    refreshDices(true);
                    break;
                case R.id.tb_dice_x:
                    final MaterialDialog md = new MaterialDialog.Builder(requireActivity())
                            .content(R.string.dialog_custom_dice_title)
                            .inputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED)
                            .positiveText(R.string.common_set)
                            .negativeColorRes(R.color.primaryColor)
                            .negativeText(R.string.common_cancel)
                            .alwaysCallInputCallback()
                            .dismissListener(d -> refreshDices(false))
                            .input(getString(R.string.dialog_custom_dice_hint), null, false,
                                    (dialog, input) -> {
                                        int parseInt = Utilities.parseInt(input.toString());
                                        if (parseInt <= 100 && parseInt > 1) {
                                            dialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);
                                        } else {
                                            dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
                                        }
                                    })
                            .onPositive((dialog, which) -> {
                                EditText editText = dialog.getInputEditText();
                                if (editText != null) {
                                    Integer side = Utilities.parseInt(editText.getText().toString());
                                    if (side > 0) {
                                        diceMaxSide = side;
                                        refreshDices(true);
                                    }
                                    dialog.dismiss();
                                }
                            })
                            .build();
                    EditText editText = md.getInputEditText();
                    if (editText != null) {
                        editText.setOnEditorActionListener((textView, actionId, event) -> {
                            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                                View positiveButton = md.getActionButton(DialogAction.POSITIVE);
                                positiveButton.callOnClick();
                            }
                            return false;
                        });
                    }
                    md.show();
                    break;
            }
        }

        private void refreshDices(boolean storeInDB) {
            if (storeInDB && diceMaxSide <= 100) {
                LocalSettings.saveDiceMaxSide(diceMaxSide);
                onDismiss(getDialog());
                return;
            }
            switch (diceMaxSide) {
                case 6:
                    diceSix.setChecked(true);
                    diceEight.setChecked(false);
                    diceTwenty.setChecked(false);
                    diceCustom.setChecked(false);
                    diceCustom.setTextOff("?");
                    diceCustom.setTextOn("?");
                    break;
                case 8:
                    diceSix.setChecked(false);
                    diceEight.setChecked(true);
                    diceTwenty.setChecked(false);
                    diceCustom.setChecked(false);
                    diceCustom.setTextOff("?");
                    diceCustom.setTextOn("?");
                    break;
                case 20:
                    diceSix.setChecked(false);
                    diceEight.setChecked(false);
                    diceTwenty.setChecked(true);
                    diceCustom.setChecked(false);
                    diceCustom.setTextOff("?");
                    diceCustom.setTextOn("?");
                    diceCustom.setText("?");
                    break;
                default:
                    diceSix.setChecked(false);
                    diceEight.setChecked(false);
                    diceTwenty.setChecked(false);
                    diceCustom.setChecked(true);
                    String label = "" + diceMaxSide;
                    diceCustom.setTextOff(label);
                    diceCustom.setTextOn(label);
                    diceCustom.setText(label);
                    break;
            }
        }
    }
}
