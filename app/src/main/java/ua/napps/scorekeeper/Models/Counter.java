package ua.napps.scorekeeper.Models;

import static ua.napps.scorekeeper.Utils.ColorUtil.getContrastColor;
import static ua.napps.scorekeeper.Utils.ColorUtil.getRandomColor;

public final class Counter {
    private String caption = "Counter";
    private int mValue;
    private int mBackgroundColor;
    private int mTextColor;
    private int mDefaultValue;
    private int mStep;

    public Counter(String name) {
        caption = name;
        mBackgroundColor = getRandomColor();
        mTextColor = getContrastColor(mBackgroundColor);
        mValue = 0;
        mDefaultValue = 0;
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

    public int getTextColor() {
        return mTextColor;
    }

    public void setTextColor(int textColor) {
        this.mTextColor = textColor;
    }

}
