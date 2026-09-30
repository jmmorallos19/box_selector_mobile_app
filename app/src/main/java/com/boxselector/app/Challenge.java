package com.boxselector.app;

/**
 * One custom challenge created by a player.
 * Difficulty must be one of the values in difficulties().
 */
public class Challenge {

    public static final String EASY = "Easy";
    public static final String MEDIUM = "Medium";
    public static final String HARD = "Hard";
    public static final String EXPERT = "Expert";
    public static final String EXTREME = "Extreme";
    public static final String INSANE = "Insane";
    public static final String MASTER = "Master";

    public int id;
    public String title;
    public String description;
    public String difficulty;
    public String createdBy;

    public Challenge() {
    }

    public Challenge(int id, String title, String description, String difficulty, String createdBy) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.difficulty = difficulty;
        this.createdBy = createdBy;
    }

    public static String[] difficulties() {
        return new String[]{EASY, MEDIUM, HARD, EXPERT, EXTREME, INSANE, MASTER};
    }

    /** Returns true only for the allowed challenge difficulties. */
    public static boolean isValidDifficulty(String value) {
        if (value == null) {
            return false;
        }
        String[] options = difficulties();
        for (int i = 0; i < options.length; i++) {
            if (options[i].equals(value)) {
                return true;
            }
        }
        return false;
    }

    /** Maps challenge difficulty to gameplay settings. */
    public String toGameplayDifficulty() {
        return difficulty;
    }
}
