package ua.napps.scorekeeper.counters;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import ua.com.napps.scorekeeper.R;
import ua.com.napps.scorekeeper.databinding.ActivityCountersBinding;
import ua.napps.scorekeeper.data.CurrentSet;
import ua.napps.scorekeeper.settings.PrefUtil;

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
        if (CurrentSet.getInstance().getSize() == 0) {
            String activeCountersJson = PrefUtil.getString(this, ACTIVE_COUNTERS, "");
            Type listType = new TypeToken<ArrayList<Counter>>() {
            }.getType();
            ArrayList<Counter> counters = new Gson().fromJson(activeCountersJson, listType);
            if (counters == null) {
                addCounter();
            } else {
                CurrentSet.getInstance().setCounters(counters);
            }
        }
    }

    @Override protected void onStop() {
        super.onStop();
        saveSettings();
    }

    @Override protected void onDestroy() {
        saveSettings();
        binding.unbind();
        super.onDestroy();
    }

    private void saveSettings() {
        String activeCountersJson = new Gson().toJson(CurrentSet.getInstance().getCounters());
        PrefUtil.putString(this, ACTIVE_COUNTERS, activeCountersJson);
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

    private void addCounter() {
        final String caption =
                getString(R.string.counter_default_title, CurrentSet.getInstance().getSize() + 1);
        CurrentSet.getInstance().addCounter(caption);
    }

    @Override public void onNameClick(String id) {
        Intent intent = EditCounterActivity.getIntent(this, id);
        startActivity(intent);
    }

    @Override public boolean onLongClick(View v, Counter counter) {
        assert counter != null;
        new MaterialDialog.Builder(this).content("Counter value")
                .inputType(InputType.TYPE_CLASS_NUMBER)
                .input("Hint", String.valueOf(counter.getValue()),
                        (dialog, input) -> counter.setValue(
                                Integer.parseInt(String.valueOf(input))))
                .widgetColor(counter.getBackgroundColor())
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
        binding.scrollView.postDelayed(() -> binding.scrollView.smoothScrollTo(0, v.getBottom()),
                300L);
    }
}