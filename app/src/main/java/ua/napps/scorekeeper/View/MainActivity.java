package ua.napps.scorekeeper.View;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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
import ua.com.napps.scorekeeper.R;
import ua.napps.scorekeeper.Adapters.CountersAdapter;
import ua.napps.scorekeeper.Helpers.NoChangeAnimator;
import ua.napps.scorekeeper.Models.Counter;
import ua.napps.scorekeeper.Models.CurrentSet;
import ua.napps.scorekeeper.Models.Dice;
import ua.napps.scorekeeper.Models.FavoriteSet;
import ua.napps.scorekeeper.Utils.PrefUtil;
import ua.napps.scorekeeper.Utils.ToastUtils;
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
import static ua.napps.scorekeeper.View.EditCounterFragment.CounterUpdateListener;
import static ua.napps.scorekeeper.View.EditDiceFragment.DiceUpdateListener;
import static ua.napps.scorekeeper.View.SettingFragment.SettingsUpdatedListener;
import static ua.napps.scorekeeper.View.SettingFragment.newInstance;

public class MainActivity extends AppCompatActivity implements FavSetLoadedListener, SettingsUpdatedListener, DiceUpdateListener, CounterUpdateListener {

    @Bind(R.id.countersRecyclerView)
    CleverRecyclerView mCountersRecyclerView;
    @Bind(R.id.diceLayout)
    View mDicesBar;
    @Bind(R.id.diceFormula)
    TextView mDiceFormula;
    @Bind(R.id.diceSum)
    TextView mDiceSum;
    @Bind(R.id.toolbar)
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
                setDiceSum(String.format("%d", Dice.getDice().roll()));
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
        if (CurrentSet.getInstance().getSize() == 0) {
            String activeCountersJson = PrefUtil.getString(this, ACTIVE_COUNTERS, "");
            Type listType = new TypeToken<ArrayList<Counter>>() {
            }.getType();
            ArrayList<Counter> counters = new Gson().fromJson(activeCountersJson, listType);
            if (counters == null) {
                counters = new ArrayList<>();
                counters.add(new Counter(getResources().getString(R.string.counter_title_default)));
            }
            CurrentSet.getInstance().setCounters(counters);
        }

        if (PrefUtil.getBoolean(this, PREFS_STAY_AWAKE, true)) {
            toggleKeepScreenOn(true);
        } else {
            toggleKeepScreenOn(false);
        }

        mIsAllCountersVisible = PrefUtil.getBoolean(this, PREFS_SHOW_ALL_COUNTERS, true);

        if (PrefUtil.getBoolean(this, PREFS_SHOW_DICES, false)) {
            toggleDicesBar(true);
            Dice.getDice().setAmount(PrefUtil.getInt(this, PREFS_DICE_AMOUNT, 1));
            Dice.getDice().setMinEdge(PrefUtil.getInt(this, PREFS_DICE_MIN_EDGE, 1));
            Dice.getDice().setMaxEdge(PrefUtil.getInt(this, PREFS_DICE_MAX_EDGE, 6));
            Dice.getDice().setBonus(PrefUtil.getInt(this, PREFS_DICE_BONUS, 0));

            setDiceSum(PrefUtil.getString(this, PREFS_DICE_SUM, "0"));
            setDiceFormula(Dice.getDice().toString());
        } else {
            toggleDicesBar(false);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveSettings();
    }

    @Override
    protected void onDestroy() {
        saveSettings();
        super.onDestroy();
    }

    private void saveSettings() {
        String activeCountersJson = new Gson().toJson(CurrentSet.getInstance().getCounters());
        PrefUtil.putString(this, ACTIVE_COUNTERS, activeCountersJson);

        if (mDicesBar.getVisibility() == VISIBLE) {
            PrefUtil.putInt(this, PREFS_DICE_AMOUNT, Dice.getDice().getAmount());
            PrefUtil.putInt(this, PREFS_DICE_MIN_EDGE, Dice.getDice().getMinEdge());
            PrefUtil.putInt(this, PREFS_DICE_MAX_EDGE, Dice.getDice().getMaxEdge());
            PrefUtil.putInt(this, PREFS_DICE_BONUS, Dice.getDice().getBonus());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        menu.clear();
        inflater.inflate(R.menu.toolbar_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                addCounter();
                updateView();
                mCountersRecyclerView.smoothScrollToPosition(mAdapter.getItemCount());
                break;
            case R.id.action_sets:
                getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_left, R.anim.slide_right, R.anim.slide_left, R.anim.slide_right)
                        .replace(R.id.fragContainer, FavoriteSetsFragment.newInstance(), "favorites").addToBackStack(null).commit();
                break;
            case R.id.action_clear_all:
                CurrentSet.getInstance().removeAllCounters();
                addCounter();
                updateView();
                break;
            case R.id.action_settings:
                getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_left, R.anim.slide_right, R.anim.slide_left, R.anim.slide_right)
                        .replace(R.id.fragContainer, newInstance(), "settings").addToBackStack(null).commit();
                break;
            case R.id.action_report:
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setType("text/plain");
                intent.setData(Uri.parse("mailto:" + SEND_REPORT_EMAIL));
                intent.putExtra(Intent.EXTRA_SUBJECT, String.format("%s %s", getString(R.string.app_name), getString(R.string.app_version_code)));
                startActivity(intent);
                break;
            default:
                break;
        }
        return true;
    }

    private void addCounter() {
        CurrentSet.getInstance().addCounter(newCounter());
    }

    @NonNull
    private Counter newCounter() {
        return new Counter(String.format("%s %d", getString(R.string.counter_title_default), CurrentSet.getInstance().getSize() + 1));
    }

    private void initRecyclerView() {
        mCountersRecyclerView.setItemAnimator(new NoChangeAnimator());
        mCountersRecyclerView.setAdapter(mAdapter);
        mCountersRecyclerView.setScrollAnimationDuration(300);
        mCountersRecyclerView.setFlingFriction(0.99f);
        mCountersRecyclerView.setSlidingThreshold(0.1f);
        mCountersRecyclerView.setOrientation(RecyclerView.VERTICAL);
    }

    @Override
    public void onBackPressed() {
        int count = getFragmentManager().getBackStackEntryCount();

        if (count == 0) {
            super.onBackPressed();
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setTitle(R.string.app_name);
        } else {
            getFragmentManager().popBackStack();
        }
    }

    public void updateView() {
        mAdapter.setCounters(CurrentSet.getInstance().getCounters());
        mAdapter.setCountersVisibility(mIsAllCountersVisible);
        if (mIsAllCountersVisible) {
            mCountersRecyclerView.setVisibleChildCount(mAdapter.getItemCount());
        } else {
            mCountersRecyclerView.setVisibleChildCount(1);
        }
        mAdapter.notifyDataSetChanged();
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

    @Override
    public void onFavSetLoaded(FavoriteSet set) {
        CurrentSet.getInstance().setCounters(set.getCounters());
        updateView();
        ToastUtils.getInstance().showToast(this, String.format("%s loaded", set.getName()), Toast.LENGTH_SHORT);
    }

    @Override
    public void onSettingsUpdated() {
        LoadSettings();
        updateView();
    }

    @Override
    public void onDiceUpdate() {
        setDiceFormula(Dice.getDice().toString());
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