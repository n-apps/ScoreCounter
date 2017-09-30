package ua.napps.scorekeeper.data;

import android.databinding.ObservableArrayList;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import ua.napps.scorekeeper.counters.Counter;

public final class CurrentSet {

  private ObservableArrayList<Counter> counters;
  private static HashSet<String> alreadyUsedColors;
  private static CurrentSet sCurrentSet;

  public synchronized static CurrentSet getInstance() {
    if (sCurrentSet == null) {
      sCurrentSet = new CurrentSet();
      alreadyUsedColors = new HashSet<>();
    }
    return sCurrentSet;
  }

  private CurrentSet() {
    counters = new ObservableArrayList<>();
  }

  public int getSize() {
    return counters.size();
  }

  public void setCounters(@NonNull ObservableArrayList<Counter> counters) {
    this.counters = counters;
  }

  public void setCounters(@NonNull ArrayList<Counter> counters) {
    this.counters.addAll(counters);
  }

  public Counter getCounter(int position) {
    return counters.get(position);
  }

  public ObservableArrayList<Counter> getCounters() {
    return counters;
  }

  public synchronized void removeCounter(Counter item) {
    counters.remove(item);
    alreadyUsedColors.remove(item.getColor());
  }

  public synchronized void removeCounter(String id) {
    final Counter counter = getCounter(id);
    if (counter != null) {
      removeCounter(counter);
    }
  }

  public void addCounter(String caption, String color) {
    assert caption != null;
    assert color != null;
    counters.add(new Counter(caption, color));
    alreadyUsedColors.add(color);
  }

  public synchronized void removeAllCounters() {
    counters.clear();
    alreadyUsedColors.clear();
  }

  @Nullable public Counter getCounter(@NonNull String id) {
    for (Counter counter : counters) {
      if (counter.getId().equals(id)) {
        return counter;
      }
    }
    return null;
  }

  public void replaceCounter(Counter counter) {
    for (int i = 0; i < counters.size(); i++) {
      Counter c = counters.get(i);
      if (c.getId().equals(counter.getId())) {
        counters.set(i, counter);
        alreadyUsedColors.remove(c.getColor());
        alreadyUsedColors.add(counter.getColor());
        return;
      }
    }
  }

  public void resetAllCounters() {
    for (Counter counter : counters) counter.setValue(counter.getDefaultValue());
  }

  public HashSet<String> getAlreadyUsedColors() {
    return alreadyUsedColors;
  }
}
