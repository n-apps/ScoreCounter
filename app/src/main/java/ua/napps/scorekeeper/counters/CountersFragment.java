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
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
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
import com.bitvale.switcher.SwitcherX;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ua.napps.scorekeeper.R;
import ua.napps.scorekeeper.listeners.DragItemListener;
import ua.napps.scorekeeper.log.LogActivity;
import ua.napps.scorekeeper.log.LogEntry;
import ua.napps.scorekeeper.log.LogType;
import ua.napps.scorekeeper.settings.AboutActivity;
import ua.napps.scorekeeper.settings.LocalSettings;
import ua.napps.scorekeeper.utils.BounceItemAnimator;
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
    private int currentCountersCount;
    private boolean isLongPressTipShowed;
    private ItemTouchHelper itemTouchHelper;
    private Toolbar toolbar;
    private TextView toolbarTitle;
    private int previousTopCounterId;
    private boolean isLowestScoreWins;
    private boolean isSumMode;
    private boolean isVibrate;
    private boolean isSwapPressLogicEnabled;
    private boolean isAutoSortEnabled;
    private boolean isAutosSortDescending;
    private boolean wasUsingLinearLayout;
    private int counterStepDialogMode;
    private int counterStep1, counterStep2, counterStep3, counterStep4, counterStep5, counterStep6, counterStep7;
    private SpanningLinearLayoutManager spanningLayoutManager;
    private LinearLayoutManager linearLayoutManager;
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
        toolbarTitle.setOnClickListener(v -> showSortingControls());

        spanningLayoutManager = createSpanningLayoutManager();
        linearLayoutManager = new LinearLayoutManager(requireContext());

        int layoutThreshold = getLayoutThreshold();
        countersAdapter = new CountersAdapter(this, this, layoutThreshold);
        recyclerView = contentView.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(spanningLayoutManager);
        recyclerView.setItemAnimator(new BounceItemAnimator());
        recyclerView.setAdapter(countersAdapter);

        emptyState = contentView.findViewById(R.id.empty_state);

        isLongPressTipShowed = LocalSettings.getLongPressTipShowed();
        isSwapPressLogicEnabled = LocalSettings.isSwapPressLogicEnabled();
        isVibrate = LocalSettings.isCountersVibrate();
        isAutoSortEnabled = LocalSettings.isAutoSortEnabled();
        isAutosSortDescending = LocalSettings.isAutoSortDescending();
        counterStep1 = LocalSettings.getCustomCounter(1);
        counterStep2 = LocalSettings.getCustomCounter(2);
        counterStep3 = LocalSettings.getCustomCounter(3);
        counterStep4 = LocalSettings.getCustomCounter(4);
        counterStep5 = LocalSettings.getCustomCounter(5);
        counterStep6 = LocalSettings.getCustomCounter(6);
        counterStep7 = LocalSettings.getCustomCounter(7);

        vibrator = (Vibrator) requireActivity().getSystemService(Context.VIBRATOR_SERVICE);

        observeData();
        return contentView;
    }

    private void showSortingControls() {
        // Inflate the popup layout
        View dialogCustomView = LayoutInflater.from(requireContext())
                .inflate(R.layout.popup_sort_controls, null);

        new MaterialDialog.Builder(requireActivity())
                .customView(dialogCustomView, false)
                .show();

        // Get references to the switches
        MaterialButtonToggleGroup titleButtonsGroup = dialogCustomView.findViewById(R.id.title_options_group);
        SwitcherX autoSortSwitch = dialogCustomView.findViewById(R.id.switch_auto_sort);
        SwitcherX sortDirectionSwitch = dialogCustomView.findViewById(R.id.switch_sort_direction);
        View autoSortSwitchContainer = dialogCustomView.findViewById(R.id.container_auto_sort);
        View sortDirectionSwitchContainer = dialogCustomView.findViewById(R.id.container_sort_direction);

        // Set initial states
        viewModel.isAutoSortEnabled().observe(getViewLifecycleOwner(),
                checked -> {
                    autoSortSwitch.setChecked(checked, false);
                    autoSortSwitch.setClickable(false);

                    // disable sort direction
                    sortDirectionSwitchContainer.setAlpha(!checked ? 0.5f : 1.0f);
                    sortDirectionSwitch.setEnabled(checked);
                    sortDirectionSwitch.setClickable(false);
                    LocalSettings.saveAutoSortEnabled(checked);
                });
        viewModel.isSortDescending().observe(getViewLifecycleOwner(),
                checked -> {
                    sortDirectionSwitch.setChecked(checked, false);
                    sortDirectionSwitch.setClickable(false);
                    LocalSettings.saveAutoSortDescending(checked);
                });

        // Set up listeners
        autoSortSwitchContainer.setOnClickListener(v -> {
            viewModel.toggleAutoSort();
            tryVibrate();
        });

        sortDirectionSwitchContainer.setOnClickListener(v -> {
            viewModel.toggleSortDirection();
            tryVibrate();
        });

        if (isSumMode) {
            titleButtonsGroup.check(R.id.btn_sum_counter);
        } else {
            if (!isLowestScoreWins) {
                titleButtonsGroup.check(R.id.btn_top_counter);
            } else {
                titleButtonsGroup.check(R.id.btn_last_counter);
            }
        }

        titleButtonsGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                switch (checkedId) {
                    case R.id.btn_top_counter:
                        isLowestScoreWins = false;
                        isSumMode = false;
                        updateTitle();
                        break;
                    case R.id.btn_last_counter:
                        isLowestScoreWins = true;
                        isSumMode = false;
                        updateTitle();
                        break;
                    case R.id.btn_sum_counter:
                        isSumMode = true;
                        updateTitle();
                        break;
                }
            }
        });
    }

    private void updateTitle() {
        List<Counter> counters = viewModel.getCounters().getValue();
        findAndUpdateTopCounterView(counters);
        tryVibrate();
    }

    private void showSnack(@StringRes Integer messageId) {
        Snackbar snack = Snackbar.make(recyclerView, messageId, Snackbar.LENGTH_SHORT);
        snack.show();
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        MenuItem removeItem = menu.findItem(R.id.menu_remove_all);
        final boolean hasCounters = currentCountersCount > 0;
        if (removeItem != null) {
            removeItem.setEnabled(hasCounters);
        }
        MenuItem clearAllItems = menu.findItem(R.id.menu_reset_all);
        if (clearAllItems != null) {
            clearAllItems.setEnabled(hasCounters);
        }

        MenuItem deleteAllItems = menu.findItem(R.id.menu_remove_all);
        if (deleteAllItems != null) {
            deleteAllItems.setEnabled(hasCounters);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.counters_menu, menu);

        MenuItem item = menu.findItem(R.id.menu_remove_all);
        SpannableString s = new SpannableString(item.getTitle().toString());
        s.setSpan(new ForegroundColorSpan(Color.argb(255, 255, 79, 94)), 0, s.length(), 0);
        item.setTitle(s);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            menu.setGroupDividerEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_counter:
                viewModel.addCounter();
                break;
            case R.id.menu_remove_all:
                new MaterialDialog.Builder(requireActivity())
                        .title(R.string.menu_delete_all_counters)
                        .content(R.string.dialog_confirmation_question)
                        .onPositive((dialog, which) -> viewModel.removeAll())
                        .onNegative((dialog, which) -> dialog.dismiss())
                        .showListener(dialog1 -> {
                            TextView content = ((MaterialDialog) dialog1).getContentView();
                            if (content != null) {
                                content.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                            }
                        })
                        .positiveText(R.string.delete)
                        .positiveColorRes(R.color.colorError)
                        .negativeText(R.string.dialog_no)
                        .show();
                break;
            case R.id.menu_reset_all:
                new MaterialDialog.Builder(requireActivity())
                        .title(R.string.menu_reset_all_counters)
                        .content(R.string.dialog_confirmation_question)
                        .onPositive((dialog, which) -> {
                            viewModel.resetAll();
                            toolbar.setTitle(R.string.common_counters);
                        })
                        .onNegative((dialog, which) -> dialog.dismiss())
                        .positiveText(R.string.dialog_yes)
                        .showListener(dialog1 -> {
                            TextView content = ((MaterialDialog) dialog1).getContentView();
                            if (content != null) {
                                content.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                            }
                        })
                        .positiveColorRes(R.color.colorError)
                        .negativeText(R.string.dialog_no)
                        .show();
                break;
            case R.id.menu_log:
                LogActivity.start(requireActivity());
                break;
            case R.id.menu_about_app:
                AboutActivity.start(requireActivity());
                break;
            case R.id.menu_sort:
                showSortingControls();
                break;
            case R.id.menu_elektu:
                new MaterialDialog.Builder(requireActivity())
                        .title(R.string.menu_elektu)
                        .content(R.string.dialog_elektu_confirm)
                        .onPositive((dialog, which) -> {
                            Intent viewIntent =
                                    new Intent(Intent.ACTION_VIEW, Uri.parse("https://jbellue.github.io/elektu/"));
                            try {
                                startActivity(viewIntent);
                            } catch (Exception e) {
                                Toast.makeText(requireContext(), R.string.message_app_not_found, Toast.LENGTH_SHORT).show();
                            }
                        })
                        .onNegative((dialog, which) -> dialog.dismiss())
                        .positiveText(R.string.common_continue)
                        .negativeText(R.string.common_back)
                        .show();

                break;
        }
        return true;
    }

    private void observeData() {
        CountersViewModelFactory factory = new CountersViewModelFactory(requireActivity().getApplication());
        viewModel = new ViewModelProvider(this, factory).get(CountersViewModel.class);
        viewModel.setAutoSort(isAutoSortEnabled);
        viewModel.setSortDescending(isAutosSortDescending);
        viewModel.getCounters().observe(getViewLifecycleOwner(), this::updateUI);
        viewModel.getSnackbarMessage().observe(getViewLifecycleOwner(), (Observer<Integer>) resourceId -> {
            if (resourceId == R.string.counter_added) {
                Snackbar.make(recyclerView, getString(resourceId), Snackbar.LENGTH_SHORT)
                        .setAction(R.string.message_one_more_counter, v -> viewModel.addCounter())
                        .show();
            } else {
                showSnack(resourceId);
            }
        });
        viewModel.eventBus.observeForever(eventBusObserver);
    }

    private void updateUI(List<Counter> counters) {
        if (counters != null) {
            final int size = counters.size();

            if (size == 0) {
                toolbar.setTitle(R.string.common_counters);
                emptyState.setVisibility(View.VISIBLE);
            } else if (size == 1) {
                emptyState.setVisibility(View.GONE);
            } else { // size >= 2
                findAndUpdateTopCounterView(counters);
                emptyState.setVisibility(View.GONE);
            }

            if (currentCountersCount != size) {
                if (size < getLayoutThreshold()) {
                    if (wasUsingLinearLayout) {
                        recyclerView.setLayoutManager(spanningLayoutManager);
                        wasUsingLinearLayout = false;
                    }
                } else {
                    if (!wasUsingLinearLayout) {
                        recyclerView.setLayoutManager(linearLayoutManager);
                        wasUsingLinearLayout = true;
                    }
                }
            }

            countersAdapter.setCountersList(counters);

            if (size > currentCountersCount && currentCountersCount > 0) {
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
                if (currentCountersCount - 1 == size) {
                    showSnack(R.string.counter_deleted);
                    viewModel.updatePositions();
                    tryVibrate();
                }
            }
            currentCountersCount = size;
        } else {
            emptyState.setVisibility(View.VISIBLE);
        }
    }

    private void findAndUpdateTopCounterView(List<Counter> counters) {
        if (counters == null || counters.isEmpty()) return;
        if (isSumMode) {
            // Calculate and display sum of all counters
            int sum = 0;
            for (Counter counter : counters) {
                sum += counter.getValue();
            }
            toolbar.setTitle("\u2211 " + sum);  // Σ symbol for sum
            previousTopCounterId = -1;  // Special value for sum mode
            return;
        }

        // Existing logic for highest/lowest counter
        List<Counter> topCounters = findTopCounters(counters);
        int topSize = topCounters.size();
        if (topSize == 1) {
            Counter top = topCounters.get(0);
            if (isLowestScoreWins) {
                toolbar.setTitle("\uD83D\uDCC9 " + top.getName());
            } else {
                toolbar.setTitle("\ud83c\udfc6 " + top.getName());
            }
            int counterId = top.getId();
            if (previousTopCounterId != counterId) {
                if (toolbarTitle != null) {
                    ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(
                            toolbarTitle,
                            PropertyValuesHolder.ofFloat("alpha", 1f, 0f, 1f),
                            PropertyValuesHolder.ofFloat("rotation", 0f, 360f)
                    );
                    animator.setDuration(500);
                    animator.start();
                }
                previousTopCounterId = counterId;
            }
        } else {
            int countersTotal = counters.size();
            if (topSize != countersTotal) {
                toolbar.setTitle(topSize + " =");
                ViewUtil.shakeView(toolbarTitle, 2, 2);
            }
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
        switch (mode) {
            case MODE_INCREASE_VALUE:
                increaseCounterValue(counter, counter.getStep());
                break;
            case MODE_DECREASE_VALUE:
                decreaseCounterValue(counter, counter.getStep());
                break;
            default:
                break;
        }
    }

    private void increaseCounterValue(Counter counter, int step) {
        if (step == 0) {
            return;
        }
        Singleton.getInstance().addLogEntry(new LogEntry(counter, LogType.INC, step, counter.getValue()));

        viewModel.increaseCounter(counter, step);
        tryVibrate();

        if (Math.abs(counter.getValue() - counter.getDefaultValue()) > 10) {
            showLongPressHint();
        }
    }

    private void decreaseCounterValue(Counter counter, int step) {
        if (step == 0) {
            return;
        }
        Singleton.getInstance().addLogEntry(new LogEntry(counter, LogType.DEC, step, counter.getValue()));

        viewModel.decreaseCounter(counter, step);
        tryVibrate();

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
        tryVibrate();
    }

    private void showSetValueDialog(Counter c, int position) {
        Typeface mono = getResources().getFont(R.font.mono);
        Typeface regular = getResources().getFont(R.font.o400);
        final MaterialDialog md = new MaterialDialog.Builder(requireActivity())
                .title(c.getName() + ": " + c.getValue())
                .titleGravity(GravityEnum.CENTER)
                .inputType(InputType.TYPE_CLASS_PHONE)
                .positiveText(R.string.common_set)
                .typeface(regular, mono)
                .contentColorRes(R.color.colorOnSurface)
                .buttonRippleColorRes(R.color.rippleColor)
                .widgetColorRes(R.color.colorPrimary)
                .positiveColorRes(R.color.colorPrimary)
                .alwaysCallInputCallback()
                .input(R.string.simple_edit_value_hint, 0, true, (dialog, input) -> {
                })
                .showListener(dialogInterface -> {
                    TextView titleTextView = ((MaterialDialog) dialogInterface).getTitleView();
                    if (titleTextView != null) {
                        titleTextView.setLines(1);
                        titleTextView.setEllipsize(TextUtils.TruncateAt.END);
                        titleTextView.setGravity(Gravity.CENTER);
                        titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
                    }
                    TextView content = ((MaterialDialog) dialogInterface).getContentView();
                    if (content != null) {
                        content.setLines(1);
                        content.setEllipsize(TextUtils.TruncateAt.END);
                        content.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
                    }
                    EditText inputEditText = ((MaterialDialog) dialogInterface).getInputEditText();
                    if (inputEditText != null) {
                        inputEditText.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
                        inputEditText.requestFocus();
                        inputEditText.setGravity(Gravity.CENTER);
                        inputEditText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32);
                    }
                })
                .onPositive((dialog, which) -> {
                    EditText editText = dialog.getInputEditText();
                    if (editText == null) {
                        return;
                    }
                    String input = editText.getText().toString();
                    if (input.isEmpty()) {
                        return;
                    }
                    int value;
                    boolean isPositive = true;
                    int startIndex = 0;
                    char firstChar = input.charAt(0);

                    if (firstChar == '+') {
                        startIndex = 1;
                    } else if (firstChar == '-') {
                        isPositive = false;
                        startIndex = 1;
                    }
                    if (startIndex < input.length()) {
                        try {
                            value = Integer.parseInt(input.substring(startIndex));
                            if (startIndex == 0) {
                                viewModel.modifyCurrentValue(c, value);
                                countersAdapter.notifyItemChanged(position);
                            } else {
                                if (isPositive) {
                                    increaseCounterValue(c, value);
                                    countersAdapter.notifyItemChanged(position, INCREASE_VALUE_CLICK);
                                } else {
                                    decreaseCounterValue(c, value);
                                    countersAdapter.notifyItemChanged(position, DECREASE_VALUE_CLICK);
                                }
                            }
                        } catch (NumberFormatException e) {
                            // Invalid input, ignore
                        }
                    }
                    dialog.dismiss();
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
        if (isVibrate) {
            Utilities.vibrate(requireContext(), vibrator);
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
                        tryVibrate();
                        updateButtonLabels(contentView);
                        break;
                    case R.id.btn_dec:
                        counterStepDialogMode = MODE_DECREASE_VALUE;
                        ViewUtil.shakeView(contentView, 2, 2);
                        tryVibrate();
                        updateButtonLabels(contentView);
                        break;
                }
            }
        });

        updateButtonLabels(contentView);
        int color = Color.parseColor(counter.getColor());
        final boolean darkBackground = ColorUtil.isDarkBackground(color);
        int tintColor = darkBackground ? 0xF7FFFFFF : 0xDE000000;

        String text = counter.getName() + ": " + counter.getValue();
        contentView.findViewById(R.id.counter_info_header).setBackgroundColor(color);
        TextView title = contentView.findViewById(R.id.counter_info_content);
        title.setText(text);
        title.setTextColor(tintColor);

        contentView.findViewById(R.id.btn_one).setOnClickListener(v -> {
            if (counterStepDialogMode == MODE_INCREASE_VALUE) {
                increaseCounterValue(counter, counterStep1);
                countersAdapter.notifyItemChanged(position, INCREASE_VALUE_CLICK);
            } else if (counterStepDialogMode == MODE_DECREASE_VALUE) {
                decreaseCounterValue(counter, counterStep1);
                countersAdapter.notifyItemChanged(position, DECREASE_VALUE_CLICK);
            }

            longClickDialog.dismiss();
        });
        contentView.findViewById(R.id.btn_two).setOnClickListener(v -> {
            if (counterStepDialogMode == MODE_INCREASE_VALUE) {
                increaseCounterValue(counter, counterStep2);
                countersAdapter.notifyItemChanged(position, INCREASE_VALUE_CLICK);
            } else if (counterStepDialogMode == MODE_DECREASE_VALUE) {
                decreaseCounterValue(counter, counterStep2);
                countersAdapter.notifyItemChanged(position, DECREASE_VALUE_CLICK);
            }

            longClickDialog.dismiss();
        });
        contentView.findViewById(R.id.btn_three).setOnClickListener(v -> {
            if (counterStepDialogMode == MODE_INCREASE_VALUE) {
                increaseCounterValue(counter, counterStep3);
                countersAdapter.notifyItemChanged(position, INCREASE_VALUE_CLICK);
            } else if (counterStepDialogMode == MODE_DECREASE_VALUE) {
                decreaseCounterValue(counter, counterStep3);
                countersAdapter.notifyItemChanged(position, DECREASE_VALUE_CLICK);
            }

            longClickDialog.dismiss();
        });
        contentView.findViewById(R.id.btn_four).setOnClickListener(v -> {
            if (counterStepDialogMode == MODE_INCREASE_VALUE) {
                increaseCounterValue(counter, counterStep4);
                countersAdapter.notifyItemChanged(position, INCREASE_VALUE_CLICK);
            } else if (counterStepDialogMode == MODE_DECREASE_VALUE) {
                decreaseCounterValue(counter, counterStep4);
                countersAdapter.notifyItemChanged(position, DECREASE_VALUE_CLICK);
            }

            longClickDialog.dismiss();
        });
        contentView.findViewById(R.id.btn_five).setOnClickListener(v -> {
            if (counterStepDialogMode == MODE_INCREASE_VALUE) {
                increaseCounterValue(counter, counterStep5);
                countersAdapter.notifyItemChanged(position, INCREASE_VALUE_CLICK);

            } else if (counterStepDialogMode == MODE_DECREASE_VALUE) {
                decreaseCounterValue(counter, counterStep5);
                countersAdapter.notifyItemChanged(position, DECREASE_VALUE_CLICK);
            }

            longClickDialog.dismiss();
        });
        contentView.findViewById(R.id.btn_six).setOnClickListener(v -> {
            if (counterStepDialogMode == MODE_INCREASE_VALUE) {
                increaseCounterValue(counter, counterStep6);
                countersAdapter.notifyItemChanged(position, INCREASE_VALUE_CLICK);

            } else if (counterStepDialogMode == MODE_DECREASE_VALUE) {
                decreaseCounterValue(counter, counterStep6);
                countersAdapter.notifyItemChanged(position, DECREASE_VALUE_CLICK);
            }

            longClickDialog.dismiss();
        });
        contentView.findViewById(R.id.btn_seven).setOnClickListener(v -> {
            if (counterStepDialogMode == MODE_INCREASE_VALUE) {
                increaseCounterValue(counter, counterStep7);
                countersAdapter.notifyItemChanged(position, INCREASE_VALUE_CLICK);

            } else if (counterStepDialogMode == MODE_DECREASE_VALUE) {
                decreaseCounterValue(counter, counterStep7);
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
                        increaseCounterValue(counter, intValue);
                        countersAdapter.notifyItemChanged(position, INCREASE_VALUE_CLICK);
                    } else if (counterStepDialogMode == MODE_DECREASE_VALUE) {
                        decreaseCounterValue(counter, intValue);
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
                            .setAllCorners(CornerFamily.ROUNDED, ViewUtil.dip2px(16, requireContext()))
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
        ((TextView) contentView.findViewById(R.id.btn_five_text)).setText(sign + counterStep5);
        ((TextView) contentView.findViewById(R.id.btn_six_text)).setText(sign + counterStep6);
        ((TextView) contentView.findViewById(R.id.btn_seven_text)).setText(sign + counterStep7);
    }

    @Override
    public void onNameClick(Counter counter) {
        final MaterialDialog md = new MaterialDialog.Builder(requireActivity())
                .title(R.string.counter_details_name)
                .inputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT | InputType.TYPE_TEXT_VARIATION_PERSON_NAME)
                .positiveText(R.string.common_set)
                .neutralText(R.string.common_more)
                .contentColorRes(R.color.colorOnSurface)
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
                        inputEditText.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
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
        viewModel.setAutoSort(false);
        viewModel.modifyPosition(counter, fromIndex, toIndex);
    }

    public void onSharedPreferencesUpdated(String key) {
        switch (key) {
            case LocalSettings.IS_SWAP_PRESS_LOGIC:
                isSwapPressLogicEnabled = LocalSettings.isSwapPressLogicEnabled();
                break;
            case LocalSettings.IS_COUNTERS_VIBRATE:
                isVibrate = LocalSettings.isCountersVibrate();
                break;
            case LocalSettings.CUSTOM_COUNTER_1:
                counterStep1 = LocalSettings.getCustomCounter(1);
                break;
            case LocalSettings.CUSTOM_COUNTER_2:
                counterStep2 = LocalSettings.getCustomCounter(2);
                break;
            case LocalSettings.CUSTOM_COUNTER_3:
                counterStep3 = LocalSettings.getCustomCounter(3);
                break;
            case LocalSettings.CUSTOM_COUNTER_4:
                counterStep4 = LocalSettings.getCustomCounter(4);
                break;
            case LocalSettings.CUSTOM_COUNTER_5:
                counterStep5 = LocalSettings.getCustomCounter(5);
                break;
            case LocalSettings.CUSTOM_COUNTER_6:
                counterStep6 = LocalSettings.getCustomCounter(6);
                break;
            case LocalSettings.CUSTOM_COUNTER_7:
                counterStep7 = LocalSettings.getCustomCounter(7);
                break;
        }
    }

    /**
     * Creates a SpanningLinearLayoutManager with the appropriate orientation based on the current configuration.
     */
    private SpanningLinearLayoutManager createSpanningLayoutManager() {
        int orientation = getResources().getConfiguration().orientation;
        return new SpanningLinearLayoutManager(requireContext(),
                orientation == Configuration.ORIENTATION_LANDSCAPE ? LinearLayoutManager.HORIZONTAL : LinearLayoutManager.VERTICAL, false);
    }

    private int getLayoutThreshold() {
        return getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? 5 : 7;
    }

    @Override
    public void onPause() {
        super.onPause();
        wasUsingLinearLayout = recyclerView.getLayoutManager().equals(linearLayoutManager);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (currentCountersCount > 0) {

            if (wasUsingLinearLayout) {
                // Force correct LayoutManager based on current list size
                int size = recyclerView.getAdapter() != null ? recyclerView.getAdapter().getItemCount() : 0;
                if (size < getLayoutThreshold()) {
                    recyclerView.setLayoutManager(spanningLayoutManager);
                } else {
                    recyclerView.setLayoutManager(linearLayoutManager);
                }

                recyclerView.post(() -> {
                    if (recyclerView.getAdapter() != null) {
                        recyclerView.getAdapter().notifyDataSetChanged();
                    }
                });
            }

            viewModel.triggerAutoSortIfNeeded();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (viewModel != null) {
            viewModel.eventBus.removeObserver(eventBusObserver);
        }
    }
}
