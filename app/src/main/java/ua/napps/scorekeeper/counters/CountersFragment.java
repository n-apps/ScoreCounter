package ua.napps.scorekeeper.counters;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.support.v7.widget.Toolbar;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.analytics.FirebaseAnalytics;

import ua.com.napps.scorekeeper.R;
import ua.napps.scorekeeper.settings.LocalSettings;
import ua.napps.scorekeeper.utils.AndroidFirebaseAnalytics;
import ua.napps.scorekeeper.utils.GroupSnapHelper;
import ua.napps.scorekeeper.utils.Utilities;

import static ua.napps.scorekeeper.counters.CountersAdapter.DECREASE_VALUE_CLICK;
import static ua.napps.scorekeeper.counters.CountersAdapter.INCREASE_VALUE_CLICK;

public class CountersFragment extends Fragment implements CounterActionCallback {

    private RecyclerView recyclerView;
    private CountersAdapter countersAdapter;
    private View emptyState;
    private CountersViewModel viewModel;
    private boolean isFirstLoad = true;
    private MaterialDialog longClickDialog;
    private int oldListSize;
    private boolean isLongPressTipShowed;

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
        recyclerView.setLayoutManager(new GridLayoutManager(requireActivity(), 1));
        SnapHelper snapHelper = new GroupSnapHelper(2);
        snapHelper.attachToRecyclerView(recyclerView);
        recyclerView.setOnFlingListener(snapHelper);
        emptyState = contentView.findViewById(R.id.empty_state);
        emptyState.setOnClickListener(view -> viewModel.addCounter());

        return contentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        CountersViewModelFactory factory = new CountersViewModelFactory(requireActivity().getApplication());
        viewModel = ViewModelProviders.of(this, factory).get(CountersViewModel.class);
        countersAdapter = new CountersAdapter(getResources().getInteger(R.integer.max_counters_to_fit), this);
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
                viewModel.removeAll();
                AndroidFirebaseAnalytics.logEvent("menu_remove_all");
                break;
            case R.id.menu_reset_all:
                viewModel.resetAll();
                AndroidFirebaseAnalytics.logEvent("menu_reset_all");
                break;
        }
        return true;
    }

    private void subscribeUi() {
        viewModel.getCounters().observe(this, counters -> {
            if (counters != null) {
                final int size = counters.size();
                viewModel.setListSize(size);
                emptyState.setVisibility(size > 0 ? View.GONE : View.VISIBLE);

                if (oldListSize != size) {
                    countersAdapter.notifyDataSetChanged();
                    if (size > countersAdapter.getMaxFitCounters()) {
                        if (((GridLayoutManager) recyclerView.getLayoutManager()).getSpanCount() != 2) {
                            recyclerView.setLayoutManager(new GridLayoutManager(requireActivity(), 2));
                        }
                    } else {
                        if (((GridLayoutManager) recyclerView.getLayoutManager()).getSpanCount() != 1) {
                            recyclerView.setLayoutManager(new GridLayoutManager(requireActivity(), 1));
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
                            }
                    );
                    Bundle params = new Bundle();
                    params.putString(FirebaseAnalytics.Param.CHARACTER, "" + size);
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
        params.putString(FirebaseAnalytics.Param.CHARACTER, "edit");
        AndroidFirebaseAnalytics.logEvent("counter_header_click", params);
    }

    @Override
    public void onDecreaseClick(Counter counter) {
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
        Button buttonAddValue = contentView.findViewById(R.id.btn_add_custom_value);
        contentView.findViewById(R.id.btn_one).setOnClickListener(v -> {
            if (isIncrease) {
                viewModel.increaseCounter(counter, 5);
                countersAdapter.notifyItemChanged(position, INCREASE_VALUE_CLICK);
            } else {
                viewModel.decreaseCounter(counter, -5);
                countersAdapter.notifyItemChanged(position, DECREASE_VALUE_CLICK);
            }
            longClickDialog.dismiss();
        });
        contentView.findViewById(R.id.btn_two).setOnClickListener(v -> {
            if (isIncrease) {
                viewModel.increaseCounter(counter, 10);
                countersAdapter.notifyItemChanged(position, INCREASE_VALUE_CLICK);
            } else {
                viewModel.decreaseCounter(counter, -10);
                countersAdapter.notifyItemChanged(position, DECREASE_VALUE_CLICK);
            }
            longClickDialog.dismiss();
        });
        contentView.findViewById(R.id.btn_three).setOnClickListener(v -> {
            if (isIncrease) {
                viewModel.increaseCounter(counter, 15);
                countersAdapter.notifyItemChanged(position, INCREASE_VALUE_CLICK);
            } else {
                viewModel.decreaseCounter(counter, -15);
                countersAdapter.notifyItemChanged(position, DECREASE_VALUE_CLICK);
            }
            longClickDialog.dismiss();
        });
        contentView.findViewById(R.id.btn_four).setOnClickListener(v -> {
            if (isIncrease) {
                viewModel.increaseCounter(counter, 30);
                countersAdapter.notifyItemChanged(position, INCREASE_VALUE_CLICK);
            } else {
                viewModel.decreaseCounter(counter, -30);
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
                buttonAddValue.setEnabled(s.length() > 0);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        editText.setOnEditorActionListener((textView, actionId, event) -> {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId
                    == EditorInfo.IME_ACTION_DONE)) {
                final String value = editText.getText().toString();
                if (TextUtils.isEmpty(value)) {
                    longClickDialog.dismiss();
                }
                if (isIncrease) {
                    viewModel.increaseCounter(counter, Utilities.parseInt(value));
                    countersAdapter.notifyItemChanged(position, INCREASE_VALUE_CLICK);
                } else {
                    viewModel.decreaseCounter(counter, -Utilities.parseInt(value));
                    countersAdapter.notifyItemChanged(position, DECREASE_VALUE_CLICK);
                }
                longClickDialog.dismiss();
            }
            return false;
        });

        buttonAddValue.setOnClickListener(view -> {
            final String value = editText.getText().toString();
            if (TextUtils.isEmpty(value)) {
                longClickDialog.dismiss();
                return;
            }
            if (isIncrease) {
                viewModel.increaseCounter(counter, Utilities.parseInt(value));
                countersAdapter.notifyItemChanged(position, INCREASE_VALUE_CLICK);
            } else {
                viewModel.decreaseCounter(counter, -Utilities.parseInt(value));
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
        params.putString(FirebaseAnalytics.Param.CHARACTER, "name");
        AndroidFirebaseAnalytics.logEvent("counter_header_click", params);
    }

    public void scrollToTop() {
        if (recyclerView != null) {
            recyclerView.smoothScrollToPosition(0);
        }
        AndroidFirebaseAnalytics.logEvent("scroll_to_top");
    }
}
