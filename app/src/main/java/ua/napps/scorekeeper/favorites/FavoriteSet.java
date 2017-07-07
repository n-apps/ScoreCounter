package ua.napps.scorekeeper.favorites;

import android.databinding.ObservableArrayList;
import ua.napps.scorekeeper.counters.Counter;

public class FavoriteSet {

  private final String name;
  private final ObservableArrayList<Counter> counters;

  public FavoriteSet(String name, ObservableArrayList<Counter> counters) {
    this.name = name;
    this.counters = counters;
  }

  public String getName() {
    return name;
  }

  public ObservableArrayList<Counter> getCounters() {
    return counters;
  }

  @Override public String toString() {
    return "FavoriteSet{" + "name='" + name + '\'' + ", counters=" + counters + '}';
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    FavoriteSet that = (FavoriteSet) o;

    if (!name.equals(that.name)) return false;
    return counters.equals(that.counters);
  }

  @Override public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + counters.hashCode();
    return result;
  }
}
