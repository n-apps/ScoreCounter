package ua.napps.scorekeeper.Models;

import android.graphics.Color;

import com.github.lzyzsd.randomcolor.RandomColor;

public final class Counter {
    private String caption = "Counter";
    private int value = 0;
    private int color;
    private int textColor;
    private int defValue = 0;
    private int minValue = -999;
    private int maxValue = 1000;
    private int step = 1;

    public Counter(String caption) {
        this.caption = caption;
        this.color = getRandomColor();
        this.textColor = defineTextColor();
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getCaption() {
        return caption;
    }

    public int getValue() {
        return value;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }

    public int getDefValue() {
        return defValue;
    }

    public void setDefValue(int defValue) {
        this.defValue = defValue;
    }

    public int getMinValue() {
        return minValue;
    }

    public void setMinValue(int minValue) {
        this.minValue = minValue;
        if (value >= minValue) return;
        value = minValue;
    }

    public int getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(int maxValue) {
        this.maxValue = maxValue;
        if (value <= maxValue) return;
        value = maxValue;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public void increaseValue() {
        value += step;
    }

    public void decreaseValue() {
        value -= step;
    }

    private int getRandomColor() {
        RandomColor randomColor = new RandomColor();
        int color = randomColor.randomColor();
        return color;
    }

    public int getTextColor() {
        return textColor;
    }

    private int defineTextColor() {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        int o = ((r * 299) + (g * 587) + (b * 114)) / 1000;
        return o > 125 ? Color.BLACK : Color.WHITE;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }
}
