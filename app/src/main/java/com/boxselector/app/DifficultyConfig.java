package com.boxselector.app;

/**
 * Settings for each difficulty.
 * Easy is slower and gives more lives. Extreme is faster and harder.
 */
public class DifficultyConfig {

    public static final String EASY = "Easy";
    public static final String NORMAL = "Normal";
    public static final String HARD = "Hard";
    public static final String EXPERT = "Expert";
    public static final String EXTREME = "Extreme";
    public static final int STAGE_COUNT = 15;

    public static String[] allNames() {
        return new String[]{EASY, NORMAL, HARD, EXPERT, EXTREME};
    }

    public static int getStartLives(String difficulty) {
        if (EASY.equals(difficulty)) {
            return 5;
        } else if (NORMAL.equals(difficulty)) {
            return 4;
        } else if (HARD.equals(difficulty)) {
            return 3;
        } else if (EXPERT.equals(difficulty)) {
            return 2;
        } else {
            return 1;
        }
    }

    public static int getTimeMs(String difficulty, int stage) {
        int base;
        if (EASY.equals(difficulty)) {
            base = 50000;
        } else if (NORMAL.equals(difficulty)) {
            base = 45000;
        } else if (HARD.equals(difficulty)) {
            base = 35000;
        } else if (EXPERT.equals(difficulty)) {
            base = 30000;
        } else {
            base = 25000;
        }
        // Later stages get a little less time
        int reduced = base - ((stage - 1) * 1000);
        if (reduced < 15000) {
            reduced = 15000;
        }
        return reduced;
    }

    public static int getMemorizeMs(String difficulty) {
        if (EASY.equals(difficulty) || NORMAL.equals(difficulty)) {
            return 4000;
        } else if (HARD.equals(difficulty)) {
            return 3000;
        } else {
            return 2000;
        }
    }

    public static int getTargetHits(String difficulty, int stage) {
        int base;
        if (EASY.equals(difficulty)) {
            base = 2;
        } else if (NORMAL.equals(difficulty) || HARD.equals(difficulty)) {
            base = 3;
        } else {
            base = 4;
        }
        if (stage >= 11) {
            base = base + 1;
        }
        return base;
    }

    public static int getCoinReward(String difficulty) {
        if (EASY.equals(difficulty)) {
            return 100;
        } else if (NORMAL.equals(difficulty)) {
            return 150;
        } else if (HARD.equals(difficulty)) {
            return 200;
        } else if (EXPERT.equals(difficulty)) {
            return 250;
        } else {
            return 300;
        }
    }

    public static int getScoreBonus(String difficulty) {
        if (EASY.equals(difficulty)) {
            return 50;
        } else if (NORMAL.equals(difficulty)) {
            return 100;
        } else if (HARD.equals(difficulty)) {
            return 200;
        } else if (EXPERT.equals(difficulty)) {
            return 350;
        } else {
            return 500;
        }
    }
}
