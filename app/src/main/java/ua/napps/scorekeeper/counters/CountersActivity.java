package ua.napps.scorekeeper.counters;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableArrayList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import java.util.List;
import java.util.Random;
import ua.com.napps.scorekeeper.R;
import ua.com.napps.scorekeeper.databinding.ActivityCountersBinding;
import ua.napps.scorekeeper.data.CurrentSet;
import ua.napps.scorekeeper.settings.SettingsActivity;
import ua.napps.scorekeeper.utils.Constants;

import static ua.napps.scorekeeper.utils.Constants.ACTIVE_COUNTERS;

public class CountersActivity extends AppCompatActivity implements CounterActionCallback {

  private ActivityCountersBinding binding;
  private String[] colors;
  private CountersAdapter mProductAdapter;
  private CountersViewModel viewModel;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = DataBindingUtil.setContentView(this, R.layout.activity_counters);

    setSupportActionBar(binding.toolbar);
    colors = getResources().getStringArray(R.array.color_collection);

    mProductAdapter = new CountersAdapter(this);

    viewModel = ViewModelProviders.of(this).get(CountersViewModel.class);

    FlexboxLayoutManager layoutManager =
        new FlexboxLayoutManager(this, FlexDirection.COLUMN, FlexWrap.NOWRAP);
    binding.recyclerView.setLayoutManager(layoutManager);

    subscribeUi();
  }

  private void subscribeUi() {
    // Update the list when the data changes
    viewModel.getProducts().observe(this, new Observer<List<Counter>>() {
      @Override public void onChanged(@Nullable List<Counter> myProducts) {
        if (myProducts != null) {
          //mBinding.setIsLoading(false);
          mProductAdapter.setProductList(myProducts);
          if (myProducts.size() <= 4) {
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
          }
        } else {
          //mBinding.setIsLoading(true);
        }
      }
    });
  }

  private void loadSettings() {
    if (CurrentSet.getInstance().getSize() == 0) {
      ObservableArrayList<Counter> counters = Paper.book().read(ACTIVE_COUNTERS);
      if (counters != null && !counters.isEmpty()) {
        CurrentSet.getInstance().setCounters(counters);
      }
    }
    final boolean isKeepScreenOn =
        Paper.book(Constants.SETTINGS).read(Constants.SETTINGS_STAY_AWAKE, true);
    if (isKeepScreenOn) {
      getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    } else {
      getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
  }

  @Override protected void onStop() {
    super.onStop();
    saveSettings();
  }

  @Override protected void onStart() {
    super.onStart();
    loadSettings();
    invalidateOptionsMenu();
  }

  private void saveSettings() {
    Paper.book().write(ACTIVE_COUNTERS, CurrentSet.getInstance().getCounters());
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.counters_menu, menu);
    return true;
  }

  @Override public boolean onPrepareOptionsMenu(Menu menu) {
    MenuItem removeItem = menu.findItem(R.id.menu_remove_all);
    final boolean hasCounters = CurrentSet.getInstance().getSize() > 0;
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
        CurrentSet.getInstance().removeAllCounters();
        invalidateOptionsMenu();
        break;
      case R.id.menu_reset_all:
        CurrentSet.getInstance().resetAllCounters();
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

  public String getRandomColor() {
    //SecureRandom randomNum = new SecureRandom();
    final int presetSize = colors.length;
    final String hex = colors[new Random().nextInt(presetSize)];
    if (!CurrentSet.getInstance().getAlreadyUsedColors().contains(hex)) {
      return hex;
    } else {
      if (presetSize <= CurrentSet.getInstance().getSize()) {
        CurrentSet.getInstance().getAlreadyUsedColors().clear();
      }
      return getRandomColor();
    }
  }

  @Override public void onNameClick(int id) {

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

  @Override public void onIncreaseClick(int id) {

  }

  @Override public void onDecreaseClick(int id) {

  }

  @Override public void onAddCounterClick() {
    addCounter();
  }

  @Override public void scrollToPosition(int position) {
    binding.recyclerView.smoothScrollToPosition(position);
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
    saveSettings();
    binding.unbind();
    super.onDestroy();
  }
}