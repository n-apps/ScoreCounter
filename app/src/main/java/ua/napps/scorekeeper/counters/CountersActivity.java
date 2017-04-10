package ua.napps.scorekeeper.counters;

import android.content.Intent;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import com.google.android.flexbox.FlexDirection;
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

        //final boolean isTablet = getResources().getBoolean(R.bool.isTablet);

        binding.flexboxContainer.setFlexDirection(
                getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE
                        ? FlexDirection.ROW : FlexDirection.COLUMN);

        loadSettings();
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
        binding.setCounters(CurrentSet.getInstance().getCounters());
        binding.setCallback(this);
        binding.executePendingBindings();
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

    @Override public void onLongClick(String id) {
        Intent intent = EditCounterActivity.getIntent(this, id);
        startActivity(intent);
    }
}