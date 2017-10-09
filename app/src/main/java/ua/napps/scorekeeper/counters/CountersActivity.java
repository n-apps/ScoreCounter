package ua.napps.scorekeeper.counters;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import io.paperdb.Paper;
import ua.com.napps.scorekeeper.R;
import ua.com.napps.scorekeeper.databinding.ActivityCountersBinding;
import ua.napps.scorekeeper.app.ScoreKeeperApp;
import ua.napps.scorekeeper.settings.SettingsActivity;
import ua.napps.scorekeeper.utils.Constants;
import ua.napps.scorekeeper.utils.NoChangeAnimator;

public class CountersActivity extends AppCompatActivity implements CounterActionCallback {

  private ActivityCountersBinding binding;
  private CountersAdapter mProductAdapter;
  private CountersViewModel viewModel;
  private int oldListSize;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = DataBindingUtil.setContentView(this, R.layout.activity_counters);

    setSupportActionBar(binding.toolbar);

    mProductAdapter = new CountersAdapter(this);

    viewModel = getViewModel();

    FlexboxLayoutManager layoutManager =
        new FlexboxLayoutManager(this, FlexDirection.COLUMN, FlexWrap.NOWRAP);
    binding.recyclerView.setLayoutManager(layoutManager);
    binding.recyclerView.setAdapter(mProductAdapter);
    binding.recyclerView.setItemAnimator(new NoChangeAnimator());

    subscribeUi();
  }

  private CountersViewModel getViewModel() {
    CountersDao countersDao = DatabaseHolder.database().countersDao();
    CountersViewModelFactory factory =
        new CountersViewModelFactory((ScoreKeeperApp) getApplication(), countersDao);
    return ViewModelProviders.of(this, factory).get(CountersViewModel.class);
  }

  private void subscribeUi() {
    // Update the list when the data changes
    viewModel.getProducts().observe(this, counters -> {
      if (counters != null) {
        final int size = counters.size();
        binding.emptyState.setVisibility(size > 0 ? View.GONE : View.VISIBLE);
        mProductAdapter.setProductList(counters);
        if (size <= 4) {
          if (((FlexboxLayoutManager) binding.recyclerView.getLayoutManager()).getFlexWrap()
              != FlexWrap.NOWRAP) {
            FlexboxLayoutManager layoutManager =
                new FlexboxLayoutManager(CountersActivity.this, FlexDirection.COLUMN,
                    FlexWrap.NOWRAP);
            binding.recyclerView.setLayoutManager(layoutManager);
          }
        } else {
          if (((FlexboxLayoutManager) binding.recyclerView.getLayoutManager()).getFlexWrap()
              != FlexWrap.WRAP) {
            FlexboxLayoutManager layoutManager =
                new FlexboxLayoutManager(CountersActivity.this, FlexDirection.ROW, FlexWrap.WRAP);
            binding.recyclerView.setLayoutManager(layoutManager);
          }
          if (oldListSize < size) {
            binding.recyclerView.smoothScrollToPosition(size);
          }
        }
        oldListSize = size;
      } else {
        binding.emptyState.setVisibility(View.VISIBLE);
      }
    });
  }

  private void loadSettings() {
    final boolean isKeepScreenOn =
        Paper.book(Constants.SETTINGS).read(Constants.SETTINGS_STAY_AWAKE, true);
    if (isKeepScreenOn) {
      getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    } else {
      getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.counters_menu, menu);
    return true;
  }

  @Override public boolean onPrepareOptionsMenu(Menu menu) {
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

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.menu_add_counter:
        addCounter();
        break;
      case R.id.menu_remove_all:
        viewModel.removeAll();
        break;
      case R.id.menu_reset_all:
        viewModel.resetAll();
        break;
      case R.id.menu_settings:
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        break;
    }
    return true;
  }

  private void addCounter() {
    viewModel.addCounter();
  }

  @Override public void onNameClick(Counter counter) {
    final Intent intent = EditCounterActivity.getIntent(this, counter.getId());
    startActivityForResult(intent, EditCounterActivity.REQUEST_CODE);
  }

  @Override public boolean onNameLongClick(View v, final Counter counter) {
    assert counter != null;
    new MaterialDialog.Builder(this).content("Counter name")
        .inputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
            | InputType.TYPE_CLASS_TEXT
            | InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE)
        .input("Hint", counter.getName(), new MaterialDialog.InputCallback() {
          @Override public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
            counter.setName(input.toString());
          }
        })
        .widgetColor(Color.parseColor(counter.getColor()))
        .positiveText("Set")
        .show();
    return true;
  }

  @Override public boolean onValueLongClick(View v, final Counter counter) {
    assert counter != null;
    new MaterialDialog.Builder(this).content("Counter value")
        .inputType(InputType.TYPE_CLASS_NUMBER)
        .input("Hint", String.valueOf(counter.getValue()), new MaterialDialog.InputCallback() {
          @Override public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
            counter.setValue(Integer.parseInt(String.valueOf(input)));
          }
        })
        .widgetColor(Color.parseColor(counter.getColor()))
        .positiveText("Set")
        .show();
    return true;
  }

  @Override public void onIncreaseClick(Counter counter) {
    viewModel.increaseCounter(counter);
  }

  @Override public void onDecreaseClick(Counter counter) {
    viewModel.decreaseCounter(counter);
  }

  @Override public void onAddCounterClick() {
    addCounter();
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == EditCounterActivity.REQUEST_CODE) {
      if (resultCode == EditCounterActivity.RESULT_DELETE) {
        invalidateOptionsMenu();
      }
    }
    super.onActivityResult(requestCode, resultCode, data);
  }

  @Override protected void onDestroy() {
    binding.unbind();
    super.onDestroy();
  }
}