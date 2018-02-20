package ua.napps.scorekeeper.counters;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
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
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.MaterialDialog.Builder;
import com.github.fernandodev.easyratingdialog.library.EasyRatingDialog;
import com.google.firebase.analytics.FirebaseAnalytics.Param;

import ua.com.napps.scorekeeper.R;
import ua.napps.scorekeeper.app.ScoreKeeperApp;
import ua.napps.scorekeeper.dice.DiceActivity;
import ua.napps.scorekeeper.settings.Constants;
import ua.napps.scorekeeper.settings.SettingsFragment;
import ua.napps.scorekeeper.storage.DatabaseHolder;
import ua.napps.scorekeeper.storage.TinyDB;
import ua.napps.scorekeeper.utils.AndroidFirebaseAnalytics;

public class CountersActivity extends AppCompatActivity implements CounterActionCallback,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private SettingsFragment bottomSheetFragment;

    private CountersAdapter countersAdapter;

    private EasyRatingDialog easyRatingDialog;

    private View emptyState;

    private boolean isFirstLoad = true;

    private MaterialDialog longClickDialog;

    private int oldListSize;

    private RecyclerView recyclerView;

    private TinyDB settingsDB;

    private CountersViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_counters);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(null);
        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);
        emptyState = findViewById(R.id.empty_state);
        emptyState.setOnClickListener(view -> {
            viewModel.addCounter();
            AndroidFirebaseAnalytics.logEvent(getApplicationContext(), "empty_state_add_counter");
        });
        settingsDB = new TinyDB(getApplicationContext());
        settingsDB.registerOnSharedPreferenceChangeListener(this);
        applyKeepScreenOn(true);

        countersAdapter = new CountersAdapter(this);
        viewModel = getViewModel();
        easyRatingDialog = new EasyRatingDialog(this);
        subscribeToModel();
    }

    @Override
    protected void onStart() {
        super.onStart();
        easyRatingDialog.onStart();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EditCounterActivity.REQUEST_CODE) {
            if (resultCode == EditCounterActivity.RESULT_DELETE) {
                invalidateOptionsMenu();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        easyRatingDialog.showIfNeeded();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        settingsDB.unregisterOnSharedPreferenceChangeListener(this);
        settingsDB = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.counters_menu, menu);
        return true;
    }

    @Override
    public void onDecreaseClick(Counter counter) {
        viewModel.decreaseCounter(counter);
    }

    @Override
    public void onEditClick(int counterId) {
        EditCounterActivity.start(this, counterId);
        Bundle params = new Bundle();
        params.putString(Param.CHARACTER, "edit");
        AndroidFirebaseAnalytics.logEvent(getApplicationContext(), "counter_header_click", params);
    }

    @Override
    public void onIncreaseClick(Counter counter) {
        viewModel.increaseCounter(counter);
    }

    @Override
    public void onLongClick(Counter counter, boolean isIncrease) {
        AndroidFirebaseAnalytics.logEvent(getApplicationContext(), "counter_long_click");
        final MaterialDialog.Builder builder = new Builder(this);
        final Observer<Counter> counterObserver = c -> {
            if (longClickDialog != null && c != null) {
                longClickDialog.getTitleView()
                        .setText(getString(R.string.dialog_change_counter_value_title, c.getValue()));
            }
        };
        final LiveData<Counter> liveData = viewModel.getCounterLiveData(counter.getId());
        liveData.observe(this, counterObserver);
        View contentView = LayoutInflater.from(this)
                .inflate(isIncrease ? R.layout.dialog_counter_step_increase : R.layout.dialog_counter_step_decrease,
                        null, false);
        Button buttonAddValue = contentView.findViewById(R.id.btn_add_custom_value);

        contentView.findViewById(R.id.btn_one).setOnClickListener(v -> {
            if (isIncrease) {
                viewModel.increaseCounter(counter, 5);
            } else {
                viewModel.decreaseCounter(counter, -5);
            }
            longClickDialog.dismiss();
        });
        contentView.findViewById(R.id.btn_two).setOnClickListener(v -> {
            if (isIncrease) {
                viewModel.increaseCounter(counter, 10);
            } else {
                viewModel.decreaseCounter(counter, -10);
            }
            longClickDialog.dismiss();
        });
        contentView.findViewById(R.id.btn_three).setOnClickListener(v -> {
            if (isIncrease) {
                viewModel.increaseCounter(counter, 15);
            } else {
                viewModel.decreaseCounter(counter, -15);
            }
            longClickDialog.dismiss();
        });
        contentView.findViewById(R.id.btn_four).setOnClickListener(v -> {
            if (isIncrease) {
                viewModel.increaseCounter(counter, 30);
            } else {
                viewModel.decreaseCounter(counter, -30);
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
                try {
                    if (isIncrease) {
                        viewModel.increaseCounter(counter, Integer.parseInt(value));
                    } else {
                        viewModel.decreaseCounter(counter, -Integer.parseInt(value));
                    }
                } catch (NumberFormatException e) {
//                    Timber.e(e, "value: %s", value);
                } finally {
                    longClickDialog.dismiss();
                }
            }
            return false;
        });

        buttonAddValue.setOnClickListener(view -> {
            final String value = editText.getText().toString();
            if (TextUtils.isEmpty(value)) {
                longClickDialog.dismiss();
                return;
            }
            try {
                if (isIncrease) {
                    viewModel.increaseCounter(counter, Integer.parseInt(value));
                } else {
                    viewModel.decreaseCounter(counter, -Integer.parseInt(value));
                }
            } catch (NumberFormatException e) {
//                Timber.e(e, "value: %s", value);
            } finally {
                longClickDialog.dismiss();
            }
        });
        builder.customView(contentView, false);
        builder.title(R.string.dialog_current_value_title);
        builder.dismissListener(dialogInterface -> liveData.removeObserver(counterObserver));
        longClickDialog = builder.build();
        longClickDialog.show();
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    @Override
    public void onNameClick(Counter counter) {
        final MaterialDialog md = new Builder(CountersActivity.this)
                .content(R.string.counter_details_name)
                .inputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS | InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT | InputType.TYPE_TEXT_VARIATION_PERSON_NAME)
                .positiveText(R.string.common_set)
                .negativeText(R.string.common_cancel)
                .input(counter.getName(), null, true,
                        (dialog, input) -> {
                            if (input.length() > 0) {
                                viewModel.modifyName(counter, input.toString());
                            }
                        })
                .build();
        md.getInputEditText().setOnEditorActionListener((textView, actionId, event) -> {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId
                    == EditorInfo.IME_ACTION_DONE)) {
                View positiveButton = md.getActionButton(DialogAction.POSITIVE);
                positiveButton.callOnClick();
            }
            return false;
        });
        md.show();
        Bundle params = new Bundle();
        params.putString(Param.CHARACTER, "name");
        AndroidFirebaseAnalytics.logEvent(getApplicationContext(), "counter_header_click", params);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_counter:
                if (viewModel.getCounters().getValue() != null) {
                    viewModel.addCounter();
                } else {
                    subscribeToModel();
                }
                AndroidFirebaseAnalytics.logEvent(getApplicationContext(), "menu_add_counter");
                break;
            case R.id.menu_remove_all:
                viewModel.removeAll();
                AndroidFirebaseAnalytics.logEvent(getApplicationContext(), "menu_remove_all");
                break;
            case R.id.menu_reset_all:
                viewModel.resetAll();
                AndroidFirebaseAnalytics.logEvent(getApplicationContext(), "menu_reset_all");
                break;
            case R.id.menu_settings:
                AndroidFirebaseAnalytics.logEvent(getApplicationContext(), "menu_settings");
                break;
            case R.id.menu_dice:
                startActivity(DiceActivity.getIntent(this));
                AndroidFirebaseAnalytics.logEvent(getApplicationContext(), "menu_dice");
                break;
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem removeItem = menu.findItem(R.id.menu_remove_all);
        final boolean hasCounters = oldListSize > 0;
        if (removeItem != null) {
            removeItem.setEnabled(hasCounters);
        }
        MenuItem clearAllItem = menu.findItem(R.id.menu_reset_all);
        if (clearAllItem != null) {
            clearAllItem.setEnabled(hasCounters);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
        switch (key) {
            case Constants.SETTINGS_KEEP_SCREEN_ON:
                applyKeepScreenOn(false);
                break;
        }
    }

    private void applyKeepScreenOn(boolean trackAnalytics) {
        final boolean isStayAwake = settingsDB.getBoolean(Constants.SETTINGS_KEEP_SCREEN_ON, true);
        if (isStayAwake) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        if (trackAnalytics) {
            Bundle params = new Bundle();
            params.putLong(Param.SCORE, isStayAwake ? 1 : 0);
            AndroidFirebaseAnalytics.logEvent(getApplicationContext(), "settings_keep_screen_on", params);
        }
    }

    private CountersViewModel getViewModel() {
        CountersDao countersDao = DatabaseHolder.database().countersDao();
        CountersViewModelFactory factory =
                new CountersViewModelFactory((ScoreKeeperApp) getApplication(), countersDao);
        return ViewModelProviders.of(this, factory).get(CountersViewModel.class);
    }

    private void subscribeToModel() {
        viewModel.getCounters().observe(this, counters -> {
            if (counters != null) {
                final int size = counters.size();
                viewModel.setListSize(size);
                emptyState.setVisibility(size > 0 ? View.GONE : View.VISIBLE);
                countersAdapter.setCountersList(counters);
                if (size > oldListSize && oldListSize > 0) {
                    recyclerView.smoothScrollToPosition(size);
                }
                oldListSize = size;
                if (isFirstLoad) {
                    recyclerView.post(() -> {
                                countersAdapter.setContainerHeight(recyclerView.getHeight());
                                countersAdapter.setContainerWidth(recyclerView.getWidth());
                                recyclerView.setAdapter(countersAdapter);
                            }
                    );
                    Bundle params = new Bundle();
                    params.putLong(Param.SCORE, size);
                    AndroidFirebaseAnalytics.logEvent(getApplicationContext(), "starting_number_of_counters", params);
                    isFirstLoad = false;
                }
            } else {
                emptyState.setVisibility(View.VISIBLE);
            }
        });
    }
}