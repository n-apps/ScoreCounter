package ua.napps.scorekeeper.app;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.transition.MaterialFade;

import ua.napps.scorekeeper.R;
import ua.napps.scorekeeper.counters.CountersFragment;
import ua.napps.scorekeeper.dice.DicesFragment;
import ua.napps.scorekeeper.dice.OnDiceFragmentInteractionListener;
import ua.napps.scorekeeper.settings.LocalSettings;
import ua.napps.scorekeeper.settings.SettingsFragment;
import ua.napps.scorekeeper.utils.RateMyAppDialog;
import ua.napps.scorekeeper.utils.Singleton;
import ua.napps.scorekeeper.utils.Utilities;
import ua.napps.scorekeeper.utils.ViewUtil;


public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener, OnDiceFragmentInteractionListener {

    private static final String TAG_DICES_FRAGMENT = "DICES_FRAGMENT";
    private static final String TAG_COUNTERS_FRAGMENT = "COUNTERS_FRAGMENT";
    private static final String TAG_SETTINGS_FRAGMENT = "SETTINGS_FRAGMENT";
    private static final String STATE_CURRENT_DICE_ROLL = "STATE_CURRENT_DICE_ROLL";
    private static final String STATE_PREVIOUS_DICE_ROLL = "STATE_PREVIOUS_DICE_ROLL";
    private static final String[] TAGS = new String[]{TAG_COUNTERS_FRAGMENT, TAG_DICES_FRAGMENT, TAG_SETTINGS_FRAGMENT};

    private RateMyAppDialog rateMyAppDialog;
    private Fragment currentFragment;
    private FragmentManager manager;
    private int lastSelectedBottomTab;
    private int currentDiceRoll;
    private int previousDiceRoll;
    private boolean isLightTheme;
    private boolean isKeepScreenOn;
    private BottomNavigationView bottomNavigationBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isLightTheme = LocalSettings.isLightTheme();
        isKeepScreenOn = LocalSettings.isKeepScreenOnEnabled();
        if (savedInstanceState != null) {
            currentDiceRoll = savedInstanceState.getInt(STATE_CURRENT_DICE_ROLL);
            previousDiceRoll = savedInstanceState.getInt(STATE_PREVIOUS_DICE_ROLL);
        }

        // If android Q override night mode settings from system default
        if (Utilities.hasQ()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

            int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

            LocalSettings.saveDarkTheme((currentNightMode == Configuration.UI_MODE_NIGHT_YES));
            isLightTheme = LocalSettings.isLightTheme();
        } else {
            AppCompatDelegate.setDefaultNightMode(isLightTheme ? AppCompatDelegate.MODE_NIGHT_NO : AppCompatDelegate.MODE_NIGHT_YES);
        }

        setContentView(R.layout.activity_main);
        rateMyAppDialog = new RateMyAppDialog(this);
        manager = getSupportFragmentManager();
        lastSelectedBottomTab = LocalSettings.getLastSelectedBottomTab();
        if (lastSelectedBottomTab > 1) {
            lastSelectedBottomTab = 0;
        }
        bottomNavigationBar = findViewById(R.id.bottom_navigation);

        bottomNavigationBar.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.counters:
                    switchFragment(TAGS[0]);
                    lastSelectedBottomTab = 0;
                    break;
                case R.id.dices:
                    switchFragment(TAGS[1]);
                    lastSelectedBottomTab = 1;
                    break;
                case R.id.more:
                    switchFragment(TAGS[2]);
                    lastSelectedBottomTab = 2;
                    break;
            }
            LocalSettings.saveLastSelectedBottomTab(lastSelectedBottomTab);

            // TODO: 06-May-20 add badge
//                if (currentDiceRoll > 0) {
//                    diceNumberBadgeItem.setText("" + currentDiceRoll);
//                } else {
//                    diceNumberBadgeItem.hide(false);
//                }
//                if (isLightTheme) {
//                    ViewUtil.setLightStatusBar(this);
//                } else {
//                    ViewUtil.clearLightStatusBar(this);
//                }
//                ViewUtil.setNavBarColor(this, isLightTheme);
            return true;
        });

        bottomNavigationBar.setOnNavigationItemReselectedListener(item -> {
            if (item.getItemId() == R.id.counters) {
                if (currentFragment instanceof CountersFragment) {
                    ((CountersFragment) currentFragment).scrollToTop();
                }
            }
        });

        switchFragment(TAGS[lastSelectedBottomTab]);
        // not selected after app restarted
        switch (lastSelectedBottomTab) {
            case 0:
                bottomNavigationBar.setSelectedItemId(R.id.counters);
                break;
            case 1:
                bottomNavigationBar.setSelectedItemId(R.id.dice);
                break;
            case 2:
                bottomNavigationBar.setSelectedItemId(R.id.more);
                break;
        }
        if (isLightTheme) {
            ViewUtil.setLightStatusBar(this);
        } else {
            ViewUtil.clearLightStatusBar(this);
        }
        ViewUtil.setNavBarColor(this, isLightTheme);
        applyKeepScreenOn();

        Singleton.getInstance().setMainContext(this);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_CURRENT_DICE_ROLL, currentDiceRoll);
        outState.putInt(STATE_PREVIOUS_DICE_ROLL, previousDiceRoll);
    }

    @Override
    protected void onResume() {
        super.onResume();
        rateMyAppDialog.showIfNeeded();
    }

    @Override
    protected void onStart() {
        super.onStart();
        rateMyAppDialog.onStart();
        App.getTinyDB().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        App.getTinyDB().unregisterOnSharedPreferenceChangeListener(this);
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
        MaterialFade fadeThrough = MaterialFade.create();
        currentFragment.setEnterTransition(fadeThrough);
        manager.beginTransaction()
                .replace(R.id.container, currentFragment, tag)
                .commit();
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
            bottomNavigationBar.setSelectedItemId(R.id.counters);
        }
    }

}