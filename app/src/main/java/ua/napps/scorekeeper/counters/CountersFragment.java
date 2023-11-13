package ua.napps.scorekeeper.counters;

import static ua.napps.scorekeeper.counters.CountersAdapter.DECREASE_VALUE_CLICK;
import static ua.napps.scorekeeper.counters.CountersAdapter.INCREASE_VALUE_CLICK;
import static ua.napps.scorekeeper.counters.CountersAdapter.MODE_DECREASE_VALUE;
import static ua.napps.scorekeeper.counters.CountersAdapter.MODE_INCREASE_VALUE;
import static ua.napps.scorekeeper.counters.CountersAdapter.MODE_SET_VALUE;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.InputType;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import timber.log.Timber;
import ua.napps.scorekeeper.R;
import ua.napps.scorekeeper.listeners.DragItemListener;
import ua.napps.scorekeeper.log.LogActivity;
import ua.napps.scorekeeper.log.LogEntry;
import ua.napps.scorekeeper.log.LogType;
import ua.napps.scorekeeper.settings.LocalSettings;
import ua.napps.scorekeeper.utils.ColorUtil;
import ua.napps.scorekeeper.utils.Singleton;
import ua.napps.scorekeeper.utils.SpanningLinearLayoutManager;
import ua.napps.scorekeeper.utils.Utilities;
import ua.napps.scorekeeper.utils.ViewUtil;
import ua.napps.scorekeeper.utils.livedata.SingleShotEvent;
import ua.napps.scorekeeper.utils.livedata.VibrateIntent;

public class CountersFragment extends Fragment implements CounterActionCallback, DragItemListener {

    private RecyclerView recyclerView;
    private CountersAdapter countersAdapter;
    private View emptyState;
    private CountersViewModel viewModel;
    private boolean isFirstLoad = true;
    private AlertDialog longClickDialog;
    private int oldListSize;
    private boolean isLongPressTipShowed;
    private ItemTouchHelper itemTouchHelper;
    private Toolbar toolbar;
    private TextView toolbarTitle;
    private int previousTopCounterId;
    private boolean isLowestScoreWins;
    private boolean isSwapPressLogicEnabled;
    private int counterStepDialogMode;
    private int counterStep1;
    private int counterStep2;
    private int counterStep3;
    private int counterStep4;
    private SpanningLinearLayoutManager spanningLinearLayoutManager;
    private LinearLayoutManager linearLayoutManager;
    private Typeface mono;
    private Typeface regular;
    private Vibrator vibrator;

    private final Observer<SingleShotEvent> eventBusObserver = event -> {
        Object intent = event.getValueAndConsume();
        if (intent instanceof VibrateIntent) tryVibrate();
    };

    public CountersFragment() {
        // Required empty public constructor
    }

