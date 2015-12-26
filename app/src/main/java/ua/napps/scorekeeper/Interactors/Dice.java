package ua.napps.scorekeeper.Interactors;

import java.security.SecureRandom;

public class Dice {
    private static final SecureRandom rnd = new SecureRandom();
    int amount;
    int minEdge;
    int maxEdge;
    int bonus;

    private static Dice instance;

    public static Dice getInstance() {
        if (instance == null) {
            instance = new Dice();
        }
        return instance;
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
