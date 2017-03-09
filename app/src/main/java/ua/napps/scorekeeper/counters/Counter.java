package ua.napps.scorekeeper.counters;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.UUID;
import ua.com.napps.scorekeeper.BR;

import static ua.napps.scorekeeper.utils.ColorUtil.getContrastColor;
import static ua.napps.scorekeeper.utils.ColorUtil.getRandomColor;

public final class Counter extends BaseObservable implements Parcelable {
    private String id;
    @Bindable private String name;
    @Bindable private int value;
    private int backgroundColor;
    private int textColor;
    private int defaultValue;
    private int step;
    private int rotation;

    public Counter(String name) {
        id = UUID.randomUUID().toString();
        this.name = name;
        backgroundColor = getRandomColor();
        textColor = getContrastColor(backgroundColor);
        value = 0;
        defaultValue = 0;
        step = 1;
        rotation = 0;
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

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
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

    public int getRotation() {
        return rotation;
    }

    public void setRotation(int rotation) {
        this.rotation = rotation;
    }

    @Override public int describeContents() {
        return 0;
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.name);
        dest.writeInt(this.value);
        dest.writeInt(this.backgroundColor);
        dest.writeInt(this.textColor);
        dest.writeInt(this.defaultValue);
        dest.writeInt(this.step);
        dest.writeInt(this.rotation);
    }

    protected Counter(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.value = in.readInt();
        this.backgroundColor = in.readInt();
        this.textColor = in.readInt();
        this.defaultValue = in.readInt();
        this.step = in.readInt();
        this.rotation = in.readInt();
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
