package ua.napps.scorekeeper.app;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import com.ashokvarma.bottomnavigation.TextBadgeItem;
import com.github.fernandodev.easyratingdialog.library.EasyRatingDialog;

import ua.com.napps.scorekeeper.R;
import ua.napps.scorekeeper.counters.CountersFragment;
import ua.napps.scorekeeper.dice.DicesFragment;
import ua.napps.scorekeeper.dice.OnDiceFragmentInteractionListener;
import ua.napps.scorekeeper.settings.LocalSettings;
import ua.napps.scorekeeper.settings.SettingsFragment;
import ua.napps.scorekeeper.utils.AndroidFirebaseAnalytics;
import ua.napps.scorekeeper.utils.Singleton;
import ua.napps.scorekeeper.utils.ViewUtil;


public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener, BottomNavigationBar.OnTabSelectedListener, OnDiceFragmentInteractionListener {

    private static final String TAG_DICES_FRAGMENT = "DICES_FRAGMENT";
    private static final String TAG_COUNTERS_FRAGMENT = "COUNTERS_FRAGMENT";
    private static final String TAG_SETTINGS_FRAGMENT = "SETTINGS_FRAGMENT";
    private static final String STATE_CURRENT_DICE_ROLL = "STATE_CURRENT_DICE_ROLL";
    private static final String STATE_PREVIOUS_DICE_ROLL = "STATE_PREVIOUS_DICE_ROLL";
    private static final String[] TAGS = new String[]{TAG_COUNTERS_FRAGMENT, TAG_DICES_FRAGMENT, TAG_SETTINGS_FRAGMENT};

    private EasyRatingDialog easyRatingDialog;
    private Fragment currentFragment;
    private FragmentManager manager;
    private TextBadgeItem diceNumberBadgeItem;
    private int lastSelectedBottomTab;
    private int currentDiceRoll;
    private int previousDiceRoll;
    private boolean isLightTheme;
    private boolean isKeepScreenOn;
    private BottomNavigationBar bottomNavigationBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isLightTheme = LocalSettings.isLightTheme();
        isKeepScreenOn = LocalSettings.isKeepScreenOnEnabled();
        if (savedInstanceState == null) {
            trackAnalytics();
        } else {
            currentDiceRoll = savedInstanceState.getInt(STATE_CURRENT_DICE_ROLL);
            previousDiceRoll = savedInstanceState.getInt(STATE_PREVIOUS_DICE_ROLL);
        }
        AppCompatDelegate.setDefaultNightMode(isLightTheme ? AppCompatDelegate.MODE_NIGHT_NO : AppCompatDelegate.MODE_NIGHT_YES);
        setContentView(R.layout.activity_main);
        easyRatingDialog = new EasyRatingDialog(this);
        manager = getSupportFragmentManager();
        lastSelectedBottomTab = LocalSettings.getLastSelectedBottomTab();
        if (lastSelectedBottomTab > 1) {
            lastSelectedBottomTab = 0;
        }
        diceNumberBadgeItem = new TextBadgeItem().setHideOnSelect(true).hide(false).setBackgroundColorResource(R.color.accentColor);
        bottomNavigationBar = findViewById(R.id.bottom_navigation_bar);
        bottomNavigationBar
                .addItem(new BottomNavigationItem(R.drawable.ic_grid, getString(R.string.bottom_navigation_tab_counters)))
                .addItem(new BottomNavigationItem(R.drawable.ic_dice, getString(R.string.bottom_navigation_tab_dice)).setBadgeItem(diceNumberBadgeItem))
                .addItem(new BottomNavigationItem(R.drawable.ic_settings, getString(R.string.bottom_navigation_tab_settings)))
                .setMode(BottomNavigationBar.MODE_FIXED_NO_TITLE)
                .setBarBackgroundColor(isLightTheme ? R.color.white : R.color.black)
                .setBackgroundStyle(BottomNavigationBar.BACKGROUND_STYLE_STATIC)
                .setActiveColor(isLightTheme ? R.color.accentColor : R.color.white)
                .setFirstSelectedPosition(lastSelectedBottomTab)
                .initialise();
        bottomNavigationBar.setTabSelectedListener(this);

        switchFragment(TAGS[lastSelectedBottomTab]);
        if (isLightTheme) {
            ViewUtil.setLightStatusBar(this, Color.WHITE);
        } else {
            ViewUtil.clearLightStatusBar(this, Color.BLACK);
        }
        ViewUtil.setNavBarColor(this, isLightTheme);
        applyKeepScreenOn();

        Singleton.getInstance().setMainContext(this);

    }

    private void trackAnalytics() {
        Bundle params1 = new Bundle();
        params1.putLong(com.google.firebase.analytics.FirebaseAnalytics.Param.SCORE, isLightTheme ? 0 : 1);
        AndroidFirebaseAnalytics.logEvent("dark_theme", params1);

        Bundle params2 = new Bundle();
        params2.putLong(com.google.firebase.analytics.FirebaseAnalytics.Param.SCORE, isKeepScreenOn ? 1 : 0);
        AndroidFirebaseAnalytics.logEvent("keep_screen_on", params2);

        Bundle params3 = new Bundle();
        params3.putLong(com.google.firebase.analytics.FirebaseAnalytics.Param.SCORE, LocalSettings.isShakeToRollEnabled() ? 1 : 0);
        AndroidFirebaseAnalytics.logEvent("shake_to_roll", params3);

        Bundle params4 = new Bundle();
        params4.putString(com.google.firebase.analytics.FirebaseAnalytics.Param.CHARACTER, "" + LocalSettings.getDiceMaxSide());
        AndroidFirebaseAnalytics.logEvent("dice_max_side", params4);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_CURRENT_DICE_ROLL, currentDiceRoll);
        outState.putInt(STATE_PREVIOUS_DICE_ROLL, previousDiceRoll);
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
        App.getTinyDB().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        App.getTinyDB().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onTabSelected(int position) {
        switchFragment(TAGS[position]);
        lastSelectedBottomTab = position;
        LocalSettings.saveLastSelectedBottomTab(lastSelectedBottomTab);
        if (currentDiceRoll > 0) {
            diceNumberBadgeItem.setText("" + currentDiceRoll);
        } else {
            diceNumberBadgeItem.hide(false);
        }
        if (isLightTheme) {
            ViewUtil.setLightStatusBar(this);
        } else {
            ViewUtil.clearLightStatusBar(this, Color.BLACK);
        }
        ViewUtil.setNavBarColor(this, isLightTheme);
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
            case LocalSettings.KEEP_SCREEN_ON:
                isKeepScreenOn = LocalSettings.isKeepScreenOnEnabled();
                applyKeepScreenOn();
                break;
            case LocalSettings.DARK_THEME:
                if (LocalSettings.isLightTheme()) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
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
        manager.beginTransaction().replace(R.id.container, currentFragment, tag).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE).commitAllowingStateLoss();
    }

    private void applyKeepScreenOn() {
        if (isKeepScreenOn) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    public void onBackPressed() {
        if (lastSelectedBottomTab != 2) {
            super.onBackPressed();
        } else {
            bottomNavigationBar.selectTab(0);
        }
    }

}