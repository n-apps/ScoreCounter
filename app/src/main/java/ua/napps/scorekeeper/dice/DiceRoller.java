package ua.napps.scorekeeper.dice;

import java.util.ArrayList;

 class DiceRoller {
    public static ArrayList<Integer> rollDice(int sides, int count) {
        ArrayList<Integer> rolls = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            rolls.add((int) (Math.random() * sides) + 1);
        }
        return rolls;
    }
}