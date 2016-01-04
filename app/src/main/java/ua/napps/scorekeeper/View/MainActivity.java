package ua.napps.scorekeeper.View;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.apkfuns.logutils.LogUtils;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nineoldandroids.animation.Animator;

import java.lang.reflect.Type;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import io.github.luckyandyzhang.cleverrecyclerview.CleverRecyclerView;
import ua.com.napps.scorekeeper.BuildConfig;
import ua.com.napps.scorekeeper.R;
import ua.napps.scorekeeper.Adapters.CountersAdapter;
import ua.napps.scorekeeper.Helpers.NoChangeAnimator;
import ua.napps.scorekeeper.Interactors.Dice;
import ua.napps.scorekeeper.Models.Counter;
import ua.napps.scorekeeper.Models.FavoriteSet;
import ua.napps.scorekeeper.Utils.PrefUtil;
import ua.napps.scorekeeper.View.FavoriteSetsFragment.FavSetLoadedListener;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
import static ua.napps.scorekeeper.Helpers.Constants.ACTIVE_COUNTERS;
import static ua.napps.scorekeeper.Helpers.Constants.PREFS_DICE_AMOUNT;
import static ua.napps.scorekeeper.Helpers.Constants.PREFS_DICE_BONUS;
import static ua.napps.scorekeeper.Helpers.Constants.PREFS_DICE_MAX_EDGE;
import static ua.napps.scorekeeper.Helpers.Constants.PREFS_DICE_MIN_EDGE;
import static ua.napps.scorekeeper.Helpers.Constants.PREFS_DICE_SUM;
import static ua.napps.scorekeeper.Helpers.Constants.PREFS_SHOW_ALL_COUNTERS;
import static ua.napps.scorekeeper.Helpers.Constants.PREFS_SHOW_DICES;
import static ua.napps.scorekeeper.Helpers.Constants.PREFS_STAY_AWAKE;
import static ua.napps.scorekeeper.Helpers.Constants.SEND_REPORT_EMAIL;
import static ua.napps.scorekeeper.Interactors.CurrentSet.getCurrentSet;
import static ua.napps.scorekeeper.View.EditDiceFragment.DiceUpdateListener;

public class MainActivity extends AppCompatActivity implements FavSetLoadedListener, SettingFragment.SettingsUpdatedListener, DiceUpdateListener, EditCounterFragment.CounterUpdateListener {

    @Bind(R.id.countersRecyclerView)
    CleverRecyclerView mCountersRecyclerView;

    @Bind(R.id.diceLayout)
    View mDicesBar;
    @Bind(R.id.diceFormula)
    TextView mDiceFormula;
    @Bind(R.id.diceSum)
    TextView mDiceSum;
    @Bind(R.id.main_toolbar)
    Toolbar mToolbar;

