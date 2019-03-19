package ua.napps.scorekeeper.counters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import ua.com.napps.scorekeeper.R;
import ua.napps.scorekeeper.app.App;
import ua.napps.scorekeeper.listeners.DialogPositiveClickListener;
import ua.napps.scorekeeper.listeners.DragItemListener;
import ua.napps.scorekeeper.log.LogActivity;
import ua.napps.scorekeeper.log.LogEntry;
import ua.napps.scorekeeper.log.LogType;
import ua.napps.scorekeeper.settings.LocalSettings;
import ua.napps.scorekeeper.utils.AndroidFirebaseAnalytics;
import ua.napps.scorekeeper.utils.Singleton;
import ua.napps.scorekeeper.utils.Utilities;

import static ua.napps.scorekeeper.counters.CountersAdapter.DECREASE_VALUE_CLICK;
import static ua.napps.scorekeeper.counters.CountersAdapter.INCREASE_VALUE_CLICK;

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
        recyclerView = contentView.findViewById(R.id.recycler_view);
        recyclerView.setItemAnimator(new ChangeCounterValueAnimator());
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
        emptyState = contentView.findViewById(R.id.empty_state);
        emptyState.setOnClickListener(view -> viewModel.addCounter());

        return contentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        CountersViewModelFactory factory = new CountersViewModelFactory(requireActivity().getApplication());
        viewModel = ViewModelProviders.of(this, factory).get(CountersViewModel.class);
        countersAdapter = new CountersAdapter(getResources().getInteger(R.integer.max_counters_to_fit), this, this);
        subscribeUi();
        isLongPressTipShowed = LocalSettings.getLongPressTipShowed();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.counters_menu, menu);
    }

    private void showDialogWithAction(int message, final DialogPositiveClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setMessage(message)
                .setPositiveButton(R.string.dialog_yes, (dialog, id) -> listener.onPositiveButtonClicked(getActivity()))
                .setNegativeButton(R.string.dialog_no, (dialog, id) -> dialog.dismiss());
        builder.create().show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_counter:
                if (viewModel.getCounters().getValue() != null) {
                    viewModel.addCounter();
                } else {
                    subscribeUi();
                }
                break;
            case R.id.menu_remove_all:
                AndroidFirebaseAnalytics.logEvent("menu_remove_all");
                DialogPositiveClickListener dialogListenerRemove = new DialogPositiveClickListener() {
                    @Override
                    public void onPositiveButtonClicked(Context context) {
                        viewModel.removeAll();
                    }
                };
                showDialogWithAction(R.string.dialog_confirmation_question, dialogListenerRemove);
                break;
            case R.id.menu_reset_all:
                AndroidFirebaseAnalytics.logEvent("menu_reset_all");
                DialogPositiveClickListener dialogListenerReset = new DialogPositiveClickListener() {
                    @Override
                    public void onPositiveButtonClicked(Context context) {
                        viewModel.resetAll();
                    }
                };
                showDialogWithAction(R.string.dialog_confirmation_question, dialogListenerReset);
                break;
            case R.id.menu_log:
                Intent intent = new Intent(getActivity(), LogActivity.class);
                startActivity(intent);
                AndroidFirebaseAnalytics.logEvent("menu_log");
                break;
            case R.id.menu_request:
                startEmailClient();
                AndroidFirebaseAnalytics.logEvent("menu_request_a_feature");
                break;
            case R.id.menu_rate:
                rateApp();
                AndroidFirebaseAnalytics.logEvent("menu_rate_app");
                break;
        }
        return true;
    }

    private void subscribeUi() {
        final Context context = getContext();
        viewModel.getCounters().observe(getViewLifecycleOwner(), counters -> {
            if (counters != null) {
                boolean databaseVersionMigration = true;
                for (Counter counter : counters) {
                    if (counters.size() > 1 && counter.getPosition() != 0) {
                        databaseVersionMigration = false;
                    }
                }
                if (databaseVersionMigration) {
                    for (int i = 0; i < counters.size(); i++) {
                        viewModel.setPositionAfterDBMigration(counters.get(i), i);
                    }
                }
                final int size = counters.size();
                emptyState.setVisibility(size > 0 ? View.GONE : View.VISIBLE);

                if (oldListSize != size) {
                    countersAdapter.notifyDataSetChanged();
                    if (size > countersAdapter.getMaxFitCounters()) {
                        if (((GridLayoutManager) recyclerView.getLayoutManager()).getSpanCount() != 2) {
                            recyclerView.setLayoutManager(new GridLayoutManager(context, 2));
                        }
                    } else {
                        if (((GridLayoutManager) recyclerView.getLayoutManager()).getSpanCount() != 1) {
                            recyclerView.setLayoutManager(new GridLayoutManager(context, 2));
                        }
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
                                countersAdapter.setContainerHeight(recyclerView.getHeight());
                                recyclerView.setAdapter(countersAdapter);
                                ItemTouchHelper.Callback callback = new ItemDragHelperCallback(countersAdapter);
                                itemTouchHelper = new ItemTouchHelper(callback);
                                itemTouchHelper.attachToRecyclerView(recyclerView);
                            }
                    );
                    Bundle params = new Bundle();
                    params.putString(com.google.firebase.analytics.FirebaseAnalytics.Param.CHARACTER, "" + size);
                    AndroidFirebaseAnalytics.logEvent("active_counters", params);
                }
            } else {
                emptyState.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onEditClick(View view, Counter counter) {
        EditCounterActivity.start(getActivity(), counter, view);
        Bundle params = new Bundle();
        params.putString(com.google.firebase.analytics.FirebaseAnalytics.Param.CHARACTER, "edit");
        AndroidFirebaseAnalytics.logEvent("counter_header_click", params);
    }

    @Override
    public void onDecreaseClick(Counter counter) {
        Singleton.getInstance().addLogEntry(new LogEntry(counter, LogType.DEC, 1, counter.getValue()));

        viewModel.decreaseCounter(counter);
        showLongPressHint();
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
    public void onStart() {
        super.onStart();
        AndroidFirebaseAnalytics.trackScreen(getActivity(), "Counters List");
    }

    @Override
    public void onIncreaseClick(Counter counter) {
        Singleton.getInstance().addLogEntry(new LogEntry(counter, LogType.INC, 1, counter.getValue()));

        viewModel.increaseCounter(counter);
        showLongPressHint();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onLongClick(Counter counter, int position, boolean isIncrease) {
        AndroidFirebaseAnalytics.logEvent("counter_long_click");
        final MaterialDialog.Builder builder = new MaterialDialog.Builder(requireActivity());

        final Observer<Counter> counterObserver = c -> {
            if (longClickDialog != null && c != null) {
                longClickDialog.getTitleView().setText(c.getName());
            }
        };

        final LiveData<Counter> liveData = viewModel.getCounterLiveData(counter.getId());
        liveData.observe(this, counterObserver);

        int layoutId = isIncrease ? R.layout.dialog_counter_step_increase : R.layout.dialog_counter_step_decrease;
        final View contentView = LayoutInflater.from(requireActivity()).inflate(layoutId, null, false);
        View btnCustomValue = contentView.findViewById(R.id.btn_add_custom_value);
        btnCustomValue.setEnabled(false);

        String btnSign = "-";
        if (isIncrease) {
            btnSign = "+";
        }

        ((TextView) contentView.findViewById(R.id.btn_one_text)).setText(btnSign + String.valueOf(LocalSettings.getCustomCounter(1)));
        contentView.findViewById(R.id.btn_one).setOnClickListener(v -> {
            int value = LocalSettings.getCustomCounter(1);
            if (isIncrease) {
                Singleton.getInstance().addLogEntry(new LogEntry(counter, LogType.INC_C, value, counter.getValue()));

                viewModel.increaseCounter(counter, value);
                countersAdapter.notifyItemChanged(position, INCREASE_VALUE_CLICK);
            } else {
                Singleton.getInstance().addLogEntry(new LogEntry(counter, LogType.DEC_C, -value, counter.getValue()));

                viewModel.decreaseCounter(counter, -value);
                countersAdapter.notifyItemChanged(position, DECREASE_VALUE_CLICK);
            }
            longClickDialog.dismiss();
        });

        ((TextView) contentView.findViewById(R.id.btn_two_text)).setText(btnSign + String.valueOf(LocalSettings.getCustomCounter(2)));
        contentView.findViewById(R.id.btn_two).setOnClickListener(v -> {
            int value = LocalSettings.getCustomCounter(2);
            if (isIncrease) {
                Singleton.getInstance().addLogEntry(new LogEntry(counter, LogType.INC_C, value, counter.getValue()));

                viewModel.increaseCounter(counter, value);
                countersAdapter.notifyItemChanged(position, INCREASE_VALUE_CLICK);
            } else {
                Singleton.getInstance().addLogEntry(new LogEntry(counter, LogType.DEC_C, value, counter.getValue()));

                viewModel.decreaseCounter(counter, -value);
                countersAdapter.notifyItemChanged(position, DECREASE_VALUE_CLICK);
            }
            longClickDialog.dismiss();
        });

        ((TextView) contentView.findViewById(R.id.btn_three_text)).setText(btnSign + String.valueOf(LocalSettings.getCustomCounter(3)));
        contentView.findViewById(R.id.btn_three).setOnClickListener(v -> {
            int value = LocalSettings.getCustomCounter(3);
            if (isIncrease) {
                Singleton.getInstance().addLogEntry(new LogEntry(counter, LogType.INC_C, value, counter.getValue()));

                viewModel.increaseCounter(counter, value);
                countersAdapter.notifyItemChanged(position, INCREASE_VALUE_CLICK);
            } else {
                Singleton.getInstance().addLogEntry(new LogEntry(counter, LogType.DEC_C, value, counter.getValue()));

                viewModel.decreaseCounter(counter, -value);
                countersAdapter.notifyItemChanged(position, DECREASE_VALUE_CLICK);
            }
            longClickDialog.dismiss();
        });

        ((TextView) contentView.findViewById(R.id.btn_four_text)).setText(btnSign + String.valueOf(LocalSettings.getCustomCounter(4)));
        contentView.findViewById(R.id.btn_four).setOnClickListener(v -> {
            int value = LocalSettings.getCustomCounter(4);
            if (isIncrease) {
                Singleton.getInstance().addLogEntry(new LogEntry(counter, LogType.INC_C, value, counter.getValue()));

                viewModel.increaseCounter(counter, value);
                countersAdapter.notifyItemChanged(position, INCREASE_VALUE_CLICK);
            } else {
                Singleton.getInstance().addLogEntry(new LogEntry(counter, LogType.DEC_C, value, counter.getValue()));

                viewModel.decreaseCounter(counter, -value);
                countersAdapter.notifyItemChanged(position, DECREASE_VALUE_CLICK);
            }
            longClickDialog.dismiss();
        });

        final EditText editText = contentView.findViewById(R.id.et_add_custom_value);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnCustomValue.setEnabled(s.length() > 0);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        editText.setOnEditorActionListener((textView, actionId, event) -> {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId
                    == EditorInfo.IME_ACTION_DONE)) {
                final String value = editText.getText().toString();
                if (!TextUtils.isEmpty(value)) {
                    int intValue = Utilities.parseInt(value);
                    if (isIncrease) {
                        Singleton.getInstance().addLogEntry(new LogEntry(counter, LogType.INC_C, intValue, counter.getValue()));

                        viewModel.increaseCounter(counter, intValue);
                        countersAdapter.notifyItemChanged(position, INCREASE_VALUE_CLICK);
                    } else {
                        Singleton.getInstance().addLogEntry(new LogEntry(counter, LogType.DEC_C, intValue, counter.getValue()));

                        viewModel.decreaseCounter(counter, -intValue);
                        countersAdapter.notifyItemChanged(position, DECREASE_VALUE_CLICK);
                    }
                }
                longClickDialog.dismiss();
            }
            return false;
        });

        btnCustomValue.setOnClickListener(v -> {
            final String value = editText.getText().toString();
            if (TextUtils.isEmpty(value)) {
                longClickDialog.dismiss();
                return;
            }
            int intValue = Utilities.parseInt(value);
            if (isIncrease) {
                Singleton.getInstance().addLogEntry(new LogEntry(counter, LogType.INC_C, intValue, counter.getValue()));

                viewModel.increaseCounter(counter, intValue);
                countersAdapter.notifyItemChanged(position, INCREASE_VALUE_CLICK);
            } else {
                Singleton.getInstance().addLogEntry(new LogEntry(counter, LogType.DEC_C, intValue, counter.getValue()));

                viewModel.decreaseCounter(counter, -intValue);
                countersAdapter.notifyItemChanged(position, DECREASE_VALUE_CLICK);
            }
            longClickDialog.dismiss();
        });
        builder.customView(contentView, false);
        builder.title(R.string.dialog_current_value_title);
        builder.dismissListener(dialogInterface -> liveData.removeObserver(counterObserver));
        longClickDialog = builder.build();
        longClickDialog.show();
        InputMethodManager inputMethodManager = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
    }

    @Override
    public void onNameClick(Counter counter) {
        final MaterialDialog md = new MaterialDialog.Builder(requireActivity())
                .content(R.string.counter_details_name)
                .inputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT | InputType.TYPE_TEXT_VARIATION_PERSON_NAME)
                .positiveText(R.string.common_set)
                .inputRange(1, 20)
                .negativeColorRes(R.color.primaryColor)
                .negativeText(R.string.common_cancel)
                .input(counter.getName(), null, false, (dialog, input) -> viewModel.modifyName(counter, input.toString()))
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
        Bundle params = new Bundle();
        params.putString(com.google.firebase.analytics.FirebaseAnalytics.Param.CHARACTER, "name");
        AndroidFirebaseAnalytics.logEvent("counter_header_click", params);
    }

    public void scrollToTop() {
        if (recyclerView != null) {
            recyclerView.smoothScrollToPosition(0);
        }
        AndroidFirebaseAnalytics.logEvent("scroll_to_top");
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        itemTouchHelper.startDrag(viewHolder);
    }

    @Override
    public void afterDrag(Counter counter, int fromPosition, int toPosition) {
        viewModel.modifyPosition(counter, fromPosition, toPosition);
    }

    private void rateApp() {
        Activity activity = requireActivity();
        Uri uri = Uri.parse("market://details?id=" + activity.getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            activity.startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            Uri playStoreUri = Uri.parse("http://play.google.com/store/apps/details?id=" + activity.getPackageName());
            activity.startActivity(new Intent(Intent.ACTION_VIEW, playStoreUri));
        }
    }

    private void startEmailClient() {
        final String title = getString(R.string.app_name);

        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "scorekeeper.feedback@gmail.com", null));
        intent.putExtra(Intent.EXTRA_EMAIL, "scorekeeper.feedback@gmail.com");
        intent.putExtra(Intent.EXTRA_SUBJECT, title);
        if (intent.resolveActivity(App.getInstance().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(getContext(), R.string.error_no_email_client, Toast.LENGTH_SHORT).show();
        }
    }
}
