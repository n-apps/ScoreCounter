package ua.napps.scorekeeper.app;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.transition.Transition;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.transition.MaterialSharedAxis;

import ua.napps.scorekeeper.R;
import ua.napps.scorekeeper.counters.CountersFragment;
import ua.napps.scorekeeper.dice.DicesFragment;
import ua.napps.scorekeeper.dice.OnDiceFragmentInteractionListener;
import ua.napps.scorekeeper.settings.LocalSettings;
import ua.napps.scorekeeper.settings.SettingsFragment;
import ua.napps.scorekeeper.utils.RateMyAppDialog;
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
    private int currentDiceRoll;
    private int previousDiceRoll;
    private boolean isKeepScreenOn;
    private BottomNavigationView bottomNavigationBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isKeepScreenOn = LocalSettings.isKeepScreenOnEnabled();
        if (savedInstanceState != null) {
            currentDiceRoll = savedInstanceState.getInt(STATE_CURRENT_DICE_ROLL);
            previousDiceRoll = savedInstanceState.getInt(STATE_PREVIOUS_DICE_ROLL);
        }
        boolean isLightTheme = LocalSettings.isLightTheme();

        // If android Q override night mode settings from system default
        if (Utilities.hasQ()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

            int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

            LocalSettings.saveDarkTheme((currentNightMode == Configuration.UI_MODE_NIGHT_YES));
        } else {
            AppCompatDelegate.setDefaultNightMode(isLightTheme ? AppCompatDelegate.MODE_NIGHT_NO : AppCompatDelegate.MODE_NIGHT_YES);
        }

        setContentView(R.layout.activity_main);
        rateMyAppDialog = new RateMyAppDialog(this);
        manager = getSupportFragmentManager();

        bottomNavigationBar = findViewById(R.id.bottom_navigation);
        bottomNavigationBar.setSelectedItemId(R.id.counters);

        bottomNavigationBar.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.counters:
                    switchFragment(TAGS[0]);
                    if (currentDiceRoll > 0) {
                        BadgeDrawable badge = bottomNavigationBar.getOrCreateBadge(R.id.dices);
                        badge.setVisible(true);
                        int primary = ContextCompat.getColor(this, R.color.colorSecondary);
                        badge.setBackgroundColor(primary);
                        badge.setNumber(currentDiceRoll);
                    } else {
                        hideDiceBadge();
                    }
                    bottomNavigationBar.setElevation(0);
                    break;
                case R.id.dices:
                    switchFragment(TAGS[1]);
                    hideDiceBadge();
                    bottomNavigationBar.setElevation(0);
                    break;
                case R.id.more:
                    switchFragment(TAGS[2]);
                    hideDiceBadge();
                    bottomNavigationBar.setElevation(20);
                    break;
            }

            return true;
        });

        bottomNavigationBar.setOnItemReselectedListener(item -> {
            if (item.getItemId() == R.id.counters) {
                if (currentFragment instanceof CountersFragment) {
                    ((CountersFragment) currentFragment).scrollToTop();
                }
            }
        });

        switchFragment(TAGS[0]);

        if (isLightTheme) {
            ViewUtil.setLightStatusBar(this);
        } else {
            ViewUtil.clearLightStatusBar(this);
        }
        ViewUtil.setNavBarColor(this, isLightTheme);
        applyKeepScreenOnIfNeeded();
    }

    private void hideDiceBadge() {
        BadgeDrawable badge = bottomNavigationBar.getBadge(R.id.dices);
        if (badge != null) {
            badge.setVisible(false);
            badge.clearNumber();
        }
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
                applyKeepScreenOnIfNeeded();
                break;
            case LocalSettings.DARK_THEME:
                if (LocalSettings.isLightTheme()) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
                bottomNavigationBar.setSelectedItemId(R.id.counters);
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
        Transition enterTrans = new MaterialSharedAxis(MaterialSharedAxis.X, true);
        currentFragment.setEnterTransition(enterTrans);
        manager.beginTransaction()
                .replace(R.id.container, currentFragment, tag)
                .commit();
    }

    private void applyKeepScreenOnIfNeeded() {
        if (isKeepScreenOn) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    public void onBackPressed() {
        if (bottomNavigationBar.getSelectedItemId() == R.id.counters) {
            super.onBackPressed();
        } else {
            bottomNavigationBar.setSelectedItemId(R.id.counters);
        }
    }
}