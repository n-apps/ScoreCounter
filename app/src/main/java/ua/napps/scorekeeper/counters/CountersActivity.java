package ua.napps.scorekeeper.counters;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableArrayList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import com.afollestad.materialdialogs.MaterialDialog;
import io.paperdb.Paper;
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

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = DataBindingUtil.setContentView(this, R.layout.activity_counters);

    setSupportActionBar(binding.toolbar);
    colors = getResources().getStringArray(R.array.color_collection);
    loadSettings();
    binding.setCallback(this);
    binding.setItems(CurrentSet.getInstance().getCounters());
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

  private void saveSettings() {
    Paper.book().write(ACTIVE_COUNTERS, CurrentSet.getInstance().getCounters());
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.counters_menu, menu);
    return true;
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.menu_add_counter:
        addCounter();
        break;
      case R.id.menu_clear_all:
        CurrentSet.getInstance().removeAllCounters();
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
    final int size = CurrentSet.getInstance().getSize();
    final String caption = getString(R.string.counter_default_title, size + 1);
    CurrentSet.getInstance().addCounter(caption, getRandomColor());
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

  @Override public void onNameClick(String id) {
    Intent intent = EditCounterActivity.getIntent(this, id);
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    startActivity(intent);
  }

  @Override public boolean onNameLongClick(View v, Counter counter) {
    assert counter != null;
    new MaterialDialog.Builder(this).content("Counter name")
        .inputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
            | InputType.TYPE_CLASS_TEXT
            | InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE)
        .input("Hint", counter.getName(), (dialog, input) -> counter.setName(input.toString()))
        .widgetColor(Color.parseColor(counter.getColor()))
        .positiveText("Set")
        .show();
    return true;
  }

  @Override public boolean onValueLongClick(View v, Counter counter) {
    assert counter != null;
    new MaterialDialog.Builder(this).content("Counter value")
        .inputType(InputType.TYPE_CLASS_NUMBER)
        .input("Hint", String.valueOf(counter.getValue()),
            (dialog, input) -> counter.setValue(Integer.parseInt(String.valueOf(input))))
        .widgetColor(Color.parseColor(counter.getColor()))
        .positiveText("Set")
        .show();
    return true;
  }

  @Override public void onIncreaseClick(String id) {
    final Counter counter = CurrentSet.getInstance().getCounter(id);
    assert counter != null;
    final int newValue = counter.getValue() + counter.getStep();
    counter.setValue(newValue);
  }

  @Override public void onDecreaseClick(String id) {
    final Counter counter = CurrentSet.getInstance().getCounter(id);
    assert counter != null;
    final int newValue = counter.getValue() - counter.getStep();
    counter.setValue(newValue);
  }

  @Override public void onAddCounterClick() {
    addCounter();
  }

  @Override public void scrollToPosition(int position) {
    binding.recyclerView.smoothScrollToPosition(position);
  }

  @Override protected void onDestroy() {
    saveSettings();
    binding.unbind();
    super.onDestroy();
  }
}