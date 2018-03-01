package ua.napps.scorekeeper.counters;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.view.WindowManager;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import com.ashokvarma.bottomnavigation.TextBadgeItem;
import com.github.fernandodev.easyratingdialog.library.EasyRatingDialog;
import com.google.firebase.analytics.FirebaseAnalytics;

import ua.com.napps.scorekeeper.R;
import ua.napps.scorekeeper.app.Constants;
import ua.napps.scorekeeper.dice.DicesFragment;
import ua.napps.scorekeeper.settings.SettingsFragment;
import ua.napps.scorekeeper.storage.TinyDB;
import ua.napps.scorekeeper.utils.AndroidFirebaseAnalytics;
import ua.napps.scorekeeper.utils.ViewUtil;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener, BottomNavigationBar.OnTabSelectedListener, DicesFragment.OnDiceFragmentInteractionListener {

    private static final String TAG_SETTINGS_FRAGMENT = "settingsFragment";
    private static final String TAG_COUNTERS_FRAGMENT = "countersFragment";
    private static final String TAG_DICES_FRAGMENT = "dicesFragment";
    private static final String STATE_LAST_DICE_RESULT = "STATE_LAST_DICE_RESULT";
    private static final String[] TAGS = new String[]{TAG_COUNTERS_FRAGMENT, TAG_DICES_FRAGMENT, TAG_SETTINGS_FRAGMENT};

    private TinyDB settingsDB;
    private EasyRatingDialog easyRatingDialog;
    private Fragment currentFragment;
    private FragmentManager manager;
    private TextBadgeItem diceNumberBadgeItem;
    private int lastSelectedPosition;
    private int lastDiceResult;
    private boolean isThemeLight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settingsDB = new TinyDB(getApplicationContext());
        isThemeLight = settingsDB.getBoolean(Constants.SETTINGS_DICE_THEME_LIGHT, true);
        AppCompatDelegate.setDefaultNightMode(isThemeLight ? AppCompatDelegate.MODE_NIGHT_NO : AppCompatDelegate.MODE_NIGHT_YES);
        setContentView(R.layout.activity_main);
        easyRatingDialog = new EasyRatingDialog(this);
        manager = getSupportFragmentManager();
        settingsDB.registerOnSharedPreferenceChangeListener(this);
        lastSelectedPosition = settingsDB.getInt(Constants.LAST_SELECTED_BOTTOM_TAB);
        diceNumberBadgeItem = new TextBadgeItem().setHideOnSelect(true).hide(false).setBackgroundColorResource(R.color.accentColor);
        BottomNavigationBar bottomNavigationBar = findViewById(R.id.bottom_navigation_bar);
        bottomNavigationBar
                .addItem(new BottomNavigationItem(R.drawable.ic_plus_one_white, "Counters"))
                .addItem(new BottomNavigationItem(R.drawable.ic_dice, "Dice").setBadgeItem(diceNumberBadgeItem))
                .addItem(new BottomNavigationItem(R.drawable.ic_settings, "Settings"))
                .setMode(BottomNavigationBar.MODE_FIXED_NO_TITLE)
                .setBarBackgroundColor(isThemeLight ? R.color.primaryColor : R.color.black)
                .setBackgroundStyle(BottomNavigationBar.BACKGROUND_STYLE_STATIC)
                .setActiveColor(R.color.white)
                .setFirstSelectedPosition(lastSelectedPosition)
                .initialise();
        bottomNavigationBar.setTabSelectedListener(this);

        switchFragment(TAGS[lastSelectedPosition]);
        if (isThemeLight) {
            ViewUtil.setLightStatusBar(this, lastSelectedPosition > 0);
        }
        applyKeepScreenOn(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putInt(STATE_LAST_DICE_RESULT, lastDiceResult);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            updateLastDiceResult(savedInstanceState.getInt(STATE_LAST_DICE_RESULT));
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
        settingsDB.putInt(Constants.LAST_SELECTED_BOTTOM_TAB, lastSelectedPosition);
        if (lastDiceResult > 0) {
            diceNumberBadgeItem.setText("" + lastDiceResult);
        } else {
            diceNumberBadgeItem.hide(false);
        }
        if (isThemeLight) {
            ViewUtil.setLightStatusBar(this, position > 0);
        }
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
    public void updateLastDiceResult(int number) {
        lastDiceResult = number;
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
                currentFragment = DicesFragment.newInstance(lastDiceResult);
                break;
            case TAG_SETTINGS_FRAGMENT:
                currentFragment = SettingsFragment.newInstance();
                break;
        }
        manager.beginTransaction().replace(R.id.container, currentFragment, tag).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE).commit();
    }

    private void applyKeepScreenOn(boolean trackAnalytics) {
        final boolean isStayAwake = settingsDB.getBoolean(Constants.SETTINGS_KEEP_SCREEN_ON, true);
        if (isStayAwake) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        if (trackAnalytics) {
            Bundle params = new Bundle();
            params.putLong(FirebaseAnalytics.Param.SCORE, isStayAwake ? 1 : 0);
            AndroidFirebaseAnalytics.logEvent(getApplicationContext(), "settings_keep_screen_on", params);
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
        settingsDB.unregisterOnSharedPreferenceChangeListener(this);
        settingsDB = null;
    }
}

