package ua.napps.scorekeeper.View;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.apkfuns.logutils.LogUtils;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.nineoldandroids.animation.Animator;

import butterknife.Bind;
import butterknife.ButterKnife;
import ua.com.napps.scorekeeper.R;
import ua.napps.scorekeeper.AwesomeLayout;
import ua.napps.scorekeeper.Interactors.Dice;
import ua.napps.scorekeeper.Models.Counter;
import ua.napps.scorekeeper.Presenter.MainPresenterImpl;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

public class MainActivity extends AppCompatActivity implements MainView {
    MainPresenterImpl mPresenter;
    @Bind(R.id.counterLayout)
    AwesomeLayout mCountersLayout;
    @Bind(R.id.addCounter)
    TextView mBtnAddCounter;
    @Bind(R.id.drawerLayout)
    DrawerLayout mDrawer;
    @Bind(R.id.dices_bar)
    View mDicesBar;
    @Bind(R.id.diceFormula)
    TextView mDiceFormula;
    @Bind(R.id.diceSum)
    TextView mDiceSum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mCountersLayout.init(this);
        if (Build.VERSION.SDK_INT > 18) getWindow().addFlags(FLAG_FULLSCREEN);

        LogUtils.configTagPrefix = "*** ";
        LogUtils.i("onCreate");
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtils.i("onResume");
        getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mCountersLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                getMainPresenter().loadCurrentSet();
            }
        });

        getMainPresenter().onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        LogUtils.i("onStart");
        getMainPresenter().onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        LogUtils.i("onStop");
        getMainPresenter().onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        LogUtils.i("onPause");
    }

    public MainPresenterImpl getMainPresenter() {
        if (mPresenter == null) {
            mPresenter = new MainPresenterImpl(this);
        }
        return mPresenter;
    }

    public void onClickAddCounter(View v) {
        mDrawer.closeDrawers();
        getMainPresenter().addCounter();
    }

    public void onFavButtonClick(View v) {
        getMainPresenter().loadFragment();
    }

    public void onCounterSwipe(Counter counter, int direction, boolean isSwipe) {
        getMainPresenter().onSwipe(counter, direction, isSwipe);
    }

    public void onDialogClickDeleteCounter(Counter counter) {
        getMainPresenter().removeCounter(counter);
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

    public void onPrefButtonClick(View v) {
        mDrawer.closeDrawers();
        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
    }


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

    public void onClickFormula(View v) {
        getMainPresenter().showDiceDialog();
    }

    @Override
    public Context getContext() {
        LogUtils.i("getContext");
        return this;
    }

    @Override
    public void clearViews() {
        mCountersLayout.removeAllViews();
        LogUtils.i("clearViews");
    }

    @Override
    public void addCounter(Counter counter) {
        mCountersLayout.createCounterView(counter);
    }

    @Override
    public void removeCounter(Counter counter) {
        mCountersLayout.destroyCounterView(counter);
    }

    @Override
    public void toggleKeepScreenOn(boolean isSelected) {
        LogUtils.i("toggleKeepScreenOn");
        if (isSelected) getWindow().addFlags(FLAG_KEEP_SCREEN_ON);
        else getWindow().clearFlags(FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void toggleDicesBar(boolean isShowing) {
        LogUtils.i("toggleDicesBar");
        if (isShowing) {
            mDicesBar.setVisibility(VISIBLE);
            Dice.getInstance();
        } else mDicesBar.setVisibility(GONE);
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
    public void loadFragment(Fragment fragment) {
        mDrawer.closeDrawers();
        mDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_up, R.anim.slide_dowm, R.anim.slide_up, R.anim.slide_dowm)
                .replace(R.id.fragContainer, fragment, "favorites").commit();
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

    @Override
    public void CounterCaptionClick(Counter counter, boolean isNeutralButtonEnabled) {
        new DialogEditCounter(this, counter, isNeutralButtonEnabled);
    }

    @Override
    public void changeAddCounterButtonState(boolean isVisible) {
        LogUtils.i("changeAddCounterButtonState");
        if (isVisible) mBtnAddCounter.setAlpha(1);
        else mBtnAddCounter.setAlpha(0.3f);
        mBtnAddCounter.setEnabled(isVisible);
    }

}
