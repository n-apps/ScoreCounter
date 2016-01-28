package ua.napps.scorekeeper.View;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nineoldandroids.animation.Animator;
import io.github.luckyandyzhang.cleverrecyclerview.CleverRecyclerView;
import java.lang.reflect.Type;
import java.util.ArrayList;
import ua.com.napps.scorekeeper.R;
import ua.com.napps.scorekeeper.databinding.ActivityMainBinding;
import ua.napps.scorekeeper.Adapters.CountersAdapter;
import ua.napps.scorekeeper.Helpers.NoChangeAnimator;
import ua.napps.scorekeeper.Models.Counter;
import ua.napps.scorekeeper.Models.CurrentSet;
import ua.napps.scorekeeper.Models.Dice;
import ua.napps.scorekeeper.Models.FavoriteSet;
import ua.napps.scorekeeper.Utils.PrefUtil;
import ua.napps.scorekeeper.Utils.ToastUtils;
import ua.napps.scorekeeper.View.FavoriteSetsFragment.FavSetLoadedListener;
import ua.napps.scorekeeper.viewmodel.MainViewModel;

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

@SuppressWarnings({ "WeakerAccess", "unused" }) public class MainActivity extends AppCompatActivity
    implements FavSetLoadedListener, SettingsUpdatedListener, DiceUpdateListener,
    CounterUpdateListener {

  CleverRecyclerView recyclerView;
  LinearLayout dicesBar;
  TextView diceFormula;
  TextView diceSum;
  ImageButton diceShake;

  private ActivityMainBinding binding;
  private MainViewModel mainViewModel;

  private CountersAdapter mAdapter;
  private boolean mIsAllCountersVisible;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
    mainViewModel = new MainViewModel();
    binding.setViewModel(mainViewModel);

    setSupportActionBar(binding.toolbar);

    recyclerView = binding.countersRecyclerView;
    dicesBar = binding.dicesLayout;
    diceFormula = binding.diceFormula;
    diceSum = binding.diceSum;
    diceShake = binding.shakeDices;

    diceFormula.setOnLongClickListener(onFormulaLongClick);
    diceShake.setOnClickListener(onDiceShakeClick);

    if (Build.VERSION.SDK_INT > 18) getWindow().addFlags(FLAG_FULLSCREEN);

    initRecyclerView();
  }

  @Override protected void onResume() {
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
        counters.add(new Counter(getResources().getString(R.string.counter_default_title)));
      }
      CurrentSet.getInstance().setCounters(counters);
    }

    if (PrefUtil.getBoolean(this, PREFS_STAY_AWAKE, true)) {
      toggleKeepScreenOn(true);
    } else {
      toggleKeepScreenOn(false);
    }

    mIsAllCountersVisible = PrefUtil.getBoolean(this, PREFS_SHOW_ALL_COUNTERS, false);

    if (PrefUtil.getBoolean(this, PREFS_SHOW_DICES, false)) {
      toggleDicesBar(true);
      Dice.getDice().setDiceNumber(PrefUtil.getInt(this, PREFS_DICE_AMOUNT, 1));
      Dice.getDice().setMinSide(PrefUtil.getInt(this, PREFS_DICE_MIN_EDGE, 1));
      Dice.getDice().setMaxSide(PrefUtil.getInt(this, PREFS_DICE_MAX_EDGE, 6));
      Dice.getDice().setTotalBonus(PrefUtil.getInt(this, PREFS_DICE_BONUS, 0));

      setDiceSum(PrefUtil.getString(this, PREFS_DICE_SUM, "0"));
      setDiceFormula(Dice.getDice().toString());
    } else {
      toggleDicesBar(false);
    }
  }

  @Override protected void onStop() {
    super.onStop();
    saveSettings();
  }

  @Override protected void onDestroy() {
    saveSettings();
    super.onDestroy();
  }

  private void saveSettings() {
    String activeCountersJson = new Gson().toJson(CurrentSet.getInstance().getCounters());
    PrefUtil.putString(this, ACTIVE_COUNTERS, activeCountersJson);

    if (dicesBar.getVisibility() == VISIBLE) {
      PrefUtil.putInt(this, PREFS_DICE_AMOUNT, Dice.getDice().getDiceNumber());
      PrefUtil.putInt(this, PREFS_DICE_MIN_EDGE, Dice.getDice().getMinSide());
      PrefUtil.putInt(this, PREFS_DICE_MAX_EDGE, Dice.getDice().getMaxSide());
      PrefUtil.putInt(this, PREFS_DICE_BONUS, Dice.getDice().getTotalBonus());
    }
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
     getMenuInflater().inflate(R.menu.toolbar_menu, menu);
    return true;
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.menu_add_counter:
        addCounter();
        updateView();
        recyclerView.smoothScrollToPosition(mAdapter.getItemCount());
        break;
      case R.id.menu_favorite_sets:
        getSupportFragmentManager().beginTransaction()
            .setCustomAnimations(R.anim.slide_left, R.anim.slide_right, R.anim.slide_left,
                R.anim.slide_right)
            .replace(R.id.container, FavoriteSetsFragment.newInstance(), "favorites")
            .addToBackStack(null)
            .commit();
        break;
      case R.id.menu_clear_all:
        CurrentSet.getInstance().removeAllCounters();
        addCounter();
        updateView();
        break;
      case R.id.menu_settings:
        getSupportFragmentManager().beginTransaction()
            .setCustomAnimations(R.anim.slide_left, R.anim.slide_right, R.anim.slide_left,
                R.anim.slide_right)
            .replace(R.id.container, newInstance(), "settings")
            .addToBackStack(null)
            .commit();
        break;
      case R.id.menu_send_feedback:
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setType("text/plain");
        intent.setData(Uri.parse("mailto:" + SEND_REPORT_EMAIL));
        intent.putExtra(Intent.EXTRA_SUBJECT, String.format("%s %s", getString(R.string.app_name),
            getString(R.string.app_version_code)));
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

  @NonNull private Counter newCounter() {
    return new Counter(String.format("%s %d", getString(R.string.counter_default_title),
        CurrentSet.getInstance().getSize() + 1));
  }

  private void initRecyclerView() {

    if (mAdapter == null) mAdapter = new CountersAdapter(this);

    recyclerView.setItemAnimator(new NoChangeAnimator());
    recyclerView.setAdapter(mAdapter);
    recyclerView.setScrollAnimationDuration(300);
    recyclerView.setFlingFriction(0.99f);
    recyclerView.setSlidingThreshold(0.1f);
    recyclerView.setOrientation(RecyclerView.VERTICAL);
  }

  @Override public void onBackPressed() {
    int count = getFragmentManager().getBackStackEntryCount();

    if (count == 0) {
      super.onBackPressed();
      assert getSupportActionBar() != null;
      getSupportActionBar().setDisplayHomeAsUpEnabled(false);
      getSupportActionBar().setTitle(R.string.app_name);
    } else {
      getFragmentManager().popBackStack();
    }
  }

  private void updateView() {
    mAdapter.setCounters(CurrentSet.getInstance().getCounters());
    mAdapter.setCountersVisibility(mIsAllCountersVisible);
    if (mIsAllCountersVisible) {
      recyclerView.setVisibleChildCount(mAdapter.getItemCount());
    } else {
      recyclerView.setVisibleChildCount(1);
    }
    mAdapter.notifyDataSetChanged();
  }

  private void toggleKeepScreenOn(boolean isSelected) {
    if (isSelected) {
      getWindow().addFlags(FLAG_KEEP_SCREEN_ON);
    } else {
      getWindow().clearFlags(FLAG_KEEP_SCREEN_ON);
    }
  }

  private void toggleDicesBar(boolean isShowing) {
    if (isShowing) {
      dicesBar.setVisibility(VISIBLE);
    } else {
      dicesBar.setVisibility(GONE);
    }
    recyclerView.invalidate();
    mAdapter.notifyDataSetChanged();
  }

  View.OnLongClickListener onFormulaLongClick = new View.OnLongClickListener() {
    @Override public boolean onLongClick(View v) {
      FragmentManager fragmentManager = getSupportFragmentManager();
      EditDiceFragment diceDialog = EditDiceFragment.newInstance();
      diceDialog.show(fragmentManager, "dice_dialog");
      return false;
    }
  };

  View.OnClickListener onDiceShakeClick = new View.OnClickListener() {
    @Override public void onClick(View v) {
      YoYo.with(Techniques.Swing).withListener(new Animator.AnimatorListener() {

        @Override public void onAnimationStart(Animator animation) {
        }

        @Override public void onAnimationEnd(Animator animation) {
          YoYo.with(Techniques.SlideInRight).duration(400).playOn(diceSum);
          setDiceSum(String.format("%d", Dice.getDice().roll()));
        }

        @Override public void onAnimationCancel(Animator animation) {

        }

        @Override public void onAnimationRepeat(Animator animation) {

        }
      }).duration(400).playOn(v);
    }
  };

  private void setDiceSum(String sum) {
    diceSum.setText(sum);
  }

  private void setDiceFormula(String formula) {
    diceFormula.setText(formula);
  }

  @Override public void onFavSetLoaded(FavoriteSet set) {
    CurrentSet.getInstance().setCounters(set.getCounters());
    updateView();
    ToastUtils.getInstance()
        .showToast(this, String.format("%s loaded", set.getName()), Toast.LENGTH_SHORT);
  }

  @Override public void onSettingsUpdated() {
    LoadSettings();
    updateView();
  }

  @Override public void onDiceUpdate() {
    setDiceFormula(Dice.getDice().toString());
  }

  @Override public void onCounterUpdate() {
    updateView();
  }

  @Override public void onCounterDelete() {
    updateView();
  }
}