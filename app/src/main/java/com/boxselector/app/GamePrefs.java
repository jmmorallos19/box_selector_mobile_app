package com.boxselector.app;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Saves account data, coins, stages, and the leaderboard.
 * Each username has its own coins and unlocked stages.
 */
public class GamePrefs {

    private static final String PREFS_NAME = "box_selector_prefs";
    private static final String KEY_CURRENT_USER = "current_user";
    private static final String KEY_USER_LIST = "user_list";
    private static final String KEY_SOUND = "sound_on";
    private static final String KEY_MUSIC = "music_on";
    private static final String KEY_LEADERBOARD = "leaderboard";
    private static final String KEY_CHALLENGES = "challenges";
    private static final String KEY_NEXT_CHALLENGE_ID = "next_challenge_id";
    private static final int STARTING_COINS = 200;

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    private static String userKey(Context context, String key) {
        return getUsername(context) + "_" + key;
    }

    // ---------- Account ----------

    public static boolean isLoggedIn(Context context) {
        String name = getUsername(context);
        return name != null && !name.isEmpty();
    }

    public static String getUsername(Context context) {
        return getPrefs(context).getString(KEY_CURRENT_USER, "");
    }

    public static boolean userExists(Context context, String username) {
        return getPrefs(context).contains("pass_" + username.toLowerCase());
    }

    public static boolean createAccount(Context context, String username, String password) {
        String cleanName = username.trim();
        if (cleanName.isEmpty() || password.isEmpty()) {
            return false;
        }
        if (userExists(context, cleanName)) {
            return false;
        }

        SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putString("pass_" + cleanName.toLowerCase(), password);
        editor.putString(KEY_CURRENT_USER, cleanName);
        editor.putInt(cleanName + "_coins", STARTING_COINS);
        editor.putInt(cleanName + "_hints", 1);

        String list = getPrefs(context).getString(KEY_USER_LIST, "");
        if (list.isEmpty()) {
            list = cleanName;
        } else {
            list = list + "," + cleanName;
        }
        editor.putString(KEY_USER_LIST, list);
        editor.apply();
        return true;
    }

    public static boolean login(Context context, String username, String password) {
        String cleanName = username.trim();
        String saved = getPrefs(context).getString("pass_" + cleanName.toLowerCase(), null);
        if (saved == null || !saved.equals(password)) {
            return false;
        }
        getPrefs(context).edit().putString(KEY_CURRENT_USER, cleanName).apply();
        return true;
    }

    public static void logout(Context context) {
        getPrefs(context).edit().remove(KEY_CURRENT_USER).apply();
    }

    // ---------- Coins and power-ups (per account) ----------

    public static int getCoins(Context context) {
        return getPrefs(context).getInt(userKey(context, "coins"), STARTING_COINS);
    }

    public static void setCoins(Context context, int coins) {
        getPrefs(context).edit().putInt(userKey(context, "coins"), coins).apply();
    }

    public static void addCoins(Context context, int amount) {
        setCoins(context, getCoins(context) + amount);
    }

    public static boolean spendCoins(Context context, int cost) {
        int coins = getCoins(context);
        if (coins < cost) {
            return false;
        }
        setCoins(context, coins - cost);
        return true;
    }

    public static int getHints(Context context) {
        return getPrefs(context).getInt(userKey(context, "hints"), 1);
    }

    public static void addHint(Context context) {
        getPrefs(context).edit().putInt(userKey(context, "hints"), getHints(context) + 1).apply();
    }

    public static boolean useHint(Context context) {
        int count = getHints(context);
        if (count <= 0) {
            return false;
        }
        getPrefs(context).edit().putInt(userKey(context, "hints"), count - 1).apply();
        return true;
    }

    public static int getExtraTime(Context context) {
        return getPrefs(context).getInt(userKey(context, "extra_time"), 0);
    }

    public static void addExtraTime(Context context) {
        getPrefs(context).edit().putInt(userKey(context, "extra_time"), getExtraTime(context) + 1).apply();
    }

