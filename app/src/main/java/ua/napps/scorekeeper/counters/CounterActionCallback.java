package ua.napps.scorekeeper.counters;

public interface CounterActionCallback {
  void onNameClick(Counter counter);

  void onIncreaseClick(Counter counter);

  void onDecreaseClick(Counter counter);
}
