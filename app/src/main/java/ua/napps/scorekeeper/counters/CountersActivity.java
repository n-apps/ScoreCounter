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
import com.afollestad.materialdialogs.MaterialDialog;
import io.paperdb.Paper;
import ua.com.napps.scorekeeper.R;
import ua.com.napps.scorekeeper.databinding.ActivityCountersBinding;
import ua.napps.scorekeeper.data.CurrentSet;
import ua.napps.scorekeeper.favorites.FavoriteSet;
import ua.napps.scorekeeper.utils.Constants;

import static ua.napps.scorekeeper.utils.Constants.ACTIVE_COUNTERS;

public class CountersActivity extends AppCompatActivity implements CounterActionCallback {

  private ActivityCountersBinding binding;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = DataBindingUtil.setContentView(this, R.layout.activity_counters);

    setSupportActionBar(binding.toolbar);

    loadSettings();
    binding.setItems(CurrentSet.getInstance().getCounters());
    binding.setCallback(this);
    binding.executePendingBindings();
  }

  private void loadSettings() {
    ObservableArrayList<Counter> counters = Paper.book().read(ACTIVE_COUNTERS);
    if (counters != null && !counters.isEmpty()) {
      CurrentSet.getInstance().setCounters(counters);
    } else {
      addCounter();
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
      case R.id.menu_save_to_favorites:
        saveCurrentSetToFavorites();
        break;
      case R.id.menu_clear_all:
        CurrentSet.getInstance().removeAllCounters();
        addCounter();
        break;
      case R.id.menu_reset_all:
        CurrentSet.getInstance().resetAllCounters();
        break;
      default:
        break;
    }
    return true;
  }

  private void saveCurrentSetToFavorites() {
    new MaterialDialog.Builder(this).content("Counter name")
        .inputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
            | InputType.TYPE_CLASS_TEXT
            | InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE)
        .input("Set name", null, false, (materialDialog, charSequence) -> {
        })
        .onPositive((materialDialog, dialogAction) -> {
          final String setName = materialDialog.getInputEditText().getText().toString();
          FavoriteSet set = new FavoriteSet(setName, CurrentSet.getInstance().getCounters());
          Paper.book(Constants.FAVORITES_COUNTER_SETS).write(setName, set);
        })
        .positiveText("Save to favorites")
        .show();
  }

  private void addCounter() {
    final String caption =
        getString(R.string.counter_default_title, CurrentSet.getInstance().getSize() + 1);
    CurrentSet.getInstance().addCounter(caption);
  }

  @Override public void onNameClick(String id) {
    Intent intent = EditCounterActivity.getIntent(this, id);
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

  @Override public void onCounterAdded(View v) {
    binding.scrollView.postDelayed(() -> binding.scrollView.smoothScrollTo(0, v.getBottom()), 300L);
  }

  @Override protected void onDestroy() {
    saveSettings();
    binding.unbind();
    super.onDestroy();
  }
}