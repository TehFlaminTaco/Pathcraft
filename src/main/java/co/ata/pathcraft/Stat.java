package co.ata.pathcraft;

import java.util.HashMap;

public enum Stat {
    STRENGTH,
    DEXTERITY,
    CONSTITUTION,
    INTELLIGENCE,
    WISDOM,
    CHARISMA;

    private static int[] Costs = { 0, 0, 0, 0, 0, 0, 0, -4, -2, -1, 0, 1, 2, 3, 5, 7, 10, 13, 17 };

    public static int TotalPoints(HashMap<Stat, Integer> stats) {
        int total = 25;
        for (Stat s : Stat.values()) {
            if (stats.get(s) == null)
                return -1;
            int v = stats.get(s);
            if (v < 7 || v > 18)
                return -1;
            total -= Costs[v];
        }
        return total;
    }

    public static boolean CanBuy(HashMap<Stat, Integer> stats, Stat target, int newValue) {
        if (newValue < 7 || newValue > 18)
            return false;
        HashMap<Stat, Integer> newStats = new HashMap<Stat, Integer>(stats);
        newStats.put(target, newValue);
        int total = TotalPoints(newStats);
        if (total < 0)
            return false;
        return true;
    }
}
