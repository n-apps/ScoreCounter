package ua.napps.scorekeeper.counters;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import com.ashokvarma.bottomnavigation.TextBadgeItem;
import com.github.fernandodev.easyratingdialog.library.EasyRatingDialog;
import com.google.firebase.analytics.FirebaseAnalytics;

import ua.com.napps.scorekeeper.R;
import ua.napps.scorekeeper.dice.DicesFragment;
import ua.napps.scorekeeper.settings.Constants;
import ua.napps.scorekeeper.settings.SettingsFragment;
import ua.napps.scorekeeper.storage.TinyDB;
import ua.napps.scorekeeper.utils.AndroidFirebaseAnalytics;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener, BottomNavigationBar.OnTabSelectedListener, DicesFragment.OnDiceFragmentInteractionListener {

    private final static String TAG_SETTINGS_FRAGMENT = "settingsFragment";
    private final static String TAG_COUNTERS_FRAGMENT = "countersFragment";
    private final static String TAG_DICES_FRAGMENT = "dicesFragment";
    private final static String[] TAGS = new String[]{TAG_COUNTERS_FRAGMENT, TAG_DICES_FRAGMENT, TAG_SETTINGS_FRAGMENT};

    private TinyDB settingsDB;
    private EasyRatingDialog easyRatingDialog;
    private Fragment currentFragment;
    private FragmentManager manager;
    private int lastSelectedPosition;
    private BottomNavigationBar bottomNavigationBar;
    private TextBadgeItem diceNumberBadgeItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomNavigationBar = findViewById(R.id.bottom_navigation_bar);
        easyRatingDialog = new EasyRatingDialog(this);
        manager = getSupportFragmentManager();
        settingsDB = new TinyDB(getApplicationContext());
        settingsDB.registerOnSharedPreferenceChangeListener(this);
        lastSelectedPosition = settingsDB.getInt(Constants.LAST_SELECTED_BOTTOM_TAB);
        int primary = ContextCompat.getColor(this, R.color.primaryColor);
        diceNumberBadgeItem = new TextBadgeItem().setHideOnSelect(true).hide(false).setBackgroundColor(primary);
        bottomNavigationBar
                .addItem(new BottomNavigationItem(R.drawable.ic_plus_one_white, "Counters").setActiveColor(primary))
                .addItem(new BottomNavigationItem(R.drawable.ic_dice_white, "Dice").setBadgeItem(diceNumberBadgeItem))
                .addItem(new BottomNavigationItem(R.drawable.ic_settings, "Settings").setActiveColor(primary))
                .setMode(BottomNavigationBar.MODE_FIXED_NO_TITLE)
                .setBackgroundStyle(BottomNavigationBar.BACKGROUND_STYLE_STATIC)
                .setFirstSelectedPosition(lastSelectedPosition)
                .initialise();
        bottomNavigationBar.setTabSelectedListener(this);

        switchFragment(TAGS[lastSelectedPosition]);
        applyKeepScreenOn(true);
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

    }

    @Override
    public void onTabUnselected(int position) {

    }

    @Override
    public void onTabReselected(int position) {
        // TODO: 17-Feb-18 scroll counters recyclerview to top
    }


    @Override
    public void updateDiceNavMenuBadge(int number) {
        diceNumberBadgeItem.setText("" + number);
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
        switch (key) {
            case Constants.SETTINGS_KEEP_SCREEN_ON:
                applyKeepScreenOn(false);
                break;
        }
    }

    private void switchFragment(String tag) {
        Fragment fragment = manager.findFragmentByTag(tag);
        if (fragment == null) {
            switch (tag) {
                case TAG_COUNTERS_FRAGMENT:
                    fragment = CountersFragment.newInstance();
                    if (currentFragment != null) {
                        manager.beginTransaction().hide(currentFragment).add(R.id.container, fragment, tag).commit();
                    } else {
                        manager.beginTransaction().add(R.id.container, fragment, tag).commit();
                    }

                    currentFragment = fragment;

                    break;
                case TAG_DICES_FRAGMENT:
                    fragment = DicesFragment.newInstance();
                    if (currentFragment != null) {
                        manager.beginTransaction().hide(currentFragment).add(R.id.container, fragment, tag).commit();
                    } else {
                        manager.beginTransaction().add(R.id.container, fragment, tag).commit();
                    }
                    currentFragment = fragment;
                    break;
                case TAG_SETTINGS_FRAGMENT:
                    fragment = SettingsFragment.newInstance();
                    if (currentFragment != null) {
                        manager.beginTransaction().hide(currentFragment).add(R.id.container, fragment, tag).commit();
                    } else {
                        manager.beginTransaction().add(R.id.container, fragment, tag).commit();
                    }
                    currentFragment = fragment;
                    break;
            }
        } else if (fragment.isHidden()) {
            manager.beginTransaction().hide(currentFragment).show(fragment).commit();
            currentFragment = fragment;
        }
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
}

