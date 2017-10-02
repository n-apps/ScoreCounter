package ua.napps.scorekeeper.counters;

import android.view.View;

public interface CounterActionCallback {
  void onNameClick(int id);

  boolean onNameLongClick(View v, Counter counter);

  boolean onValueLongClick(View v, Counter counter);

  void onIncreaseClick(int id);

  void onDecreaseClick(int id);

  void onAddCounterClick();

  void scrollToPosition(int position);
}
