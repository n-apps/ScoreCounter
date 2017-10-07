package ua.napps.scorekeeper.counters;

import android.view.View;

public interface CounterActionCallback {
  void onNameClick(Counter counter);

  boolean onNameLongClick(View v, Counter counter);

  boolean onValueLongClick(View v, Counter counter);

  void onIncreaseClick(Counter counter);

  void onDecreaseClick(Counter counter);

  void onAddCounterClick();
}
