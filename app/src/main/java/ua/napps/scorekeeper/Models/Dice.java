package ua.napps.scorekeeper.Models;

import java.security.SecureRandom;

public final class Dice {
    private static final SecureRandom rnd = new SecureRandom();
    private int mDiceNumber;
    private int mMinSide;
    private int mMaxSide;
    private int mTotalBonus;

    private static Dice sDice;

    public synchronized static Dice getDice() {
        if (sDice == null) {
            sDice = new Dice();
        }
        return sDice;
    }

    private Dice() {
        mDiceNumber = 1;
        mMinSide = 1;
        mMaxSide = 6;
        mTotalBonus = 10;
    }

    public int roll() {
        int sum = 0;
        for (int i = 0; i < mDiceNumber; i++) {
            sum += mMinSide + rnd.nextInt(mMaxSide - mMinSide + 1);
        }
        return sum + mTotalBonus;
    }

    @Override
    public String toString() {
        String bonusStr = (mTotalBonus > 0) ? "+" + mTotalBonus : (mTotalBonus < 0) ? "" + mTotalBonus : "";
        return mDiceNumber + "d" + (1 + mMaxSide - mMinSide) + bonusStr;
    }

    public void setDiceNumber(int diceNumber) {
        this.mDiceNumber = diceNumber;
    }

    public void setMinSide(int minSide) {
        this.mMinSide = minSide;
    }

    public void setMaxSide(int maxSide) {
        this.mMaxSide = maxSide;
    }

    public void setTotalBonus(int totalBonus) {
        this.mTotalBonus = totalBonus;
    }

    public int getDiceNumber() {
        return mDiceNumber;
    }

    public int getMinSide() {
        return mMinSide;
    }

    public int getMaxSide() {
        return mMaxSide;
    }

    public int getTotalBonus() {
        return mTotalBonus;
    }
}
