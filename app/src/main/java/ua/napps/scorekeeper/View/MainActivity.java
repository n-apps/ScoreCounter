package ua.napps.scorekeeper.View;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnLongClick;
import com.google.android.flexbox.FlexboxLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import ua.com.napps.scorekeeper.R;
import ua.napps.scorekeeper.Models.Counter;
import ua.napps.scorekeeper.Models.CurrentSet;
import ua.napps.scorekeeper.Models.Dice;
import ua.napps.scorekeeper.Models.FavoriteSet;
import ua.napps.scorekeeper.Utils.PrefUtil;
import ua.napps.scorekeeper.Utils.ToastUtils;
import ua.napps.scorekeeper.Utils.Util;
import ua.napps.scorekeeper.View.FavoriteSetsFragment.FavSetLoadedListener;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
import static ua.napps.scorekeeper.Helpers.Constants.ACTIVE_COUNTERS;
import static ua.napps.scorekeeper.Helpers.Constants.PREFS_DICE_AMOUNT;
import static ua.napps.scorekeeper.Helpers.Constants.PREFS_DICE_BONUS;
import static ua.napps.scorekeeper.Helpers.Constants.PREFS_DICE_MAX_EDGE;
import static ua.napps.scorekeeper.Helpers.Constants.PREFS_DICE_MIN_EDGE;
import static ua.napps.scorekeeper.Helpers.Constants.PREFS_DICE_SUM;
import static ua.napps.scorekeeper.Helpers.Constants.PREFS_SHOW_DICES;
import static ua.napps.scorekeeper.Helpers.Constants.PREFS_STAY_AWAKE;
import static ua.napps.scorekeeper.Helpers.Constants.SEND_REPORT_EMAIL;
import static ua.napps.scorekeeper.View.EditCounterFragment.CounterUpdateListener;
import static ua.napps.scorekeeper.View.EditDiceFragment.DiceUpdateListener;
import static ua.napps.scorekeeper.View.SettingFragment.SettingsUpdatedListener;
import static ua.napps.scorekeeper.View.SettingFragment.newInstance;

public class MainActivity extends AppCompatActivity
        implements FavSetLoadedListener, SettingsUpdatedListener, DiceUpdateListener,
        CounterUpdateListener, View.OnClickListener {

    private static final int DEFAULT_WIDTH = 120;

    private static final int DEFAULT_HEIGHT = 80;

    @Bind(R.id.flexbox_container) FlexboxLayout flexboxLayout;
    @Bind(R.id.dices) LinearLayout dicesBar;
    @Bind(R.id.dice_formula) TextView mDiceFormula;
    @Bind(R.id.dice_sum) TextView mDiceSum;
    @Bind(R.id.toolbar) Toolbar mToolbar;

    @SuppressWarnings("SameReturnValue") @OnLongClick(R.id.dice_formula)
    public boolean onLongClick() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        EditDiceFragment diceDialog = EditDiceFragment.newInstance();
        diceDialog.show(fragmentManager, "dice_dialog");
        return true;
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        flexboxLayout.setFlexDirection(FlexboxLayout.FLEX_DIRECTION_COLUMN);

        //if (Build.VERSION.SDK_INT > 18) getWindow().addFlags(FLAG_FULLSCREEN);
    }

    @Override protected void onResume() {
        super.onResume();

        loadSettings();
    }

    private void loadSettings() {
        if (CurrentSet.getInstance().getSize() == 0) {
            String activeCountersJson = PrefUtil.getString(this, ACTIVE_COUNTERS, "");
            Type listType = new TypeToken<ArrayList<Counter>>() {
            }.getType();
            ArrayList<Counter> counters = new Gson().fromJson(activeCountersJson, listType);
            if (counters == null) {
                counters = new ArrayList<>();
                counters.add(new Counter(getResources().getString(R.string.counter_default_title)));
            }
            CurrentSet.getInstance().setCounters(counters);
            for (Counter counter : counters) {
                addCounterToFlexbox(counter);
            }
        }

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

    private void addCounterToFlexbox(Counter counter) {
        int viewIndex = flexboxLayout.getChildCount();
        // index starts from 0. New View's index is N if N views ([0, 1, 2, ... N-1])
        // exist.
        TextView textView = createBaseFlexItemTextView(viewIndex);
        textView.setText(counter.getCaption());
        textView.setTag(counter);
        textView.setOnClickListener(this);
        textView.setLayoutParams(createDefaultLayoutParams());
        flexboxLayout.addView(textView);
    }


    private FlexboxLayout.LayoutParams createDefaultLayoutParams() {
        FlexboxLayout.LayoutParams lp =
                new FlexboxLayout.LayoutParams(Util.dpToPixel(this, DEFAULT_WIDTH),
                        Util.dpToPixel(this, DEFAULT_HEIGHT));
        lp.order = -1;
        lp.flexGrow = 2;
        return lp;
    }

    private TextView createBaseFlexItemTextView(int index) {
        TextView textView = new TextView(this);
        textView.setBackgroundResource(R.drawable.flex_item_background);
        textView.setText(String.valueOf(index + 1));
        textView.setGravity(Gravity.CENTER);
        return textView;
    }

    @Override protected void onStop() {
        super.onStop();
        saveSettings();
    }

    @Override protected void onDestroy() {
        saveSettings();
        super.onDestroy();
    }

    private void saveSettings() {
        String activeCountersJson = new Gson().toJson(CurrentSet.getInstance().getCounters());
        PrefUtil.putString(this, ACTIVE_COUNTERS, activeCountersJson);

        if (dicesBar.getVisibility() == VISIBLE) {
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
                flexboxLayout.removeAllViews();
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
        final Counter counter = newCounter();
        CurrentSet.getInstance().addCounter(counter);
        addCounterToFlexbox(counter);
    }

    @NonNull private Counter newCounter() {
        return new Counter(String.format("%s %d", getString(R.string.counter_default_title),
                CurrentSet.getInstance().getSize() + 1));
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
            dicesBar.setVisibility(VISIBLE);
        } else {
            dicesBar.setVisibility(GONE);
        }
    }

    private void setDiceSum(String sum) {
        mDiceSum.setText(sum);
    }

    private void setDiceFormula(String formula) {
        mDiceFormula.setText(formula);
    }

    @Override public void onFavSetLoaded(FavoriteSet set) {
        CurrentSet.getInstance().setCounters(set.getCounters());
        ToastUtils.getInstance()
                .showToast(this, String.format("%s loaded", set.getName()), Toast.LENGTH_SHORT);
    }

    @Override public void onSettingsUpdated() {
        loadSettings();
    }

    @Override public void onDiceUpdate() {
        setDiceFormula(Dice.getDice().toString());
    }

    @Override public void onCounterUpdate() {

    }

    @Override public void onCounterDelete() {

    }

    @Override public void onClick(View v) {
        CurrentSet.getInstance().removeCounter(v.getTag());
        flexboxLayout.removeView(v);
    }
}