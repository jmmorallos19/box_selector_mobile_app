package com.boxselector.app;

/**
 * Settings for each difficulty.
 * Later names are faster, give fewer lives, and award more points.
 */
public class DifficultyConfig {

    public static final String BEGINNER = "Beginner";
    public static final String EASY = "Easy";
    public static final String MEDIUM = "Medium";
    public static final String NORMAL = "Normal";
    public static final String HARD = "Hard";
    public static final String EXPERT = "Expert";
    public static final String EXTREME = "Extreme";
    public static final String INSANE = "Insane";
    public static final String MASTER = "Master";
    public static final int STAGE_COUNT = 15;

    public static String[] allNames() {
        return new String[]{
                BEGINNER, EASY, MEDIUM, NORMAL, HARD, EXPERT, EXTREME, INSANE, MASTER
        };
    }

    public static int getStartLives(String difficulty) {
        if (BEGINNER.equals(difficulty)) {
            return 6;
        } else if (EASY.equals(difficulty)) {
            return 5;
        } else if (MEDIUM.equals(difficulty) || NORMAL.equals(difficulty)) {
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
        if (BEGINNER.equals(difficulty)) {
            base = 55000;
        } else if (EASY.equals(difficulty)) {
            base = 50000;
        } else if (MEDIUM.equals(difficulty) || NORMAL.equals(difficulty)) {
            base = 45000;
        } else if (HARD.equals(difficulty)) {
            base = 35000;
        } else if (EXPERT.equals(difficulty)) {
            base = 30000;
        } else if (EXTREME.equals(difficulty)) {
            base = 25000;
        } else if (INSANE.equals(difficulty)) {
            base = 20000;
        } else {
            base = 15000;
        }
        int reduced = base - ((stage - 1) * 1000);
        int minTime = MASTER.equals(difficulty) || INSANE.equals(difficulty) ? 10000 : 15000;
        if (reduced < minTime) {
            reduced = minTime;
        }
        return reduced;
    }

    public static int getMemorizeMs(String difficulty) {
        if (BEGINNER.equals(difficulty)) {
            return 5000;
        } else if (EASY.equals(difficulty) || MEDIUM.equals(difficulty) || NORMAL.equals(difficulty)) {
            return 4000;
        } else if (HARD.equals(difficulty)) {
            return 3000;
        } else if (EXPERT.equals(difficulty) || EXTREME.equals(difficulty)) {
            return 2000;
        } else {
            return 1000;
        }
    }

    public static int getTargetHits(String difficulty, int stage) {
        int base;
        if (BEGINNER.equals(difficulty) || EASY.equals(difficulty)) {
            base = 2;
        } else if (MEDIUM.equals(difficulty) || NORMAL.equals(difficulty) || HARD.equals(difficulty)) {
            base = 3;
        } else if (EXPERT.equals(difficulty) || EXTREME.equals(difficulty)) {
            base = 4;
        } else {
            base = 5;
        }
        if (stage >= 11) {
            base = base + 1;
        }
        return base;
    }

    public static int getCoinReward(String difficulty) {
        if (BEGINNER.equals(difficulty)) {
            return 80;
        } else if (EASY.equals(difficulty)) {
            return 100;
        } else if (MEDIUM.equals(difficulty) || NORMAL.equals(difficulty)) {
            return 150;
        } else if (HARD.equals(difficulty)) {
            return 200;
        } else if (EXPERT.equals(difficulty)) {
            return 250;
        } else if (EXTREME.equals(difficulty)) {
            return 300;
        } else if (INSANE.equals(difficulty)) {
            return 350;
        } else {
            return 400;
        }
    }

    public static int getScoreBonus(String difficulty) {
        if (BEGINNER.equals(difficulty)) {
            return 30;
        } else if (EASY.equals(difficulty)) {
            return 50;
        } else if (MEDIUM.equals(difficulty) || NORMAL.equals(difficulty)) {
            return 100;
        } else if (HARD.equals(difficulty)) {
            return 200;
        } else if (EXPERT.equals(difficulty)) {
            return 350;
        } else if (EXTREME.equals(difficulty)) {
            return 500;
        } else if (INSANE.equals(difficulty)) {
            return 700;
        } else {
            return 1000;
        }
    }
}
