package ua.napps.scorekeeper.counters;

import android.view.View;

public interface CounterActionCallback {
  void onNameClick(String id);

  boolean onNameLongClick(View v, Counter counter);

  boolean onValueLongClick(View v, Counter counter);

  void onIncreaseClick(String id);

  void onDecreaseClick(String id);

  void onAddCounterClick();

  void scrollToPosition(int position);
}
