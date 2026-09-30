package com.boxselector.app;

/**
 * Eight difficulties for Box Selector.
 * Harder modes mean more boxes to remember, a shorter look, and fewer lives.
 */
public class DifficultyConfig {

    public static final String EASY = "Easy";
    public static final String NORMAL = "Normal";
    public static final String HARD = "Hard";
    public static final String EXPERT = "Expert";
    public static final String EXTREME = "Extreme";
    public static final String MASTER = "Master";
    public static final String NIGHTMARE = "Nightmare";
    public static final String IMPOSSIBLE = "Impossible";
    public static final int STAGE_COUNT = 15;
    public static final int HINT_COST = 25;
    public static final int EXTRA_TIME_SECONDS = 5;

    public static String[] allNames() {
        return new String[]{
                EASY, NORMAL, HARD, EXPERT, EXTREME, MASTER, NIGHTMARE, IMPOSSIBLE
        };
    }

    public static int getIndex(String difficulty) {
        String[] names = allNames();
        for (int i = 0; i < names.length; i++) {
            if (names[i].equals(difficulty)) {
                return i;
            }
        }
        return 1;
    }

    public static String getPrevious(String difficulty) {
        int index = getIndex(difficulty);
        if (index <= 0) {
            return null;
        }
        return allNames()[index - 1];
    }

    public static int getLifeLimit(String difficulty) {
        if (EASY.equals(difficulty)) {
            return 4;
        } else if (NORMAL.equals(difficulty)) {
            return 3;
        } else if (HARD.equals(difficulty) || EXPERT.equals(difficulty)) {
            return 2;
        } else {
            return 1;
        }
    }

    /**
     * How many green boxes the player must remember at once.
     * Higher difficulty = more boxes. Later stages add 1 more, max 8 of 9.
     */
    public static int getBoxCount(String difficulty, int stage) {
        int boxes = getIndex(difficulty) + 1;
        if (stage >= 11) {
            boxes = boxes + 1;
        }
        if (boxes > 8) {
            boxes = 8;
        }
        return boxes;
    }

    /** How many correct rounds are needed to clear the stage. */
    public static int getRoundsToClear(String difficulty, int stage) {
        if (EASY.equals(difficulty)) {
            return 2 + (stage / 5);
        } else if (NORMAL.equals(difficulty)) {
            return 3 + (stage / 5);
        } else if (HARD.equals(difficulty)) {
            return 3 + (stage / 4);
        } else if (EXPERT.equals(difficulty)) {
            return 4 + (stage / 4);
        } else if (EXTREME.equals(difficulty)) {
            return 4 + (stage / 3);
        } else if (MASTER.equals(difficulty)) {
            return 5 + (stage / 3);
        } else if (NIGHTMARE.equals(difficulty)) {
            return 5 + (stage / 2);
        } else {
            return 6 + (stage / 2);
        }
    }

    public static int getMemorizeMs(String difficulty) {
        if (EASY.equals(difficulty)) {
            return 4000;
        } else if (NORMAL.equals(difficulty)) {
            return 3500;
        } else if (HARD.equals(difficulty)) {
            return 3000;
        } else if (EXPERT.equals(difficulty)) {
            return 2200;
        } else if (EXTREME.equals(difficulty)) {
            return 1800;
        } else if (MASTER.equals(difficulty)) {
            return 1200;
        } else if (NIGHTMARE.equals(difficulty)) {
            return 1000;
        } else {
            return 800;
        }
    }

    /**
     * 0 = boxes stay still while you look.
     * 1 = they jump once.
     * 2 = they keep jumping.
     */
    public static int getMoveStyle(String difficulty) {
        if (EASY.equals(difficulty) || NORMAL.equals(difficulty) || HARD.equals(difficulty)) {
            return 0;
        } else if (EXPERT.equals(difficulty) || EXTREME.equals(difficulty)) {
            return 1;
        } else {
            return 2;
        }
    }

    public static int getTimerSeconds(String difficulty, int stage) {
        int seconds;
        if (EASY.equals(difficulty)) {
            seconds = 30 - (stage / 3);
        } else if (NORMAL.equals(difficulty)) {
            seconds = 26 - (stage / 3);
        } else if (HARD.equals(difficulty)) {
            seconds = 22 - (stage / 3);
        } else if (EXPERT.equals(difficulty)) {
            seconds = 18 - (stage / 3);
        } else if (EXTREME.equals(difficulty)) {
            seconds = 15 - (stage / 3);
        } else if (MASTER.equals(difficulty)) {
            seconds = 12 - (stage / 3);
        } else if (NIGHTMARE.equals(difficulty)) {
            seconds = 10 - (stage / 3);
        } else {
            seconds = 8 - (stage / 3);
        }
        int minTime = 6 + (7 - getIndex(difficulty));
        if (minTime < 6) {
            minTime = 6;
        }
        if (seconds < minTime) {
            seconds = minTime;
        }
        return seconds;
    }

    public static int getTimeMs(String difficulty, int stage) {
        return getTimerSeconds(difficulty, stage) * 1000;
    }

    public static int getClearScore(String difficulty, int stage, int correctHits, int secondsLeft) {
        int inner = (correctHits * 10) + (secondsLeft * 2) + (stage * 5);
        return inner * (getIndex(difficulty) + 1);
    }

    public static int getClearCoins(String difficulty, int stage, int secondsLeft) {
        return 8 + (getIndex(difficulty) * 4) + (stage / 3) + (secondsLeft / 5);
    }

    public static String getMoveLabel(String difficulty) {
        int style = getMoveStyle(difficulty);
        if (style == 0) {
            return "boxes stay still";
        } else if (style == 1) {
            return "boxes jump once";
        } else {
            return "boxes keep moving";
        }
    }

    public static String getButtonDescription(String difficulty) {
        int lives = getLifeLimit(difficulty);
        int look = getMemorizeMs(difficulty) / 1000;
        int boxes = getBoxCount(difficulty, 1);
        int lateBoxes = getBoxCount(difficulty, 15);
        String boxWord;
        if (boxes == lateBoxes) {
            boxWord = boxes == 1 ? "1 box" : boxes + " boxes";
        } else {
            boxWord = boxes + "–" + lateBoxes + " boxes";
        }
        return lives + " lives · " + look + "s look · " + boxWord + ", " + getMoveLabel(difficulty);
    }
}
