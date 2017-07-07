package ua.napps.scorekeeper.counters;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import java.util.UUID;
import ua.com.napps.scorekeeper.BR;
import ua.napps.scorekeeper.utils.RandomColor;

public final class Counter extends BaseObservable implements Parcelable {
  private String id;
  @Bindable private String name;
  @Bindable private int value;
  private String color;
  private String textColor;
  private int defaultValue;
  private int step;

  public Counter(@NonNull String name) {
    id = UUID.randomUUID().toString();
    this.name = name;
    color = RandomColor.getRandomColor();
    textColor = RandomColor.getContrastColor(color);
    value = 0;
    defaultValue = 0;
    step = 1;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
    notifyPropertyChanged(BR.name);
  }

  public int getValue() {
    return value;
  }

  public void setValue(int value) {
    this.value = value;
    notifyPropertyChanged(BR.value);
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getColor() {
    return color;
  }

  public void setColor(String color) {
    this.color = color;
  }

  public String getTextColor() {
    return textColor;
  }

  public void setTextColor(String textColor) {
    this.textColor = textColor;
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

  @Override public String toString() {
    return "Counter{" + "id='" + id + '\'' + ", name='" + name + '\'' + ", value=" + value + '}';
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Counter counter = (Counter) o;

    if (value != counter.value) return false;
    if (defaultValue != counter.defaultValue) return false;
    if (step != counter.step) return false;
    if (!id.equals(counter.id)) return false;
    if (!name.equals(counter.name)) return false;
    if (!color.equals(counter.color)) return false;
    return textColor.equals(counter.textColor);
  }

  @Override public int hashCode() {
    int result = id.hashCode();
    result = 31 * result + name.hashCode();
    result = 31 * result + value;
    result = 31 * result + color.hashCode();
    result = 31 * result + textColor.hashCode();
    result = 31 * result + defaultValue;
    result = 31 * result + step;
    return result;
  }

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(this.id);
    dest.writeString(this.name);
    dest.writeInt(this.value);
    dest.writeString(this.color);
    dest.writeString(this.textColor);
    dest.writeInt(this.defaultValue);
    dest.writeInt(this.step);
  }

  protected Counter(Parcel in) {
    this.id = in.readString();
    this.name = in.readString();
    this.value = in.readInt();
    this.color = in.readString();
    this.textColor = in.readString();
    this.defaultValue = in.readInt();
    this.step = in.readInt();
  }

  public static final Creator<Counter> CREATOR = new Creator<Counter>() {
    @Override public Counter createFromParcel(Parcel source) {
      return new Counter(source);
    }

    @Override public Counter[] newArray(int size) {
      return new Counter[size];
    }
  };
}
