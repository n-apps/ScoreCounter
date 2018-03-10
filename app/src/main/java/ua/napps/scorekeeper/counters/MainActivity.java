package ua.napps.scorekeeper.counters;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.view.WindowManager;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import com.ashokvarma.bottomnavigation.TextBadgeItem;
import com.github.fernandodev.easyratingdialog.library.EasyRatingDialog;
import com.google.firebase.analytics.FirebaseAnalytics;

import ua.com.napps.scorekeeper.R;
import ua.napps.scorekeeper.app.App;
import ua.napps.scorekeeper.app.Constants;
import ua.napps.scorekeeper.dice.DicesFragment;
import ua.napps.scorekeeper.settings.SettingsFragment;
import ua.napps.scorekeeper.utils.AndroidFirebaseAnalytics;
import ua.napps.scorekeeper.utils.ViewUtil;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener, BottomNavigationBar.OnTabSelectedListener, DicesFragment.OnDiceFragmentInteractionListener {

    private static final String TAG_SETTINGS_FRAGMENT = "SETTINGS_FRAGMENT";
    private static final String TAG_COUNTERS_FRAGMENT = "COUNTERS_FRAGMENT";
    private static final String TAG_DICES_FRAGMENT = "DICES_FRAGMENT";
    private static final String STATE_CURRENT_DICE_ROLL = "STATE_CURRENT_DICE_ROLL";
    private static final String STATE_PREVIOUS_DICE_ROLL = "STATE_PREVIOUS_DICE_ROLL";
    private static final String[] TAGS = new String[]{TAG_COUNTERS_FRAGMENT, TAG_DICES_FRAGMENT, TAG_SETTINGS_FRAGMENT};

    private EasyRatingDialog easyRatingDialog;
    private Fragment currentFragment;
    private FragmentManager manager;
    private TextBadgeItem diceNumberBadgeItem;
    private int lastSelectedPosition;
    private int currentDiceRoll;
    private int previousDiceRoll;
    private boolean isThemeLight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            currentDiceRoll = savedInstanceState.getInt(STATE_CURRENT_DICE_ROLL);
            previousDiceRoll = savedInstanceState.getInt(STATE_PREVIOUS_DICE_ROLL);
        }
        isThemeLight = App.getTinyDB().getBoolean(Constants.SETTINGS_DICE_THEME_LIGHT, true);
        AppCompatDelegate.setDefaultNightMode(isThemeLight ? AppCompatDelegate.MODE_NIGHT_NO : AppCompatDelegate.MODE_NIGHT_YES);
        setContentView(R.layout.activity_main);
        easyRatingDialog = new EasyRatingDialog(this);
        manager = getSupportFragmentManager();
        App.getTinyDB().registerOnSharedPreferenceChangeListener(this);
        lastSelectedPosition = App.getTinyDB().getInt(Constants.LAST_SELECTED_BOTTOM_TAB);
        diceNumberBadgeItem = new TextBadgeItem().setHideOnSelect(true).hide(false).setBackgroundColorResource(R.color.accentColor);
        BottomNavigationBar bottomNavigationBar = findViewById(R.id.bottom_navigation_bar);
        bottomNavigationBar
                .addItem(new BottomNavigationItem(R.drawable.ic_plus_one_white, getString(R.string.bottom_navigation_tab_counters)))
                .addItem(new BottomNavigationItem(R.drawable.ic_dice, getString(R.string.bottom_navigation_tab_dice)).setBadgeItem(diceNumberBadgeItem))
                .addItem(new BottomNavigationItem(R.drawable.ic_settings, getString(R.string.bottom_navigation_tab_settings)))
                .setMode(BottomNavigationBar.MODE_FIXED_NO_TITLE)
                .setBarBackgroundColor(isThemeLight ? R.color.primaryColor : R.color.black)
                .setBackgroundStyle(BottomNavigationBar.BACKGROUND_STYLE_STATIC)
                .setActiveColor(R.color.white)
                .setFirstSelectedPosition(lastSelectedPosition)
                .initialise();
        bottomNavigationBar.setTabSelectedListener(this);

        switchFragment(TAGS[lastSelectedPosition]);
        ViewUtil.setLightStatusBar(this, isThemeLight && lastSelectedPosition > 0,
                ContextCompat.getColor(this, R.color.white), ContextCompat.getColor(this, R.color.dark_status_bar));
        applyKeepScreenOn(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_CURRENT_DICE_ROLL, currentDiceRoll);
        outState.putInt(STATE_PREVIOUS_DICE_ROLL, previousDiceRoll);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            updateCurrentRoll(savedInstanceState.getInt(STATE_CURRENT_DICE_ROLL));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        easyRatingDialog.showIfNeeded();
    }

    @Override
    protected void onStart() {
        super.onStart();
        easyRatingDialog.onStart();
    }

    @Override
    public void onTabSelected(int position) {
        switchFragment(TAGS[position]);
        lastSelectedPosition = position;
        App.getTinyDB().putInt(Constants.LAST_SELECTED_BOTTOM_TAB, lastSelectedPosition);
        if (currentDiceRoll > 0) {
            diceNumberBadgeItem.setText("" + currentDiceRoll);
        } else {
            diceNumberBadgeItem.hide(false);
        }
        ViewUtil.setLightStatusBar(this, isThemeLight && lastSelectedPosition > 0,
                ContextCompat.getColor(this, R.color.white), ContextCompat.getColor(this, R.color.dark_status_bar));
    }

    @Override
    public void onTabUnselected(int position) {

    }

    @Override
    public void onTabReselected(int position) {
        if (position == 0) {
            if (currentFragment instanceof CountersFragment) {
                ((CountersFragment) currentFragment).scrollToTop();
            }
        }
    }

    @Override
    public void updateCurrentRoll(int number) {
        previousDiceRoll = currentDiceRoll;
        currentDiceRoll = number;
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
        switch (key) {
            case Constants.SETTINGS_KEEP_SCREEN_ON:
                applyKeepScreenOn(false);
                break;
            case Constants.SETTINGS_DICE_THEME_LIGHT:
                if (sharedPreferences.getBoolean(Constants.SETTINGS_DICE_THEME_LIGHT, true)) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                }
                recreate();
                break;
        }
    }

    private void switchFragment(String tag) {
        switch (tag) {
            case TAG_COUNTERS_FRAGMENT:
                currentFragment = CountersFragment.newInstance();
                break;
            case TAG_DICES_FRAGMENT:
                currentFragment = DicesFragment.newInstance(currentDiceRoll, previousDiceRoll);
                break;
            case TAG_SETTINGS_FRAGMENT:
                currentFragment = SettingsFragment.newInstance();
                break;
        }
        manager.beginTransaction().replace(R.id.container, currentFragment, tag).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE).commitNow();
    }

    private void applyKeepScreenOn(boolean trackAnalytics) {
        final boolean isStayAwake = App.getTinyDB().getBoolean(Constants.SETTINGS_KEEP_SCREEN_ON, true);
        if (isStayAwake) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        if (trackAnalytics) {
            Bundle params = new Bundle();
            params.putLong(FirebaseAnalytics.Param.SCORE, isStayAwake ? 1 : 0);
            AndroidFirebaseAnalytics.logEvent("settings_keep_screen_on", params);
        }
    }

    @Override
    public void onBackPressed() {
        if (lastSelectedPosition == 2) {
            switchFragment(TAG_COUNTERS_FRAGMENT);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        App.getTinyDB().unregisterOnSharedPreferenceChangeListener(this);
    }
}

