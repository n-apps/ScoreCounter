package ua.napps.scorekeeper.counters;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "counters") public class Counter implements CounterNew {
  @PrimaryKey(autoGenerate = true) private int id;
  private String name;
  private int value;
  private String color;
  private int defaultValue;
  private int step;

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getValue() {
    return value;
  }

  public void setValue(int value) {
    this.value = value;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getColor() {
    return color;
  }

  public void setColor(String color) {
    this.color = color;
  }

  public int getDefaultValue() {
    return defaultValue;
  }

  public void setDefaultValue(int defaultValue) {
    this.defaultValue = defaultValue;
  }

  public int getStep() {
    return step;
  }

  public void setStep(int step) {
    this.step = step;
  }

  public Counter(@NonNull String name, String color) {
    this.name = name;
    this.color = color;
    value = 0;
    defaultValue = 0;
    step = 1;
  }

  public Counter(Counter counter) {
    id = counter.id;
    this.name = counter.name;
    color = counter.color;
    value = counter.value;
    defaultValue = counter.defaultValue;
    step = counter.step;
  }

  @Override public String toString() {
    return "Counter{" + "id='" + id + '\'' + ", name='" + name + '\'' + ", value=" + value + '}';
  }
}
