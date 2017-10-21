package ua.napps.scorekeeper.counters;

public class OldCounter {

    private int mBackgroundColor;

    private String mCaption = "Counter";

    private int mDefaultValue;

    private int mRotationValue;

    private int mStep;

    private int mTextColor;

    private int mValue;

    public OldCounter() {
    }

    public void decreaseValue() {
        mValue -= mStep;
    }

    public String getCaption() {
        return mCaption;
    }

    public void setCaption(String caption) {
        this.mCaption = caption;
    }

    public int getColor() {
        return mBackgroundColor;
    }

    public void setColor(int color) {
        this.mBackgroundColor = color;
    }

    public int getDefValue() {
        return mDefaultValue;
    }

    public void setDefValue(int defValue) {
        this.mDefaultValue = defValue;
    }

    public int getRotationValue() {
        return mRotationValue;
    }

    public void setRotationValue(int rotationValue) {
        mRotationValue = rotationValue;
    }

    public int getStep() {
        return mStep;
    }

    public void setStep(int step) {
        this.mStep = step;
    }

    public int getTextColor() {
        return mTextColor;
    }

    public void setTextColor(int textColor) {
        this.mTextColor = textColor;
    }

    public int getValue() {
        return mValue;
    }

    public void setValue(int value) {
        this.mValue = value;
    }

    public void increaseValue() {
        mValue += mStep;
    }
}