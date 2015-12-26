package ua.napps.scorekeeper.Models;

import android.graphics.Color;

import com.github.lzyzsd.randomcolor.RandomColor;

public final class Counter {
    private String caption = "Counter";
    private int mValue;
    private int mBackgroundColor;
    private int mTextColor;
    private int mDefaultValue;
    private int mMinValue;
    private int mMaxValue;
    private int mStep;

    public Counter(String name) {
        caption = name;
        mBackgroundColor = getRandomColor();
        mTextColor = defineTextColor();
        mValue = 0;
        mDefaultValue = 0;
        mMinValue = -999;
        mMaxValue = 1000;
        mStep = 1;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public void setValue(int value) {
        this.mValue = value;
    }

    public String getCaption() {
        return caption;
    }

    public int getValue() {
        return mValue;
    }

    public void setColor(int color) {
        this.mBackgroundColor = color;
    }

    public int getColor() {
        return mBackgroundColor;
    }

    public int getDefValue() {
        return mDefaultValue;
    }

    public void setDefValue(int defValue) {
        this.mDefaultValue = defValue;
    }

    public int getMinValue() {
        return mMinValue;
    }

    public void setMinValue(int minValue) {
        this.mMinValue = minValue;
        if (mValue >= minValue) return;
        mValue = minValue;
    }

    public int getMaxValue() {
        return mMaxValue;
    }

    public void setMaxValue(int maxValue) {
        this.mMaxValue = maxValue;
        if (mValue <= maxValue) return;
        mValue = maxValue;
    }

    public int getStep() {
        return mStep;
    }

    public void setStep(int step) {
        this.mStep = step;
    }

    public void increaseValue() {
        mValue += mStep;
    }

    public void decreaseValue() {
        mValue -= mStep;
    }

    private int getRandomColor() {
        RandomColor randomColor = new RandomColor();
        int color = randomColor.randomColor();
        return color;
    }

    public int getTextColor() {
        return mTextColor;
    }

    private int defineTextColor() {
        int r = Color.red(mBackgroundColor);
        int g = Color.green(mBackgroundColor);
        int b = Color.blue(mBackgroundColor);
        int o = ((r * 299) + (g * 587) + (b * 114)) / 1000;
        return o > 125 ? Color.BLACK : Color.WHITE;
    }

    public void setTextColor(int textColor) {
        this.mTextColor = textColor;
    }

}