    public static boolean useExtraTime(Context context) {
        int count = getExtraTime(context);
        if (count <= 0) {
            return false;
        }
        getPrefs(context).edit().putInt(userKey(context, "extra_time"), count - 1).apply();
        return true;
    }

    public static int getBoxReveal(Context context) {
        return getPrefs(context).getInt(userKey(context, "box_reveal"), 0);
    }

    public static void addBoxReveal(Context context) {
        getPrefs(context).edit().putInt(userKey(context, "box_reveal"), getBoxReveal(context) + 1).apply();
    }

    public static boolean useBoxReveal(Context context) {
        int count = getBoxReveal(context);
        if (count <= 0) {
            return false;
        }
        getPrefs(context).edit().putInt(userKey(context, "box_reveal"), count - 1).apply();
        return true;
    }

    // ---------- Stages per difficulty ----------

    public static int getHighestStage(Context context, String difficulty) {
        return getPrefs(context).getInt(userKey(context, "stage_" + difficulty), 1);
    }

    public static void unlockStage(Context context, String difficulty, int stage) {
        if (stage > DifficultyConfig.STAGE_COUNT) {
            stage = DifficultyConfig.STAGE_COUNT;
        }
        if (stage > getHighestStage(context, difficulty)) {
            getPrefs(context).edit().putInt(userKey(context, "stage_" + difficulty), stage).apply();
        }
    }

    public static int getHighestLevel(Context context) {
        return getHighestStage(context, DifficultyConfig.NORMAL);
    }

    public static void unlockLevel(Context context, int level) {
        unlockStage(context, DifficultyConfig.NORMAL, level);
    }

    // ---------- Score and leaderboard ----------

    public static int getBestScore(Context context) {
        return getPrefs(context).getInt(userKey(context, "best_score"), 0);
    }

    public static void saveBestScore(Context context, int score) {
        if (score > getBestScore(context)) {
            getPrefs(context).edit().putInt(userKey(context, "best_score"), score).apply();
        }
    }

    public static void addLeaderboardEntry(Context context, String username, int score, String difficulty) {
        if (username == null || username.isEmpty() || score <= 0) {
            return;
        }

        List<LeaderboardEntry> entries = getLeaderboard(context);
        boolean updated = false;
        for (LeaderboardEntry entry : entries) {
            if (entry.username.equalsIgnoreCase(username) && entry.difficulty.equals(difficulty)) {
                if (score > entry.score) {
                    entry.score = score;
                }
                updated = true;
                break;
            }
        }
        if (!updated) {
            entries.add(new LeaderboardEntry(username, score, difficulty));
        }

        Collections.sort(entries, new Comparator<LeaderboardEntry>() {
            @Override
            public int compare(LeaderboardEntry a, LeaderboardEntry b) {
                return b.score - a.score;
            }
        });

        if (entries.size() > 10) {
            entries = entries.subList(0, 10);
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < entries.size(); i++) {
            LeaderboardEntry entry = entries.get(i);
            if (i > 0) {
                builder.append(";");
            }
            builder.append(entry.username).append("|").append(entry.score).append("|").append(entry.difficulty);
        }
        getPrefs(context).edit().putString(KEY_LEADERBOARD, builder.toString()).apply();
        saveBestScore(context, score);
    }

