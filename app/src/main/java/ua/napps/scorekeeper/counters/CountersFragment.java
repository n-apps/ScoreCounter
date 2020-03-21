package ua.napps.scorekeeper.counters;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.analytics.FirebaseAnalytics;

import ua.napps.scorekeeper.R;
import ua.napps.scorekeeper.listeners.DialogPositiveClickListener;
import ua.napps.scorekeeper.listeners.DragItemListener;
import ua.napps.scorekeeper.log.LogActivity;
import ua.napps.scorekeeper.log.LogEntry;
import ua.napps.scorekeeper.log.LogType;
import ua.napps.scorekeeper.settings.LocalSettings;
import ua.napps.scorekeeper.utils.AndroidFirebaseAnalytics;
import ua.napps.scorekeeper.utils.DonateDialog;
import ua.napps.scorekeeper.utils.Singleton;
import ua.napps.scorekeeper.utils.SpanningLinearLayoutManager;
import ua.napps.scorekeeper.utils.Utilities;

import static ua.napps.scorekeeper.counters.CountersAdapter.MODE_DECREASE_VALUE;
import static ua.napps.scorekeeper.counters.CountersAdapter.MODE_INCREASE_VALUE;
import static ua.napps.scorekeeper.counters.CountersAdapter.MODE_SET_VALUE;

public class CountersFragment extends Fragment implements CounterActionCallback, DragItemListener {

    private RecyclerView recyclerView;
    private CountersAdapter countersAdapter;
    private View emptyState;
    private CountersViewModel viewModel;
    private boolean isFirstLoad = true;
    private MaterialDialog longClickDialog;
    private int oldListSize;
    private boolean isLongPressTipShowed;
    private ItemTouchHelper itemTouchHelper;

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
        Toolbar toolbar = contentView.findViewById(R.id.toolbar);

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(null);

