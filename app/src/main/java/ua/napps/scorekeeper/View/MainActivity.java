package ua.napps.scorekeeper.View;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.apkfuns.logutils.LogUtils;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.nineoldandroids.animation.Animator;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.luckyandyzhang.cleverrecyclerview.CleverRecyclerView;
import ua.com.napps.scorekeeper.R;
import ua.napps.scorekeeper.Adapters.CountersAdapter;
import ua.napps.scorekeeper.Helpers.NoChangeAnimator;
import ua.napps.scorekeeper.Interactors.Dice;
import ua.napps.scorekeeper.Models.Counter;
import ua.napps.scorekeeper.Presenter.MainPresenterImpl;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
import static ua.napps.scorekeeper.Interactors.CurrentSet.getCurrentSet;

public class MainActivity extends AppCompatActivity implements MainView {

    @Bind(R.id.countersRecyclerView)
    CleverRecyclerView mCountersRecyclerView;
    @Bind(R.id.drawerLayout)
    DrawerLayout mDrawer;
    @Bind(R.id.diceLayout)
    View mDicesBar;
    @Bind(R.id.diceFormula)
    TextView mDiceFormula;
    @Bind(R.id.diceSum)
    TextView mDiceSum;


    @OnClick(R.id.addCounterMenuItem)
    public void onClickAddCounter(View v) {
        mDrawer.closeDrawers();
        getCurrentSet().addCounter(new Counter("d2d"));
        mAdapter.notifyDataSetChanged();
        updateView();
    }

    @OnClick(R.id.openFavoritesMenuItem)
    public void onFavButtonClick(View v) {
        mDrawer.closeDrawers();
        mDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_up, R.anim.slide_dowm, R.anim.slide_up, R.anim.slide_dowm)
                .replace(R.id.fragContainer, FragmentFav.newInstance(), "favorites").commit();
    }

    @OnClick(R.id.openPreferencesMenuItem)
    public void onPrefButtonClick(View v) {
        mDrawer.closeDrawers();
        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
    }

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
                setDiceSum(String.format("%d", Dice.getInstance().roll()));
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

    @OnClick(R.id.diceFormula)
    public void onClickFormula(View v) {
        new DiceDialog(this);
    }

    MainPresenterImpl mPresenter;
    CountersAdapter mAdapter;
    private boolean mIsAllCountersVisible;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        if (Build.VERSION.SDK_INT > 18) getWindow().addFlags(FLAG_FULLSCREEN);

        LogUtils.configTagPrefix = "*** ";

        if (mAdapter == null) mAdapter = new CountersAdapter(this);

        initRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getMainPresenter().onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        getMainPresenter().onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        getMainPresenter().onStop();
    }

    public MainPresenterImpl getMainPresenter() {
        if (mPresenter == null) {
            mPresenter = new MainPresenterImpl(this);
        }
        return mPresenter;
    }

    @Override
    public Context getContext() {
        return this;
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

    public void onDialogClickDeleteCounter(Counter counter) {
        getCurrentSet().removeCounter(counter);
        updateView();
    }

    @Override
    public void onBackPressed() {
        mDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        if (mDrawer.isDrawerOpen(GravityCompat.START)) mDrawer.closeDrawers();
        else {
            Fragment favFragment = getSupportFragmentManager().findFragmentByTag("favorites");
            if (favFragment != null) {
                closeFragment("favorites");
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    public void updateView() {
        mAdapter.setCounters(getCurrentSet().getCounters());
        mAdapter.notifyDataSetChanged();

        if (mIsAllCountersVisible) {
            mCountersRecyclerView.setVisibleChildCount(mAdapter.getItemCount());
            mCountersRecyclerView.smoothScrollToPosition(mAdapter.getItemCount());
        } else {
            mCountersRecyclerView.setVisibleChildCount(1);
        }
        mCountersRecyclerView.invalidate();
    }

    @Override
    public void toggleKeepScreenOn(boolean isSelected) {
        if (isSelected) getWindow().addFlags(FLAG_KEEP_SCREEN_ON);
        else getWindow().clearFlags(FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void toggleDicesBar(boolean isShowing) {
        if (isShowing) mDicesBar.setVisibility(VISIBLE);
        else mDicesBar.setVisibility(GONE);
    }

    @Override
    public void showAllCountersOnScreen(boolean isShowing) {
        mIsAllCountersVisible = isShowing;
    }

    @Override
    public void setDiceSum(String sum) {
        mDiceSum.setText(sum);
    }

    @Override
    public void setDiceFormula(String formula) {
        mDiceFormula.setText(formula);
    }

    @Override
    public void closeFragment(String tag) {
        Fragment favFragment = getSupportFragmentManager().findFragmentByTag(tag);
        if (favFragment != null) {
            getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_up, R.anim.slide_dowm, R.anim.slide_up, R.anim.slide_dowm)
                    .remove(favFragment).commit();
            getSupportFragmentManager().popBackStack();
            mDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        }
    }
}