    @OnClick(R.id.shakeDices)
    public void onClickShake(View v) {

        YoYo.with(Techniques.Swing).withListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                YoYo.with(Techniques.FadeIn)
                        .duration(400)
                        .playOn(mDiceSum);
                setDiceSum(String.format("%d", Dice.getDiceInstance().roll()));
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        })
                .duration(400)
                .playOn(v);
    }

    @OnLongClick(R.id.diceFormula)
    public boolean onLongClick(View v) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        EditDiceFragment diceDialog = EditDiceFragment.newInstance();
        diceDialog.show(fragmentManager, "dice_dialog");
        return true;
    }

    CountersAdapter mAdapter;
    private boolean mIsAllCountersVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);

        if (Build.VERSION.SDK_INT > 18) getWindow().addFlags(FLAG_FULLSCREEN);

        LogUtils.configTagPrefix = "*** ";

        if (mAdapter == null) mAdapter = new CountersAdapter(this);

        initRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();

        LoadSettings();
        updateView();
    }

    private void LoadSettings() {
        if (getCurrentSet().getSize() == 0) {
            String activeCountersJson = PrefUtil.getString(this, ACTIVE_COUNTERS, "");
            Type listType = new TypeToken<ArrayList<Counter>>() {
            }.getType();
            ArrayList<Counter> counters = new Gson().fromJson(activeCountersJson, listType);
            if (counters == null) {
                counters = new ArrayList<>();
                counters.add(new Counter(getResources().getString(R.string.counter_title_default)));
            }
            getCurrentSet().setCounters(counters);
        }

        if (PrefUtil.getBoolean(this, PREFS_STAY_AWAKE, true)) {
            toggleKeepScreenOn(true);
        } else {
            toggleKeepScreenOn(false);
        }

        mIsAllCountersVisible = PrefUtil.getBoolean(this, PREFS_SHOW_ALL_COUNTERS, true);

        if (PrefUtil.getBoolean(this, PREFS_SHOW_DICES, false)) {
            toggleDicesBar(true);
            Dice.getDiceInstance().setAmount(PrefUtil.getInt(this, PREFS_DICE_AMOUNT, 1));
            Dice.getDiceInstance().setMinEdge(PrefUtil.getInt(this, PREFS_DICE_MIN_EDGE, 1));
            Dice.getDiceInstance().setMaxEdge(PrefUtil.getInt(this, PREFS_DICE_MAX_EDGE, 6));
            Dice.getDiceInstance().setBonus(PrefUtil.getInt(this, PREFS_DICE_BONUS, 0));

            setDiceSum(PrefUtil.getString(this, PREFS_DICE_SUM, "0"));
            setDiceFormula(Dice.getDiceInstance().toString());
        } else {
            toggleDicesBar(false);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        String activeCountersJson = new Gson().toJson(getCurrentSet().getCounters());
        PrefUtil.putString(this, ACTIVE_COUNTERS, activeCountersJson);

        if (mDicesBar.getVisibility() == VISIBLE) {
            PrefUtil.putInt(this, PREFS_DICE_AMOUNT, Dice.getDiceInstance().getAmount());
            PrefUtil.putInt(this, PREFS_DICE_MIN_EDGE, Dice.getDiceInstance().getMinEdge());
            PrefUtil.putInt(this, PREFS_DICE_MAX_EDGE, Dice.getDiceInstance().getMaxEdge());
            PrefUtil.putInt(this, PREFS_DICE_BONUS, Dice.getDiceInstance().getBonus());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.action_add:
                getCurrentSet().addCounter(new Counter(String.format("Counter %d", getCurrentSet().getSize()))); /* TODO: remove hardcoded string.*/
                updateView();
                mCountersRecyclerView.smoothScrollToPosition(mAdapter.getItemCount());
                break;
            // action with ID action_settings was selected
            case R.id.action_sets:
                getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_up, R.anim.slide_dowm, R.anim.slide_up, R.anim.slide_dowm)
                        .replace(R.id.fragContainer, FavoriteSetsFragment.newInstance(), "favorites").addToBackStack(null).commit();
                break;
            case R.id.action_settings:
                getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_up, R.anim.slide_dowm, R.anim.slide_up, R.anim.slide_dowm)
                        .replace(R.id.fragContainer, SettingFragment.newInstance(), "settings").addToBackStack(null).commit();
                break;
            case R.id.action_report:
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setType("text/plain");
                intent.setData(Uri.parse("mailto:" + SEND_REPORT_EMAIL));
                intent.putExtra(Intent.EXTRA_SUBJECT, String.format("%s %s", getString(R.string.app_name), BuildConfig.VERSION_NAME));
                startActivity(intent);
                break;
            default:
                break;
        }

        return true;
    }

    private void initRecyclerView() {
        mCountersRecyclerView.setItemAnimator(new NoChangeAnimator());
        mCountersRecyclerView.setAdapter(mAdapter);
        mCountersRecyclerView.setScrollAnimationDuration(300);
        mCountersRecyclerView.setFlingFriction(0.99f);
        mCountersRecyclerView.setSlidingThreshold(0.1f);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mCountersRecyclerView.setOrientation(RecyclerView.VERTICAL);
        } else {
            mCountersRecyclerView.setOrientation(RecyclerView.HORIZONTAL);
        }
    }

    @Override
    public void onBackPressed() {
        int count = getFragmentManager().getBackStackEntryCount();

        if (count == 0) {
            super.onBackPressed();
        } else {
            getFragmentManager().popBackStack();
        }
    }


    public void updateView() {
        mAdapter.setCounters(getCurrentSet().getCounters());
        mAdapter.setCountersVisibility(mIsAllCountersVisible);
        mAdapter.notifyDataSetChanged();
        if (mIsAllCountersVisible) {
            mCountersRecyclerView.setVisibleChildCount(mAdapter.getItemCount());
        } else {
            mCountersRecyclerView.setVisibleChildCount(1);
        }
        mCountersRecyclerView.invalidate();
        mCountersRecyclerView.requestLayout();
    }

    public void toggleKeepScreenOn(boolean isSelected) {
        if (isSelected) getWindow().addFlags(FLAG_KEEP_SCREEN_ON);
        else getWindow().clearFlags(FLAG_KEEP_SCREEN_ON);
    }


    public void toggleDicesBar(boolean isShowing) {
        if (isShowing) mDicesBar.setVisibility(VISIBLE);
        else mDicesBar.setVisibility(GONE);
        mCountersRecyclerView.invalidate();
        mAdapter.notifyDataSetChanged();
    }

    public void setDiceSum(String sum) {
        mDiceSum.setText(sum);
    }

    public void setDiceFormula(String formula) {
        mDiceFormula.setText(formula);
    }

    public void closeFragment(String tag) {
        Fragment favFragment = getSupportFragmentManager().findFragmentByTag(tag);
        if (favFragment != null) {
            getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_up, R.anim.slide_dowm, R.anim.slide_up, R.anim.slide_dowm)
                    .remove(favFragment).commit();
            getSupportFragmentManager().popBackStack();
        }
    }

    @Override
    public void onFavSetLoaded(FavoriteSet set) {
        closeFragment("favorites");
        getCurrentSet().setCounters(set.getCounters());
        updateView();
    }

    @Override
    public void onSettingsUpdated() {
        LoadSettings();
        updateView();
    }

    @Override
    public void onDiceUpdate() {
        setDiceFormula(Dice.getDiceInstance().toString());
    }

    @Override
    public void onCounterUpdate() {
        updateView();
    }

    @Override
    public void onCounterDelete() {
        updateView();
    }
}