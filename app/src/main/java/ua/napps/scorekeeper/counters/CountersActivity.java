package ua.napps.scorekeeper.counters;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.MaterialDialog.Builder;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.firebase.analytics.FirebaseAnalytics.Param;
import timber.log.Timber;
import ua.com.napps.scorekeeper.R;
import ua.napps.scorekeeper.app.ScoreKeeperApp;
import ua.napps.scorekeeper.dice.DiceActivity;
import ua.napps.scorekeeper.settings.SettingsFragment;
import ua.napps.scorekeeper.settings.SettingsUtil;
import ua.napps.scorekeeper.storage.DatabaseHolder;
import ua.napps.scorekeeper.storage.TinyDB;

public class CountersActivity extends AppCompatActivity implements CounterActionCallback,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private SettingsFragment bottomSheetFragment;

    private CountersAdapter countersAdapter;

    private View emptyState;

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
        emptyState = findViewById(R.id.empty_state);
        emptyState.setOnClickListener(view -> {
            viewModel.addCounter();
            ((ScoreKeeperApp) getApplication()).getFirebaseAnalytics().logEvent("empty_state_add_counter", null);
        });
        settingsDB = new TinyDB(getApplicationContext());
        settingsDB.registerOnSharedPreferenceChangeListener(this);
        applyKeepScreenOn(true);
        final boolean isTryToFitAllCounters = settingsDB
                .getBoolean(SettingsUtil.SETTINGS_TRY_TO_FIT_ALL_COUNTERS, false);
        countersAdapter = new CountersAdapter(this);
        countersAdapter.setTryToFitAllCounters(isTryToFitAllCounters);
        viewModel = getViewModel();
        FlexboxLayoutManager layoutManager =
                new FlexboxLayoutManager(this, FlexDirection.COLUMN, FlexWrap.NOWRAP);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(countersAdapter);
        recyclerView.setItemAnimator(null);

        subscribeToModel();
        Bundle params = new Bundle(1);
        params.putString(Param.ITEM_VARIANT, isTryToFitAllCounters ? "on" : "off");
        ((ScoreKeeperApp) getApplication()).getFirebaseAnalytics()
                .logEvent("settings_try_to_fit_all_counters", params);
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
    public void onIncreaseClick(Counter counter) {
        viewModel.increaseCounter(counter);
    }

    @Override
    public void onLongClick(Counter counter, boolean isIncrease) {
        ((ScoreKeeperApp) getApplication()).getFirebaseAnalytics().logEvent("counter_long_click", null);
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
        contentView.findViewById(R.id.btn_add_custom_value).setOnClickListener(view -> {
            final EditText editText = contentView.findViewById(R.id.et_add_custom_value);
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
                Timber.e(e, "value: %s", value);
            } finally {
                longClickDialog.dismiss();
            }
        });
        builder.customView(contentView, false);
        builder.title(R.string.dialog_current_value_title);
        builder.dismissListener(dialogInterface -> liveData.removeObserver(counterObserver));
        longClickDialog = builder.build();
        longClickDialog.show();
    }

    @Override
    public void onNameClick(int counterId) {
        final Intent intent = EditCounterActivity.getIntent(this, counterId);
        startActivityForResult(intent, EditCounterActivity.REQUEST_CODE);
        ((ScoreKeeperApp) getApplication()).getFirebaseAnalytics().logEvent("counter_header_click", null);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_counter:
                viewModel.addCounter();
                ((ScoreKeeperApp) getApplication()).getFirebaseAnalytics().logEvent("menu_add_counter", null);
                break;
            case R.id.menu_remove_all:
                viewModel.removeAll();
                ((ScoreKeeperApp) getApplication()).getFirebaseAnalytics().logEvent("menu_remove_all", null);
                break;
            case R.id.menu_reset_all:
                viewModel.resetAll();
                ((ScoreKeeperApp) getApplication()).getFirebaseAnalytics().logEvent("menu_reset_all", null);
                break;
            case R.id.menu_settings:
                showBottomSheetFragment();
                ((ScoreKeeperApp) getApplication()).getFirebaseAnalytics().logEvent("menu_settings", null);
                break;
            case R.id.menu_dice:
                Intent intent = new Intent(this, DiceActivity.class);
                startActivity(intent);
                ((ScoreKeeperApp) getApplication()).getFirebaseAnalytics().logEvent("menu_dice", null);
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
            case SettingsUtil.SETTINGS_KEEP_SCREEN_ON:
                applyKeepScreenOn(false);
                break;
            case SettingsUtil.SETTINGS_TRY_TO_FIT_ALL_COUNTERS:
                final boolean newValue = settingsDB.getBoolean(SettingsUtil.SETTINGS_TRY_TO_FIT_ALL_COUNTERS, false);
                countersAdapter.setTryToFitAllCounters(newValue);
                countersAdapter = new CountersAdapter(this);
                countersAdapter.setTryToFitAllCounters(newValue);
                recyclerView.setAdapter(countersAdapter);
                subscribeToModel();
                break;
        }
    }

    private void applyKeepScreenOn(boolean trackAnalytics) {
        final boolean isStayAwake = settingsDB.getBoolean(SettingsUtil.SETTINGS_KEEP_SCREEN_ON, true);
        if (isStayAwake) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        if (trackAnalytics) {
            Bundle params = new Bundle(1);
            params.putString(Param.ITEM_VARIANT, isStayAwake ? "on" : "off");
            ((ScoreKeeperApp) getApplication()).getFirebaseAnalytics().logEvent("settings_keep_screen_on", params);
        }
    }

    private CountersViewModel getViewModel() {
        CountersDao countersDao = DatabaseHolder.database().countersDao();
        CountersViewModelFactory factory =
                new CountersViewModelFactory((ScoreKeeperApp) getApplication(), countersDao);
        return ViewModelProviders.of(this, factory).get(CountersViewModel.class);
    }

    private void showBottomSheetFragment() {
        bottomSheetFragment = new SettingsFragment();
        bottomSheetFragment.show(getSupportFragmentManager(), "SettingsFragment");
    }

    private void subscribeToModel() {
        // Update the list when the data changes
        viewModel.getCounters().observe(this, counters -> {
            if (counters != null) {
                final int size = counters.size();
                emptyState.setVisibility(size > 0 ? View.GONE : View.VISIBLE);
                assert countersAdapter != null;
                countersAdapter.setCountersList(counters);

                if (!countersAdapter.isTryToFitAllCounters()) {
                    if (size <= SettingsUtil.MAX_COUNTERS_TO_FIT_ON_SCREEN) {
                        if (((FlexboxLayoutManager) recyclerView.getLayoutManager()).getFlexWrap()
                                != FlexWrap.NOWRAP) {
                            FlexboxLayoutManager layoutManager =
                                    new FlexboxLayoutManager(CountersActivity.this, FlexDirection.COLUMN,
                                            FlexWrap.NOWRAP);
                            recyclerView.setLayoutManager(layoutManager);
                        }
                    } else {
                        if (((FlexboxLayoutManager) recyclerView.getLayoutManager()).getFlexWrap()
                                != FlexWrap.WRAP) {
                            FlexboxLayoutManager layoutManager =
                                    new FlexboxLayoutManager(CountersActivity.this, FlexDirection.ROW, FlexWrap.WRAP);
                            recyclerView.setLayoutManager(layoutManager);
                        }
                        if (oldListSize < size) {
                            recyclerView.smoothScrollToPosition(size);
                        }
                    }
                } else {
                    if (((FlexboxLayoutManager) recyclerView.getLayoutManager()).getFlexWrap()
                            != FlexWrap.NOWRAP) {
                        FlexboxLayoutManager layoutManager =
                                new FlexboxLayoutManager(this, FlexDirection.COLUMN, FlexWrap.NOWRAP);
                        recyclerView.setLayoutManager(layoutManager);
                    }
                }
                oldListSize = size;
            } else {
                emptyState.setVisibility(View.VISIBLE);
            }
        });
    }
}