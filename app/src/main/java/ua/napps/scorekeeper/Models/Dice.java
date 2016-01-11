package ua.napps.scorekeeper.Models;

import java.security.SecureRandom;

public final class Dice {
    private static final SecureRandom rnd = new SecureRandom();
    private int amount;
    private int minEdge;
    private int maxEdge;
    private int bonus;

    private static Dice sDice;

    public synchronized static Dice getDice() {
        if (sDice == null) {
            sDice = new Dice();
        }
        return sDice;
    }

    private Dice() {
        amount = 1;
        minEdge = 1;
        maxEdge = 6;
        bonus = 10;
    }

    public int roll() {
        int sum = 0;
        for (int i = 0; i < amount; i++) {
            sum += minEdge + rnd.nextInt(maxEdge - minEdge + 1);
        }
        return sum + bonus;
    }

    @Override
    public String toString() {
        String bonusStr = (bonus > 0) ? "+" + bonus : (bonus < 0) ? "" + bonus : "";
        return amount + "d" + (1 + maxEdge - minEdge) + bonusStr;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void setMinEdge(int minEdge) {
        this.minEdge = minEdge;
    }

    public void setMaxEdge(int maxEdge) {
        this.maxEdge = maxEdge;
    }

    public void setBonus(int bonus) {
        this.bonus = bonus;
    }

    public int getAmount() {
        return amount;
    }

    public int getMinEdge() {
        return minEdge;
    }

    public int getMaxEdge() {
        return maxEdge;
    }

    public int getBonus() {
        return bonus;
    }
}
