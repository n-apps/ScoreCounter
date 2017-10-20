package ua.napps.scorekeeper.counters;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import timber.log.Timber;
import ua.com.napps.scorekeeper.R;
import ua.napps.scorekeeper.app.ScoreKeeperApp;
import ua.napps.scorekeeper.settings.BottomSheetFragment;
import ua.napps.scorekeeper.utils.Constants;
import ua.napps.scorekeeper.utils.NoChangeAnimator;
import ua.napps.scorekeeper.utils.TinyDB;

public class CountersActivity extends AppCompatActivity implements CounterActionCallback {

    private CountersAdapter countersAdapter;

    private View emptyState;

    private BottomSheetFragment bottomSheetFragment;

    private int oldListSize;

    private RecyclerView recyclerView;

    private CountersViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_counters);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        recyclerView = findViewById(R.id.recycler_view);
        emptyState = findViewById(R.id.empty_state);
        countersAdapter = new CountersAdapter(this);
        viewModel = getViewModel();
        FlexboxLayoutManager layoutManager =
                new FlexboxLayoutManager(this, FlexDirection.COLUMN, FlexWrap.NOWRAP);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(countersAdapter);
        recyclerView.setItemAnimator(new NoChangeAnimator());

        subscribeUi();
        applySettings();
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
    public void onNameClick(Counter counter) {
        final Intent intent = EditCounterActivity.getIntent(this, counter.getId());
        startActivityForResult(intent, EditCounterActivity.REQUEST_CODE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_counter:
                viewModel.addCounter();
                break;
            case R.id.menu_remove_all:
                viewModel.removeAll();
                break;
            case R.id.menu_reset_all:
                viewModel.resetAll();
                break;
            case R.id.menu_settings:
                showBottomSheetFragment();
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

    private void applySettings() {
        final TinyDB settingsDB = new TinyDB(getApplicationContext());
        boolean isStayAwake = settingsDB.getBoolean(Constants.SETTINGS_STAY_AWAKE);

        if (isStayAwake) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        Timber.d("applySettings");
    }

    private CountersViewModel getViewModel() {
        CountersDao countersDao = DatabaseHolder.database().countersDao();
        CountersViewModelFactory factory =
                new CountersViewModelFactory((ScoreKeeperApp) getApplication(), countersDao);
        return ViewModelProviders.of(this, factory).get(CountersViewModel.class);
    }

    private void showBottomSheetFragment() {
        bottomSheetFragment = new BottomSheetFragment();
        bottomSheetFragment.show(getSupportFragmentManager(), "BottomSheetFragment");
    }

    private void subscribeUi() {
        // Update the list when the data changes
        viewModel.getCounters().observe(this, counters -> {
            if (counters != null) {
                final int size = counters.size();
                emptyState.setVisibility(size > 0 ? View.GONE : View.VISIBLE);
                countersAdapter.setCountersList(counters);
                if (size <= 4) {
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
                oldListSize = size;
            } else {
                emptyState.setVisibility(View.VISIBLE);
            }
        });
    }
}