    public static List<LeaderboardEntry> getLeaderboard(Context context) {
        List<LeaderboardEntry> entries = new ArrayList<>();
        String raw = getPrefs(context).getString(KEY_LEADERBOARD, "");
        if (raw.isEmpty()) {
            return entries;
        }

        String[] rows = raw.split(";");
        for (String row : rows) {
            String[] parts = row.split("\\|");
            if (parts.length >= 3) {
                try {
                    entries.add(new LeaderboardEntry(parts[0], Integer.parseInt(parts[1]), parts[2]));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return entries;
    }

    public static boolean isSoundOn(Context context) {
        return getPrefs(context).getBoolean(KEY_SOUND, true);
    }

    public static void setSoundOn(Context context, boolean on) {
        getPrefs(context).edit().putBoolean(KEY_SOUND, on).apply();
    }

    public static boolean isMusicOn(Context context) {
        return getPrefs(context).getBoolean(KEY_MUSIC, true);
    }

    public static void setMusicOn(Context context, boolean on) {
        getPrefs(context).edit().putBoolean(KEY_MUSIC, on).apply();
    }

    // ---------- Challenges (local database via SharedPreferences) ----------

    /** Returns all saved challenges. This is the app's challenge list API. */
    public static List<Challenge> getChallenges(Context context) {
        List<Challenge> challenges = new ArrayList<>();
        String raw = getPrefs(context).getString(KEY_CHALLENGES, "");
        if (raw.isEmpty()) {
            return challenges;
        }

        String[] rows = raw.split("\\|\\|\\|");
        for (String row : rows) {
            Challenge challenge = parseChallenge(row);
            if (challenge != null) {
                challenges.add(challenge);
            }
        }
        return challenges;
    }

    public static Challenge getChallengeById(Context context, int id) {
        List<Challenge> challenges = getChallenges(context);
        for (int i = 0; i < challenges.size(); i++) {
            if (challenges.get(i).id == id) {
                return challenges.get(i);
            }
        }
        return null;
    }

    /**
     * Creates or updates a challenge after validating difficulty.
     * Returns the saved challenge, or null if validation failed.
     */
    public static Challenge saveChallenge(Context context, Challenge challenge) {
        if (challenge == null || challenge.title == null || challenge.title.trim().isEmpty()) {
            return null;
        }
        if (!Challenge.isValidDifficulty(challenge.difficulty)) {
            return null;
        }

        challenge.title = challenge.title.trim();
        if (challenge.description == null) {
            challenge.description = "";
        } else {
            challenge.description = challenge.description.trim();
        }

        List<Challenge> challenges = getChallenges(context);
        if (challenge.id <= 0) {
            int nextId = getPrefs(context).getInt(KEY_NEXT_CHALLENGE_ID, 1);
            challenge.id = nextId;
            getPrefs(context).edit().putInt(KEY_NEXT_CHALLENGE_ID, nextId + 1).apply();
            challenges.add(challenge);
        } else {
            boolean found = false;
            for (int i = 0; i < challenges.size(); i++) {
                if (challenges.get(i).id == challenge.id) {
                    challenges.set(i, challenge);
                    found = true;
                    break;
                }
            }
            if (!found) {
                challenges.add(challenge);
            }
        }

        writeChallenges(context, challenges);
        return challenge;
    }

    public static boolean deleteChallenge(Context context, int id) {
        List<Challenge> challenges = getChallenges(context);
        boolean removed = false;
        for (int i = 0; i < challenges.size(); i++) {
            if (challenges.get(i).id == id) {
                challenges.remove(i);
                removed = true;
                break;
            }
        }
        if (removed) {
            writeChallenges(context, challenges);
        }
        return removed;
    }

    private static void writeChallenges(Context context, List<Challenge> challenges) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < challenges.size(); i++) {
            if (i > 0) {
                builder.append("|||");
            }
            builder.append(encodeChallenge(challenges.get(i)));
        }
        getPrefs(context).edit().putString(KEY_CHALLENGES, builder.toString()).apply();
    }

    private static String encodeChallenge(Challenge challenge) {
        return challenge.id + "::"
                + safeText(challenge.title) + "::"
                + safeText(challenge.description) + "::"
                + challenge.difficulty + "::"
                + safeText(challenge.createdBy);
    }

    private static Challenge parseChallenge(String row) {
        String[] parts = row.split("::", -1);
        if (parts.length < 5) {
            return null;
        }
        try {
            String difficulty = parts[3];
            if (!Challenge.isValidDifficulty(difficulty)) {
                return null;
            }
            return new Challenge(
                    Integer.parseInt(parts[0]),
                    parts[1],
                    parts[2],
                    difficulty,
                    parts[4]
            );
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static String safeText(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("::", " ").replace("|||", " ").replace("\n", " ");
    }

    /** One row on the leaderboard. */
    public static class LeaderboardEntry {
        public String username;
        public int score;
        public String difficulty;

        public LeaderboardEntry(String username, int score, String difficulty) {
            this.username = username;
            this.score = score;
            this.difficulty = difficulty;
        }
    }
}
