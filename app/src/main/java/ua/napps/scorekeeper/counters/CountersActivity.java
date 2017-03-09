package ua.napps.scorekeeper.counters;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableArrayList;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import com.google.android.flexbox.FlexboxLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import ua.com.napps.scorekeeper.R;
import ua.com.napps.scorekeeper.databinding.ActivityCountersBinding;
import ua.napps.scorekeeper.data.CurrentSet;
import ua.napps.scorekeeper.dices.Dice;
import ua.napps.scorekeeper.favorites.FavoriteSet;
import ua.napps.scorekeeper.favorites.FavoriteSetsFragment;
import ua.napps.scorekeeper.favorites.FavoriteSetsFragment.FavSetLoadedListener;
import ua.napps.scorekeeper.settings.PrefUtil;
import ua.napps.scorekeeper.utils.ToastUtils;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
import static ua.napps.scorekeeper.dices.EditDiceFragment.DiceUpdateListener;
import static ua.napps.scorekeeper.settings.SettingsFragment.SettingsUpdatedListener;
import static ua.napps.scorekeeper.settings.SettingsFragment.newInstance;
import static ua.napps.scorekeeper.utils.Constants.ACTIVE_COUNTERS;
import static ua.napps.scorekeeper.utils.Constants.PREFS_DICE_AMOUNT;
import static ua.napps.scorekeeper.utils.Constants.PREFS_DICE_BONUS;
import static ua.napps.scorekeeper.utils.Constants.PREFS_DICE_MAX_EDGE;
import static ua.napps.scorekeeper.utils.Constants.PREFS_DICE_MIN_EDGE;
import static ua.napps.scorekeeper.utils.Constants.PREFS_DICE_SUM;
import static ua.napps.scorekeeper.utils.Constants.PREFS_SHOW_DICES;
import static ua.napps.scorekeeper.utils.Constants.PREFS_STAY_AWAKE;
import static ua.napps.scorekeeper.utils.Constants.SEND_REPORT_EMAIL;

public class CountersActivity extends AppCompatActivity
        implements FavSetLoadedListener, SettingsUpdatedListener, DiceUpdateListener,
        CounterActionCallback {

    private ActivityCountersBinding binding;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_counters);

        setSupportActionBar(binding.toolbar);

        binding.flexboxContainer.setFlexDirection(FlexboxLayout.FLEX_DIRECTION_COLUMN);

        loadSettings();

        //if (Build.VERSION.SDK_INT > 18) getWindow().addFlags(FLAG_FULLSCREEN);
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

        if (PrefUtil.getBoolean(this, PREFS_STAY_AWAKE, true)) {
            toggleKeepScreenOn(true);
        } else {
            toggleKeepScreenOn(false);
        }

        if (PrefUtil.getBoolean(this, PREFS_SHOW_DICES, false)) {
            toggleDicesBar(true);
            Dice.getDice().setDiceNumber(PrefUtil.getInt(this, PREFS_DICE_AMOUNT, 1));
            Dice.getDice().setMinSide(PrefUtil.getInt(this, PREFS_DICE_MIN_EDGE, 1));
            Dice.getDice().setMaxSide(PrefUtil.getInt(this, PREFS_DICE_MAX_EDGE, 6));
            Dice.getDice().setTotalBonus(PrefUtil.getInt(this, PREFS_DICE_BONUS, 0));

            setDiceSum(PrefUtil.getString(this, PREFS_DICE_SUM, "0"));
            setDiceFormula(Dice.getDice().toString());
        } else {
            toggleDicesBar(false);
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

        if (binding.dices.getVisibility() == VISIBLE) {
            PrefUtil.putInt(this, PREFS_DICE_AMOUNT, Dice.getDice().getDiceNumber());
            PrefUtil.putInt(this, PREFS_DICE_MIN_EDGE, Dice.getDice().getMinSide());
            PrefUtil.putInt(this, PREFS_DICE_MAX_EDGE, Dice.getDice().getMaxSide());
            PrefUtil.putInt(this, PREFS_DICE_BONUS, Dice.getDice().getTotalBonus());
        }
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        menu.clear();
        inflater.inflate(R.menu.toolbar_menu, menu);

        return true;
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_counter:
                addCounter();
                break;
            case R.id.menu_favorite_sets:
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_left, R.anim.slide_right,
                                R.anim.slide_left, R.anim.slide_right)
                        .replace(R.id.fragment_container, FavoriteSetsFragment.newInstance(),
                                "favorites")
                        .addToBackStack(null)
                        .commit();
                break;
            case R.id.menu_clear_all:
                CurrentSet.getInstance().removeAllCounters();
                addCounter();
                break;
            case R.id.menu_settings:
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_left, R.anim.slide_right,
                                R.anim.slide_left, R.anim.slide_right)
                        .replace(R.id.fragment_container, newInstance(), "settings")
                        .addToBackStack(null)
                        .commit();
                break;
            case R.id.menu_send_feedback:
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setType("text/plain");
                intent.setData(Uri.parse("mailto:" + SEND_REPORT_EMAIL));
                intent.putExtra(Intent.EXTRA_SUBJECT,
                        String.format("%s %s", getString(R.string.app_name),
                                getString(R.string.app_version_code)));
                startActivity(intent);
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

    @Override public void onBackPressed() {
        int count = getFragmentManager().getBackStackEntryCount();

        if (count == 0) {
            super.onBackPressed();
            assert getSupportActionBar() != null;
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setTitle(R.string.app_name);
        } else {
            getFragmentManager().popBackStack();
        }
    }

    private void toggleKeepScreenOn(boolean isSelected) {
        if (isSelected) {
            getWindow().addFlags(FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(FLAG_KEEP_SCREEN_ON);
        }
    }

    private void toggleDicesBar(boolean isShowing) {
        if (isShowing) {
            binding.dices.setVisibility(VISIBLE);
        } else {
            binding.dices.setVisibility(GONE);
        }
    }

    private void setDiceSum(String sum) {
        binding.diceSum.setText(sum);
    }

    private void setDiceFormula(String formula) {
        binding.diceFormula.setText(formula);
    }

    @Override public void onFavSetLoaded(FavoriteSet set) {
        ObservableArrayList<Counter> list = new ObservableArrayList<>();
        list.addAll(set.getCounters());
        CurrentSet.getInstance().setCounters(list);
        ToastUtils.getInstance()
                .showToast(this, String.format("%s loaded", set.getName()), Toast.LENGTH_SHORT);
    }

    @Override public void onSettingsUpdated() {
        loadSettings();
    }

    @Override public void onDiceUpdate() {
        setDiceFormula(Dice.getDice().toString());
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