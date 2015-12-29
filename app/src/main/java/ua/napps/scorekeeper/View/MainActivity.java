package ua.napps.scorekeeper.View;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import static ua.napps.scorekeeper.Helpers.Constants.SEND_REPORT_EMAIL;
import static ua.napps.scorekeeper.Interactors.CurrentSet.getCurrentSet;

public class MainActivity extends AppCompatActivity implements MainView {

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
        setSupportActionBar(mToolbar);

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
                getCurrentSet().addCounter(new Counter("d2d")); /* TODO: remove hardcoded string.*/
                mAdapter.notifyDataSetChanged();
                updateView();
                break;
            // action with ID action_settings was selected
            case R.id.action_sets:
                getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_up, R.anim.slide_dowm, R.anim.slide_up, R.anim.slide_dowm)
                        .replace(R.id.fragContainer, FragmentFav.newInstance(), "favorites").commit();
                break;
            case R.id.action_settings:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                break;
            case R.id.action_report:
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setType("text/plain");
                intent.setData(Uri.parse("mailto:" + SEND_REPORT_EMAIL));
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                startActivity(intent);
                break;
            default:
                break;
        }

        return true;
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

    @Override
    public void onBackPressed() {
        Fragment favFragment = getSupportFragmentManager().findFragmentByTag("favorites");
        if (favFragment != null) {
            closeFragment("favorites");
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void updateView() {
        mAdapter.setCounters(getCurrentSet().getCounters());
        mAdapter.notifyDataSetChanged();

        if (mIsAllCountersVisible) {
            mCountersRecyclerView.setVisibleChildCount(mAdapter.getItemCount());
        } else {
            mCountersRecyclerView.setVisibleChildCount(1);
        }
        mCountersRecyclerView.smoothScrollToPosition(mAdapter.getItemCount()); // TODO: only if counter added
        mCountersRecyclerView.invalidate();
    }

/*
TODO: // This method hides the system bars and resize the content
  private void hideSystemUI() {
    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
            | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
            // remove the following flag for version < API 19
            | View.SYSTEM_UI_FLAG_IMMERSIVE); 
  } 
*/

    @Override
    public void toggleKeepScreenOn(boolean isSelected) {
        if (isSelected) getWindow().addFlags(FLAG_KEEP_SCREEN_ON);
        else getWindow().clearFlags(FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void toggleDicesBar(boolean isShowing) {
        if (isShowing) mDicesBar.setVisibility(VISIBLE);
        else mDicesBar.setVisibility(GONE);
        mCountersRecyclerView.invalidate();
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
        }
    }
}