        Drawable drawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_more_vert);
        toolbar.setOverflowIcon(drawable);

        recyclerView = contentView.findViewById(R.id.recycler_view);
        recyclerView.setItemAnimator(new ChangeCounterValueAnimator());
        emptyState = contentView.findViewById(R.id.empty_state);
        emptyState.setOnClickListener(view -> viewModel.addCounter());

        return contentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        CountersViewModelFactory factory = new CountersViewModelFactory(requireActivity().getApplication());
        viewModel = new ViewModelProvider(this, factory).get(CountersViewModel.class);
        countersAdapter = new CountersAdapter(this, this);
        subscribeUi();
        isLongPressTipShowed = LocalSettings.getLongPressTipShowed();
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
    }

    private void showDialogWithAction(final DialogPositiveClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setMessage(R.string.dialog_confirmation_question)
                .setPositiveButton(R.string.dialog_yes, (dialog, id) -> listener.onPositiveButtonClicked(getActivity()))
                .setNegativeButton(R.string.dialog_no, (dialog, id) -> dialog.dismiss());
        builder.create().show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_counter:
                if (viewModel.getCounters().getValue() != null) {
                    viewModel.addCounter();
                } else {
                    subscribeUi();
                }
                break;
            case R.id.menu_remove_all:
                AndroidFirebaseAnalytics.logEvent("CountersScreenMenuRemoveAllClick");
                DialogPositiveClickListener dialogListenerRemove = context -> viewModel.removeAll();
                showDialogWithAction(dialogListenerRemove);
                break;
            case R.id.menu_reset_all:
                AndroidFirebaseAnalytics.logEvent("CountersScreenMenuResetAllClick");
                DialogPositiveClickListener dialogListenerReset = context -> viewModel.resetAll();
                showDialogWithAction(dialogListenerReset);
                break;
            case R.id.menu_log:
                Intent intent = new Intent(getActivity(), LogActivity.class);
                startActivity(intent);
                AndroidFirebaseAnalytics.logEvent("CountersScreenMenuHistoryClick");
                break;
            case R.id.menu_donate:
                AndroidFirebaseAnalytics.logEvent("CountersScreenMenuDonateClick");

                DonateDialog dialog = new DonateDialog();
                dialog.show(getParentFragmentManager(), "donate");
                break;
        }
        return true;
    }

    private void subscribeUi() {
        viewModel.getCounters().observe(getViewLifecycleOwner(), counters -> {
            if (counters != null) {
                final int size = counters.size();
                emptyState.setVisibility(size > 0 ? View.GONE : View.VISIBLE);

                if (oldListSize != size) {
                    countersAdapter.notifyDataSetChanged();
                    if (size > 4) {
                        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

                    } else {
                        recyclerView.setLayoutManager(new SpanningLinearLayoutManager(requireContext()));
                    }
                }

                countersAdapter.setCountersList(counters);
                if (size > oldListSize && oldListSize > 0) {
                    recyclerView.smoothScrollToPosition(size);
                }
                oldListSize = size;
                if (isFirstLoad) {
                    isFirstLoad = false;
                    recyclerView.post(() -> {
                                recyclerView.setAdapter(countersAdapter);
                                ItemTouchHelper.Callback callback = new ItemDragHelperCallback(countersAdapter);
                                itemTouchHelper = new ItemTouchHelper(callback);
                                itemTouchHelper.attachToRecyclerView(recyclerView);
                            }
                    );
                    AndroidFirebaseAnalytics.setUserProperty("counters_size", "" + size);
                }
            } else {
                emptyState.setVisibility(View.VISIBLE);
            }
        });
    }

    private void showLongPressHint() {
        if (!isLongPressTipShowed) {
            Handler handler = new Handler();
            handler.postDelayed(() -> Toast.makeText(requireContext(), R.string.message_you_can_use_long_press, Toast.LENGTH_LONG).show(), 500);
            LocalSettings.setLongPressTipShowed();
            isLongPressTipShowed = true;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        AndroidFirebaseAnalytics.trackScreen(requireActivity(), "Counters List", getClass().getSimpleName());
    }

    @Override
    public void onSingleClick(Counter counter, int position, int mode) {
        Bundle params = new Bundle();
        params.putString(FirebaseAnalytics.Param.CHARACTER, "" + mode);
        AndroidFirebaseAnalytics.logEvent("CountersScreenCounterSingleClick", params);

        if (mode == MODE_DECREASE_VALUE) {
            if (counter.getStep() == 0) {
                return;
            }
            Singleton.getInstance().addLogEntry(new LogEntry(counter, LogType.DEC, 1, counter.getValue()));

            viewModel.decreaseCounter(counter, -counter.getStep());
            if (Math.abs(counter.getValue() - counter.getDefaultValue()) > 20) {
                showLongPressHint();
            }
        } else if (mode == MODE_INCREASE_VALUE) {
            if (counter.getStep() == 0) {
                return;
            }
            Singleton.getInstance().addLogEntry(new LogEntry(counter, LogType.INC, 1, counter.getValue()));

            viewModel.increaseCounter(counter, counter.getStep());
            if (Math.abs(counter.getValue() - counter.getDefaultValue()) > 20) {
                showLongPressHint();
            }
        } else if (mode == MODE_SET_VALUE) {
            showCounterStepDialog(counter, position, mode);
        }
    }

    @Override
    public void onLongClick(Counter counter, int position, int mode) {
        Bundle params = new Bundle();
        params.putString(FirebaseAnalytics.Param.CHARACTER, "" + mode);
        AndroidFirebaseAnalytics.logEvent("CountersScreenCounterLongClick", params);
        if (mode == MODE_SET_VALUE) {
            final MaterialDialog md = new MaterialDialog.Builder(requireActivity())
                    .content(counter.getName())
                    .inputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED)
                    .positiveText(R.string.common_set)
                    .neutralText(R.string.reset)
                    .alwaysCallInputCallback()
                    .input("" + counter.getValue(), null, false, (dialog, input) -> {

                    })
                    .onNeutral((dialog, which) -> viewModel.resetCounter(counter))
                    .onPositive((dialog, which) -> {
                        EditText editText = dialog.getInputEditText();
                        if (editText != null) {
                            Integer value = Utilities.parseInt(editText.getText().toString());
                            viewModel.modifyCurrentValue(counter, value);
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
        } else {
            showCounterStepDialog(counter, position, mode);
        }
    }

    private void showCounterStepDialog(Counter counter, int position, int mode) {
        final MaterialDialog.Builder builder = new MaterialDialog.Builder(requireActivity());
        final Observer<Counter> counterObserver = c -> {
            if (longClickDialog != null && c != null) {
                longClickDialog.getTitleView().setText(c.getName());
            }
        };
        boolean isIncrease = mode != MODE_DECREASE_VALUE;
        final LiveData<Counter> liveData = viewModel.getCounterLiveData(counter.getId());
        liveData.observe(this, counterObserver);

        int layoutId = isIncrease ? R.layout.dialog_counter_step_increase : R.layout.dialog_counter_step_decrease;
        final View contentView = LayoutInflater.from(requireActivity()).inflate(layoutId, null, false);

        String btnSign = "-";
        if (isIncrease) {
            btnSign = "+";
        }

        ((TextView) contentView.findViewById(R.id.btn_one_text)).setText(btnSign + LocalSettings.getCustomCounter(1));
        contentView.findViewById(R.id.btn_one).setOnClickListener(v -> {
            int value = LocalSettings.getCustomCounter(1);
            if (isIncrease) {
                Singleton.getInstance().addLogEntry(new LogEntry(counter, LogType.INC_C, value, counter.getValue()));

                viewModel.increaseCounter(counter, value);
                countersAdapter.notifyItemChanged(position, MODE_INCREASE_VALUE);
            } else {
                Singleton.getInstance().addLogEntry(new LogEntry(counter, LogType.DEC_C, -value, counter.getValue()));

                viewModel.decreaseCounter(counter, -value);
                countersAdapter.notifyItemChanged(position, MODE_DECREASE_VALUE);
            }
            longClickDialog.dismiss();
        });

        ((TextView) contentView.findViewById(R.id.btn_two_text)).setText(btnSign + LocalSettings.getCustomCounter(2));
        contentView.findViewById(R.id.btn_two).setOnClickListener(v -> {
            int value = LocalSettings.getCustomCounter(2);
            if (isIncrease) {
                Singleton.getInstance().addLogEntry(new LogEntry(counter, LogType.INC_C, value, counter.getValue()));

                viewModel.increaseCounter(counter, value);
                countersAdapter.notifyItemChanged(position, MODE_INCREASE_VALUE);
            } else {
                Singleton.getInstance().addLogEntry(new LogEntry(counter, LogType.DEC_C, value, counter.getValue()));

                viewModel.decreaseCounter(counter, -value);
                countersAdapter.notifyItemChanged(position, MODE_DECREASE_VALUE);
            }
            longClickDialog.dismiss();
        });

        ((TextView) contentView.findViewById(R.id.btn_three_text)).setText(btnSign + LocalSettings.getCustomCounter(3));
        contentView.findViewById(R.id.btn_three).setOnClickListener(v -> {
            int value = LocalSettings.getCustomCounter(3);
            if (isIncrease) {
                Singleton.getInstance().addLogEntry(new LogEntry(counter, LogType.INC_C, value, counter.getValue()));

                viewModel.increaseCounter(counter, value);
                countersAdapter.notifyItemChanged(position, MODE_INCREASE_VALUE);
            } else {
                Singleton.getInstance().addLogEntry(new LogEntry(counter, LogType.DEC_C, value, counter.getValue()));

                viewModel.decreaseCounter(counter, -value);
                countersAdapter.notifyItemChanged(position, MODE_DECREASE_VALUE);
            }
            longClickDialog.dismiss();
        });

        ((TextView) contentView.findViewById(R.id.btn_four_text)).setText(btnSign + LocalSettings.getCustomCounter(4));
        contentView.findViewById(R.id.btn_four).setOnClickListener(v -> {
            int value = LocalSettings.getCustomCounter(4);
            if (isIncrease) {
                Singleton.getInstance().addLogEntry(new LogEntry(counter, LogType.INC_C, value, counter.getValue()));

                viewModel.increaseCounter(counter, value);
                countersAdapter.notifyItemChanged(position, MODE_INCREASE_VALUE);
            } else {
                Singleton.getInstance().addLogEntry(new LogEntry(counter, LogType.DEC_C, value, counter.getValue()));

                viewModel.decreaseCounter(counter, -value);
                countersAdapter.notifyItemChanged(position, MODE_DECREASE_VALUE);
            }
            longClickDialog.dismiss();
        });

        final EditText editText = contentView.findViewById(R.id.et_add_custom_value);
        editText.setOnEditorActionListener((textView, actionId, event) -> {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId
                    == EditorInfo.IME_ACTION_DONE)) {
                final String value = editText.getText().toString();
                if (!TextUtils.isEmpty(value)) {
                    int intValue = Utilities.parseInt(value);
                    if (isIncrease) {
                        Singleton.getInstance().addLogEntry(new LogEntry(counter, LogType.INC_C, intValue, counter.getValue()));

                        viewModel.increaseCounter(counter, intValue);
                        countersAdapter.notifyItemChanged(position, MODE_INCREASE_VALUE);
                    } else {
                        Singleton.getInstance().addLogEntry(new LogEntry(counter, LogType.DEC_C, intValue, counter.getValue()));

                        viewModel.decreaseCounter(counter, -intValue);
                        countersAdapter.notifyItemChanged(position, MODE_DECREASE_VALUE);
                    }
                }
                longClickDialog.dismiss();
            }
            return false;
        });
        builder.customView(contentView, false);
        builder.title(R.string.dialog_current_value_title);
        builder.dismissListener(dialogInterface -> liveData.removeObserver(counterObserver));
        longClickDialog = builder.build();
        longClickDialog.show();

        editText.post(() -> {
            editText.requestFocus();

            InputMethodManager inputMethodManager = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputMethodManager != null) {
                inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
            }
        });
    }

    @Override
    public void onNameClick(Counter counter) {
        final MaterialDialog md = new MaterialDialog.Builder(requireActivity())
                .content(R.string.counter_details_name)
                .inputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT | InputType.TYPE_TEXT_VARIATION_PERSON_NAME)
                .positiveText(R.string.common_set)
                .neutralText(R.string.common_more)
                .input(counter.getName(), null, false, (dialog, input) -> viewModel.modifyName(counter, input.toString()))
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
        AndroidFirebaseAnalytics.logEvent("CountersScreenCounterNameClick");
    }

    @Override
    public void onEditClick(Counter counter) {
        EditCounterActivity.start(getActivity(), counter);
        AndroidFirebaseAnalytics.logEvent("CountersScreenCounterEditClick");
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

}