    public static CountersFragment newInstance() {
        return new CountersFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_counters, container, false);
        toolbar = contentView.findViewById(R.id.toolbar);
        Drawable drawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_more_vertical);
        toolbar.setOverflowIcon(drawable);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        for (int i = 0; i < toolbar.getChildCount(); i++) {
            View view = toolbar.getChildAt(i);
            if (view instanceof TextView) {
                toolbarTitle = (TextView) view;
                break;
            }
        }
        toolbarTitle.setOnClickListener(v -> switchTopLogic());

        spanningLinearLayoutManager = new SpanningLinearLayoutManager(requireContext());
        linearLayoutManager = new LinearLayoutManager(requireContext());

        countersAdapter = new CountersAdapter(this, this);
        recyclerView = contentView.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(spanningLinearLayoutManager);
        recyclerView.setItemAnimator(null);
        recyclerView.setAdapter(countersAdapter);

        emptyState = contentView.findViewById(R.id.empty_state);
        contentView.findViewById(R.id.btn_add_c).setOnClickListener(view -> viewModel.addCounter());

        isLongPressTipShowed = LocalSettings.getLongPressTipShowed();
        isSwapPressLogicEnabled = LocalSettings.isSwapPressLogicEnabled();

        mono = getResources().getFont(R.font.azm);
        regular = getResources().getFont(R.font.o400);

        vibrator = (Vibrator) requireActivity().getSystemService(Context.VIBRATOR_SERVICE);

        observeData();
        return contentView;
    }

    private void switchTopLogic() {
        isLowestScoreWins = !isLowestScoreWins;
        List<Counter> counters = viewModel.getCounters().getValue();
        findAndUpdateTopCounterView(counters);
        showSnack(R.string.message_lowest_wins);
        tryVibrate();
    }

    private void showSnack(@StringRes Integer messageId) {
        Snackbar snack = Snackbar.make(recyclerView, messageId, Snackbar.LENGTH_SHORT);
        snack.setAnchorView(R.id.bottom_navigation);
        snack.show();
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        MenuItem removeItem = menu.findItem(R.id.menu_remove_all);
        final boolean hasCounters = oldListSize > 0;
        if (removeItem != null) {
            removeItem.setEnabled(hasCounters);
        }
        MenuItem clearAllItem = menu.findItem(R.id.menu_reset_all);
        if (clearAllItem != null) {
            clearAllItem.setEnabled(hasCounters);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.counters_menu, menu);

        MenuItem item = menu.getItem(4);
        SpannableString s = new SpannableString(item.getTitle().toString());
        s.setSpan(new ForegroundColorSpan(Color.argb(255, 255, 79, 94)), 0, s.length(), 0);
        item.setTitle(s);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_counter:
                viewModel.addCounter();
                break;
            case R.id.menu_remove_all:
                new MaterialDialog.Builder(requireActivity())
                        .title(R.string.dialog_confirmation_question)
                        .onPositive((dialog, which) -> viewModel.removeAll())
                        .onNegative((dialog, which) -> dialog.dismiss())
                        .typeface(mono, regular)
                        .positiveText(R.string.dialog_yes)
                        .negativeText(R.string.dialog_no)
                        .show();
                break;
            case R.id.menu_reset_all:
                new MaterialDialog.Builder(requireActivity())
                        .title(R.string.dialog_confirmation_question)
                        .onPositive((dialog, which) -> viewModel.resetAll())
                        .onNegative((dialog, which) -> dialog.dismiss())
                        .typeface(mono, regular)
                        .positiveText(R.string.dialog_yes)
                        .negativeText(R.string.dialog_no)
                        .show();
                break;
            case R.id.menu_log:
                LogActivity.start(requireActivity());
                break;
            case R.id.menu_elektu:
                Intent viewIntent =
                        new Intent(Intent.ACTION_VIEW, Uri.parse("https://jbellue.github.io/elektu/"));
                try {
                    startActivity(viewIntent);
                } catch (Exception e) {
                    Toast.makeText(requireContext(), R.string.message_app_not_found, Toast.LENGTH_SHORT).show();
                    Timber.e(e, "Launch web intent");
                }
                break;
        }
        return true;
    }

    private void observeData() {
        CountersViewModelFactory factory = new CountersViewModelFactory(requireActivity().getApplication());
        viewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) factory).get(CountersViewModel.class);
        viewModel.getCounters().observe(getViewLifecycleOwner(), counters -> {
            if (counters != null) {
                final int size = counters.size();

                if (size == 0) {
                    toolbar.setTitle(null);
                    emptyState.setVisibility(View.VISIBLE);
                } else if (size == 1) {
                    toolbar.setTitle(null);
                    emptyState.setVisibility(View.GONE);
                } else { // size >= 2
                    findAndUpdateTopCounterView(counters);
                    emptyState.setVisibility(View.GONE);
                }

                if (oldListSize != size) {
//                    countersAdapter.notifyDataSetChanged();
                    if (size < CountersAdapter.COUNTERS_LAYOUT_THRESHOLD) {
                        if (recyclerView.getLayoutManager().equals(linearLayoutManager)) {
                            recyclerView.setLayoutManager(spanningLinearLayoutManager);
                        }
                    } else {
                        if (recyclerView.getLayoutManager().equals(spanningLinearLayoutManager)) {
                            recyclerView.setLayoutManager(linearLayoutManager);
                        }
                    }
                }

                countersAdapter.setCountersList(counters);

                if (size > oldListSize && oldListSize > 0) {
                    recyclerView.smoothScrollToPosition(size);
                }
                if (isFirstLoad) {
                    isFirstLoad = false;
                    recyclerView.post(() -> {
                                recyclerView.setAdapter(countersAdapter);
                                ItemTouchHelper.Callback callback = new ItemDragHelperCallback(countersAdapter);
                                itemTouchHelper = new ItemTouchHelper(callback);
                                itemTouchHelper.attachToRecyclerView(recyclerView);
                            }
                    );
                } else {
                    // TODO: 28-Mar-20 smells bad. should be better
                    if (oldListSize - 1 == size) {
                        showSnack(R.string.counter_deleted);
                        viewModel.updatePositions();
                    }

                }
                oldListSize = size;
            } else {
                emptyState.setVisibility(View.VISIBLE);
            }
        });
        viewModel.getSnackbarMessage().observe(getViewLifecycleOwner(), (Observer<Integer>) resourceId -> {
            if (resourceId == R.string.counter_added) {
                Snackbar.make(recyclerView, getString(resourceId), Snackbar.LENGTH_SHORT)
                        .setAction(R.string.message_one_more_counter, v -> viewModel.addCounter())
                        .setAnchorView(R.id.bottom_navigation)
                        .show();
            } else {
                showSnack(resourceId);
            }
        });
        viewModel.eventBus.observeForever(eventBusObserver);
    }

    private void findAndUpdateTopCounterView(List<Counter> counters) {
        List<Counter> topCounters = findTopCounters(counters);
        int topSize = topCounters.size();
        if (topSize == 1) {
            Counter top = topCounters.get(0);
            toolbar.setTitle("\uD83C\uDFC5 " + top.getName());
            int counterId = top.getId();
            if (previousTopCounterId != counterId) {
                if (toolbarTitle != null) {
                    ObjectAnimator animator =
                            ObjectAnimator.ofPropertyValuesHolder(toolbarTitle,
                                    PropertyValuesHolder.ofFloat("alpha", 1f, 0f, 1f),
                                    PropertyValuesHolder.ofFloat("rotation", 0f, 360f));
                    animator.setDuration(500);
                    animator.start();
                }
                previousTopCounterId = counterId;
            }
        } else { // At least the first and the second counters have the same value.
            boolean isAllCountersTheSame = topSize == counters.size();
            toolbar.setTitle(isAllCountersTheSame ? null : topSize + " \uD83D\uDCCF");
            ViewUtil.shakeView(toolbarTitle, 2, 2);
            previousTopCounterId = 0;
        }
    }

    @NonNull
    private List<Counter> findTopCounters(List<Counter> counters) {
        if (counters == null || counters.size() < 2) return Collections.emptyList();

        int topValue = !isLowestScoreWins ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        List<Counter> top = new ArrayList<>();

        for (Counter counter : counters) {
            int value = counter.getValue();
            if ((!isLowestScoreWins && (value > topValue)) || (isLowestScoreWins && (value < topValue))) {
                topValue = value;
                top.clear();
                top.add(counter);
            } else if (topValue == value) {
                top.add(counter);
            }
        }
        return top;
    }

    private void showLongPressHint() {
        if (!isLongPressTipShowed) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                Toast.makeText(getActivity(), R.string.message_you_can_use_long_press, Toast.LENGTH_LONG).show();
            } else {
                requireActivity().runOnUiThread(() -> Toast.makeText(getActivity(), R.string.message_you_can_use_long_press, Toast.LENGTH_LONG).show());
            }
            LocalSettings.setLongPressTipShowed();
            isLongPressTipShowed = true;
        }
    }

    @Override
    public void onSingleClick(Counter counter, int position, int mode) {
        if (mode == MODE_SET_VALUE) {
            showCounterStepDialog(counter, position, MODE_INCREASE_VALUE);
            return;
        }
        if (!isSwapPressLogicEnabled) {
            handleSingleStep(counter, mode);
            countersAdapter.notifyItemChanged(position, INCREASE_VALUE_CLICK);
        } else {
            showCounterStepDialog(counter, position, mode);
        }
    }

    private void handleSingleStep(Counter counter, int mode) {
        int step = counter.getStep();
        if (mode == MODE_DECREASE_VALUE) {
            decreaseValue(counter, step);
        } else if (mode == MODE_INCREASE_VALUE) {
            increaseValue(counter, step);
        }
    }

    private void increaseValue(Counter counter, int step) {
        if (step == 0) {
            return;
        }
        Singleton.getInstance().addLogEntry(new LogEntry(counter, LogType.INC, step, counter.getValue()));

        viewModel.increaseCounter(counter, step);
        if (Math.abs(counter.getValue() - counter.getDefaultValue()) > 10) {
            showLongPressHint();
        }
    }

    private void decreaseValue(Counter counter, int step) {
        if (step == 0) {
            return;
        }
        Singleton.getInstance().addLogEntry(new LogEntry(counter, LogType.DEC, step, counter.getValue()));

        viewModel.decreaseCounter(counter, step);

        if (Math.abs(counter.getValue() - counter.getDefaultValue()) > 10) {
            showLongPressHint();
        }
    }

    @Override
    public void onLongClick(Counter counter, int position, int mode) {
        if (mode == MODE_SET_VALUE) {
            showSetValueDialog(counter, position);
            tryVibrate();
        } else {
            if (!isSwapPressLogicEnabled) {
                showCounterStepDialog(counter, position, mode);
            } else {
                handleSingleStep(counter, mode);
            }
        }
    }

    private void showSetValueDialog(Counter counter, int position) {
        final MaterialDialog md = new MaterialDialog.Builder(requireActivity())
                .title(counter.getName() + " [" + counter.getValue()+ "]")
                .titleGravity(GravityEnum.CENTER)
                .inputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED)
                .positiveText(R.string.common_set)
                .typeface(regular, mono)
                .contentColorRes(R.color.textColorPrimary)
                .buttonRippleColorRes(R.color.rippleColor)
                .widgetColorRes(R.color.colorPrimary)
                .positiveColorRes(R.color.colorPrimary)
                .alwaysCallInputCallback()
                .input(R.string.simple_edit_value_hint, 0, false, (dialog, input) -> {
                })
                .showListener(dialogInterface -> {
                    TextView titleTextView = ((MaterialDialog) dialogInterface).getTitleView();
                    if (titleTextView != null) {
                        titleTextView.setLines(1);
                        titleTextView.setEllipsize(TextUtils.TruncateAt.END);
                        titleTextView.setGravity(Gravity.CENTER);
                        titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
                    }
                    EditText inputEditText = ((MaterialDialog) dialogInterface).getInputEditText();
                    if (inputEditText != null) {
                        inputEditText.requestFocus();
                        inputEditText.setGravity(Gravity.CENTER);
                        inputEditText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32);
                    }
                })
                .onPositive((dialog, which) -> {
                    EditText editText = dialog.getInputEditText();
                    if (editText != null) {
                        Integer value = Utilities.parseInt(editText.getText().toString(), counter.getValue());
                        viewModel.modifyCurrentValue(counter, value);
                        countersAdapter.notifyItemChanged(position, INCREASE_VALUE_CLICK);
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
        md.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    private void tryVibrate() {
        if (!isAdded() || !LocalSettings.isCountersVibrate() || vibrator == null) {
            return;
        }

        if (Utilities.hasQ()) {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK));
        } else {
            vibrator.vibrate(100L);
        }
    }

    private void showCounterStepDialog(Counter counter, int position, int mode) {
        counterStepDialogMode = mode;

        final MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(requireActivity());
        final View contentView = LayoutInflater.from(requireActivity()).inflate(R.layout.dialog_counter_step, null, false);

        MaterialButtonToggleGroup signBtnGroup = contentView.findViewById(R.id.group_sign_btn);
        if (counterStepDialogMode == MODE_INCREASE_VALUE) {
            signBtnGroup.check(R.id.btn_add);
        } else if (counterStepDialogMode == MODE_DECREASE_VALUE) {
            signBtnGroup.check(R.id.btn_dec);
        }

        signBtnGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                switch (checkedId) {
                    case R.id.btn_add:
                        counterStepDialogMode = MODE_INCREASE_VALUE;
                        ViewUtil.shakeView(contentView, 1, 2);
                        updateButtonLabels(contentView);
                        break;
                    case R.id.btn_dec:
                        counterStepDialogMode = MODE_DECREASE_VALUE;
                        ViewUtil.shakeView(contentView, 2, 2);
                        updateButtonLabels(contentView);
                        break;
                }
            }
        });

        updateButtonLabels(contentView);
        int color = Color.parseColor(counter.getColor());
        final boolean darkBackground = ColorUtil.isDarkBackground(color);
        int tintColor = darkBackground ? Color.WHITE : 0xDE000000;

        ((TextView) contentView.findViewById(R.id.counter_value)).setText("" + counter.getValue());
        ((TextView) contentView.findViewById(R.id.counter_name)).setText(counter.getName());
        contentView.findViewById(R.id.counter_info_header).setBackgroundColor(color);
        ((TextView) contentView.findViewById(R.id.counter_value)).setTextColor(tintColor);
        ((TextView) contentView.findViewById(R.id.counter_name)).setTextColor(tintColor);

        contentView.findViewById(R.id.btn_one).setOnClickListener(v -> {
            if (counterStepDialogMode == MODE_INCREASE_VALUE) {
                Singleton.getInstance().addLogEntry(new LogEntry(counter, LogType.INC_C, counterStep1, counter.getValue()));

                viewModel.increaseCounter(counter, counterStep1);
                countersAdapter.notifyItemChanged(position, INCREASE_VALUE_CLICK);
            } else if (counterStepDialogMode == MODE_DECREASE_VALUE) {
                Singleton.getInstance().addLogEntry(new LogEntry(counter, LogType.DEC_C, counterStep1, counter.getValue()));

                viewModel.decreaseCounter(counter, counterStep1);
                countersAdapter.notifyItemChanged(position, MODE_DECREASE_VALUE);
            }

            longClickDialog.dismiss();
        });

        contentView.findViewById(R.id.btn_two).setOnClickListener(v -> {
            if (counterStepDialogMode == MODE_INCREASE_VALUE) {
                Singleton.getInstance().addLogEntry(new LogEntry(counter, LogType.INC_C, counterStep2, counter.getValue()));

                viewModel.increaseCounter(counter, counterStep2);
                countersAdapter.notifyItemChanged(position, INCREASE_VALUE_CLICK);
            } else if (counterStepDialogMode == MODE_DECREASE_VALUE) {
                Singleton.getInstance().addLogEntry(new LogEntry(counter, LogType.DEC_C, counterStep2, counter.getValue()));

                viewModel.decreaseCounter(counter, counterStep2);
                countersAdapter.notifyItemChanged(position, DECREASE_VALUE_CLICK);
            }

            longClickDialog.dismiss();
        });

        contentView.findViewById(R.id.btn_three).setOnClickListener(v -> {
            if (counterStepDialogMode == MODE_INCREASE_VALUE) {
                Singleton.getInstance().addLogEntry(new LogEntry(counter, LogType.INC_C, counterStep3, counter.getValue()));

                viewModel.increaseCounter(counter, counterStep3);
                countersAdapter.notifyItemChanged(position, INCREASE_VALUE_CLICK);
            } else if (counterStepDialogMode == MODE_DECREASE_VALUE) {
                Singleton.getInstance().addLogEntry(new LogEntry(counter, LogType.DEC_C, counterStep3, counter.getValue()));

                viewModel.decreaseCounter(counter, counterStep3);
                countersAdapter.notifyItemChanged(position, DECREASE_VALUE_CLICK);
            }

            longClickDialog.dismiss();
        });

        contentView.findViewById(R.id.btn_four).setOnClickListener(v -> {
            if (counterStepDialogMode == MODE_INCREASE_VALUE) {
                Singleton.getInstance().addLogEntry(new LogEntry(counter, LogType.INC_C, counterStep4, counter.getValue()));

                viewModel.increaseCounter(counter, counterStep4);
                countersAdapter.notifyItemChanged(position, INCREASE_VALUE_CLICK);

            } else if (counterStepDialogMode == MODE_DECREASE_VALUE) {
                Singleton.getInstance().addLogEntry(new LogEntry(counter, LogType.DEC_C, counterStep4, counter.getValue()));

                viewModel.decreaseCounter(counter, counterStep4);
                countersAdapter.notifyItemChanged(position, DECREASE_VALUE_CLICK);
            }

            longClickDialog.dismiss();
        });

        final EditText editText = contentView.findViewById(R.id.et_add_custom_value);
        editText.setTransformationMethod(null);
        editText.requestFocus();
        editText.setOnEditorActionListener((textView, actionId, event) -> {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId
                    == EditorInfo.IME_ACTION_DONE)) {
                final String value = editText.getText().toString();
                if (!TextUtils.isEmpty(value)) {
                    int intValue = Utilities.parseInt(value, 0);
                    if (counterStepDialogMode == MODE_INCREASE_VALUE) {
                        Singleton.getInstance().addLogEntry(new LogEntry(counter, LogType.INC_C, intValue, counter.getValue()));

                        viewModel.increaseCounter(counter, intValue);
                        countersAdapter.notifyItemChanged(position, INCREASE_VALUE_CLICK);
                    } else if (counterStepDialogMode == MODE_DECREASE_VALUE) {
                        Singleton.getInstance().addLogEntry(new LogEntry(counter, LogType.DEC_C, intValue, counter.getValue()));

                        viewModel.decreaseCounter(counter, intValue);
                        countersAdapter.notifyItemChanged(position, DECREASE_VALUE_CLICK);
                    }
                }

                longClickDialog.dismiss();
            }
            return false;
        });
        materialAlertDialogBuilder.setView(contentView);

        // Set a custom ShapeAppearanceModel
        MaterialShapeDrawable alertBackground = (MaterialShapeDrawable) materialAlertDialogBuilder.getBackground();
        if (alertBackground != null) {
            alertBackground.setShapeAppearanceModel(
                    alertBackground.getShapeAppearanceModel()
                            .toBuilder()
                            .setAllCorners(CornerFamily.ROUNDED, 24.0f)
                            .build());
        }

        longClickDialog = materialAlertDialogBuilder.create();

        Window window = longClickDialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.CENTER);
            window.setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
        longClickDialog.show();
    }

    private void updateButtonLabels(View contentView) {
        String sign = "";

        if (counterStepDialogMode == MODE_INCREASE_VALUE) {
            sign = "+";
        } else if (counterStepDialogMode == MODE_DECREASE_VALUE) {
            sign = "-";
        }

        ((TextView) contentView.findViewById(R.id.btn_one_text)).setText(sign + counterStep1);
        ((TextView) contentView.findViewById(R.id.btn_two_text)).setText(sign + counterStep2);
        ((TextView) contentView.findViewById(R.id.btn_three_text)).setText(sign + counterStep3);
        ((TextView) contentView.findViewById(R.id.btn_four_text)).setText(sign + counterStep4);
    }

    @Override
    public void onNameClick(Counter counter) {
        final MaterialDialog md = new MaterialDialog.Builder(requireActivity())
                .title(R.string.counter_details_name)
                .inputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT | InputType.TYPE_TEXT_VARIATION_PERSON_NAME)
                .positiveText(R.string.common_set)
                .neutralText(R.string.common_more)
                .typeface(regular, regular)
                .contentColorRes(R.color.textColorPrimary)
                .buttonRippleColorRes(R.color.rippleColor)
                .widgetColorRes(R.color.colorPrimary)
                .positiveColorRes(R.color.colorPrimary)
                .neutralColorRes(R.color.colorPrimary)
                .showListener(dialogInterface -> {
                    TextView titleTextView = ((MaterialDialog) dialogInterface).getContentView();
                    if (titleTextView != null) {
                        titleTextView.setLines(1);
                        titleTextView.setEllipsize(TextUtils.TruncateAt.END);
                        titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
                    }
                    EditText inputEditText = ((MaterialDialog) dialogInterface).getInputEditText();
                    if (inputEditText != null) {
                        inputEditText.requestFocus();
                        inputEditText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32);
                    }
                })
                .input(counter.getName(), null, false, (dialog, input) -> {
                    String value = input.toString().trim();
                    viewModel.modifyName(counter, value);
                })
                .onNeutral((dialog, which) -> onEditClick(counter))
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
        md.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    @Override
    public void onEditClick(Counter counter) {
        EditCounterActivity.start(getActivity(), counter);
    }

    public void scrollToTop() {
        if (recyclerView != null) {
            recyclerView.smoothScrollToPosition(0);
        }
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        itemTouchHelper.startDrag(viewHolder);
    }

    @Override
    public void afterDrag(Counter counter, int fromIndex, int toIndex) {
        viewModel.modifyPosition(counter, fromIndex, toIndex);
    }

    @Override
    public void onResume() {
        super.onResume();
        isSwapPressLogicEnabled = LocalSettings.isSwapPressLogicEnabled();
        counterStep1 = LocalSettings.getCustomCounter(1);
        counterStep2 = LocalSettings.getCustomCounter(2);
        counterStep3 = LocalSettings.getCustomCounter(3);
        counterStep4 = LocalSettings.getCustomCounter(4);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (viewModel != null) {
            viewModel.eventBus.removeObserver(eventBusObserver);
        }
    }
}
