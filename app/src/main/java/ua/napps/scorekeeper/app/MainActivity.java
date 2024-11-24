package ua.napps.scorekeeper.app;

import static ua.napps.scorekeeper.settings.LocalSettings.THEME_DARK;
import static ua.napps.scorekeeper.settings.LocalSettings.THEME_LIGHT;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.transition.platform.MaterialFadeThrough;

import ua.napps.scorekeeper.R;
import ua.napps.scorekeeper.counters.CountersFragment;
import ua.napps.scorekeeper.dice.DicesFragment;
import ua.napps.scorekeeper.dice.OnDiceFragmentInteractionListener;
import ua.napps.scorekeeper.settings.LocalSettings;
import ua.napps.scorekeeper.settings.SettingsFragment;
import ua.napps.scorekeeper.utils.RateMyAppDialog;
import ua.napps.scorekeeper.utils.ViewUtil;


public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener, OnDiceFragmentInteractionListener {

    private enum NavigationTag {
        COUNTERS(TAG_COUNTERS_FRAGMENT),
        DICES(TAG_DICES_FRAGMENT),
        SETTINGS(TAG_SETTINGS_FRAGMENT);

        private final String tag;

        NavigationTag(String tag) {
            this.tag = tag;
        }

        public String getTag() {
            return tag;
        }
    }

    private static final String TAG_DICES_FRAGMENT = "DICES_FRAGMENT";
    private static final String TAG_COUNTERS_FRAGMENT = "COUNTERS_FRAGMENT";
    private static final String TAG_SETTINGS_FRAGMENT = "SETTINGS_FRAGMENT";
    private static final String STATE_CURRENT_DICE_ROLL = "STATE_CURRENT_DICE_ROLL";

    private String currentFragmentTag;
    private FragmentManager manager;
    private int currentDiceRoll;
    private boolean isKeepScreenOn;
    private BottomNavigationView bottomNavigationBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isKeepScreenOn = LocalSettings.isKeepScreenOnEnabled();
        if (savedInstanceState != null) {
            currentDiceRoll = savedInstanceState.getInt(STATE_CURRENT_DICE_ROLL);
        }

        applyAppTheme();
        boolean nightModeActive = ViewUtil.isNightModeActive(this);
        ViewUtil.setLightMode(this, !nightModeActive);
        ViewUtil.setNavBarColor(this, !nightModeActive);
        setContentView(R.layout.activity_main);


        manager = getSupportFragmentManager();

        bottomNavigationBar = findViewById(R.id.bottom_navigation);
        bottomNavigationBar.setSelectedItemId(R.id.counters);

        bottomNavigationBar.setOnItemSelectedListener(item -> {
            updateUIForSelectedItem(item.getItemId(), nightModeActive);
            switchFragment(getTagForItemId(item.getItemId()));
            return true;
        });

        bottomNavigationBar.setOnItemReselectedListener(item -> {
            if (item.getItemId() == R.id.counters) {
                Fragment f = getSupportFragmentManager().findFragmentByTag(currentFragmentTag);
                if (f instanceof CountersFragment) {
                    ((CountersFragment) f).scrollToTop();
                }
            }
        });

        switchFragment(NavigationTag.COUNTERS);

        applyKeepScreenOnIfNeeded();

        RateMyAppDialog rateMyAppDialog = new RateMyAppDialog(this);
        rateMyAppDialog.showIfNeeded();


        //Handle the case when back button is pressed. Show warning message
        OnBackPressedDispatcher dispatcher = getOnBackPressedDispatcher();
        dispatcher.addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (bottomNavigationBar.getSelectedItemId() == R.id.counters) {
                    finish();
                } else {
                    bottomNavigationBar.setSelectedItemId(R.id.counters);
                }
            }
        });
    }

    private void applyAppTheme() {
        int theme = LocalSettings.getDefaultTheme();
        final int nightMode;
        switch (theme) {
            case THEME_DARK:
                nightMode = AppCompatDelegate.MODE_NIGHT_YES;
                break;
            case THEME_LIGHT:
                nightMode = AppCompatDelegate.MODE_NIGHT_NO;
                break;
            default:
                nightMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                break;
        }
        AppCompatDelegate.setDefaultNightMode(nightMode);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_CURRENT_DICE_ROLL, currentDiceRoll);
    }
    @Override
    protected void onStart() {
        super.onStart();
        App.getTinyDB().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        App.getTinyDB().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void updateCurrentRoll(int diceRoll) {
        currentDiceRoll = diceRoll;
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
        if (LocalSettings.KEEP_SCREEN_ON.equals(key)) {
            isKeepScreenOn = LocalSettings.isKeepScreenOnEnabled();
            applyKeepScreenOnIfNeeded();
        } else if (LocalSettings.APP_THEME_MODE.equals(key)) {
            bottomNavigationBar.setSelectedItemId(R.id.counters);
            applyAppTheme();
            getDelegate().applyDayNight();
        } else if (LocalSettings.isRelevantToCounters(key)) { // Check if the key affects CountersFragment
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_COUNTERS_FRAGMENT);
            if (fragment instanceof CountersFragment) {
                ((CountersFragment) fragment).onSharedPreferencesUpdated(key);
            }
        }
    }


    private void switchFragment(NavigationTag navigationTag) {
        String tag = navigationTag.getTag();
        if (tag.equals(currentFragmentTag)) return;

        FragmentTransaction transaction = manager.beginTransaction();
        Fragment currentFragment = manager.findFragmentByTag(currentFragmentTag);
        Fragment newFragment = getFragmentByTag(tag);
        newFragment.setEnterTransition(createTransition());

        if (currentFragment != null) transaction.hide(currentFragment);
        if (newFragment.isAdded()) {
            transaction.show(newFragment);
        } else {
            transaction.add(R.id.container, newFragment, tag);
        }

        transaction.commit();
        currentFragmentTag = tag;
    }

    private Fragment getFragmentByTag(String tag) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
        if (fragment != null) return fragment;

        switch (tag) {
            case TAG_COUNTERS_FRAGMENT:
                return CountersFragment.newInstance();
            case TAG_DICES_FRAGMENT:
                return DicesFragment.newInstance(currentDiceRoll);
            case TAG_SETTINGS_FRAGMENT:
                return SettingsFragment.newInstance();
        }
        return null;
    }

    private void applyKeepScreenOnIfNeeded() {
        boolean shouldKeepScreenOn = LocalSettings.isKeepScreenOnEnabled();
        if (isKeepScreenOn == shouldKeepScreenOn) return;

        isKeepScreenOn = shouldKeepScreenOn;
        if (isKeepScreenOn) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    private void updateUIForSelectedItem(int itemId, boolean isNightMode) {
        int statusBarColor;
        boolean lightMode;

        if (itemId == R.id.settings) {
            statusBarColor = ContextCompat.getColor(this, R.color.colorPrimaryVariant);
            lightMode = false;
        } else {
            statusBarColor = ContextCompat.getColor(this, R.color.primaryBackground);
            lightMode = !isNightMode;
        }

        getWindow().setStatusBarColor(statusBarColor);
        ViewUtil.setLightMode(this, lightMode);
    }

    private NavigationTag getTagForItemId(int itemId) {
        switch (itemId) {
            case R.id.counters:
                return NavigationTag.COUNTERS;
            case R.id.dices:
                return NavigationTag.DICES;
            case R.id.settings:
                return NavigationTag.SETTINGS;
            default:
                throw new IllegalArgumentException("Unknown item ID: " + itemId);
        }
    }

    private MaterialFadeThrough createTransition() {
        MaterialFadeThrough fadeThrough = new MaterialFadeThrough();

        // Add targets for this transition to explicitly run transitions only on these views. Without
        // targeting, a MaterialFadeThrough would be run for every view in the Fragment's layout.
        fadeThrough.addTarget(R.id.counters_fragment);
        fadeThrough.addTarget(R.id.dices_fragment);
        fadeThrough.addTarget(R.id.settings_fragment);

        return fadeThrough;
    }
}