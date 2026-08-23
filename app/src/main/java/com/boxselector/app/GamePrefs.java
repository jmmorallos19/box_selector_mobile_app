package com.boxselector.app;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Simple helper for saving coins and power-up counts.
 * Students can reuse these methods in any Activity.
 */
public class GamePrefs {

    private static final String PREFS_NAME = "box_selector_prefs";
    private static final String KEY_COINS = "coins";
    private static final String KEY_HINTS = "hints";
    private static final String KEY_EXTRA_TIME = "extra_time";
    private static final String KEY_BOX_REVEAL = "box_reveal";
    private static final String KEY_HIGHEST_LEVEL = "highest_level";
    private static final int STARTING_COINS = 200;

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static int getCoins(Context context) {
        return getPrefs(context).getInt(KEY_COINS, STARTING_COINS);
    }

    public static void setCoins(Context context, int coins) {
        getPrefs(context).edit().putInt(KEY_COINS, coins).apply();
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
        return getPrefs(context).getInt(KEY_HINTS, 0);
    }

    public static void addHint(Context context) {
        getPrefs(context).edit().putInt(KEY_HINTS, getHints(context) + 1).apply();
    }

    public static boolean useHint(Context context) {
        int count = getHints(context);
        if (count <= 0) {
            return false;
        }
        getPrefs(context).edit().putInt(KEY_HINTS, count - 1).apply();
        return true;
    }

    public static int getExtraTime(Context context) {
        return getPrefs(context).getInt(KEY_EXTRA_TIME, 0);
    }

    public static void addExtraTime(Context context) {
        getPrefs(context).edit().putInt(KEY_EXTRA_TIME, getExtraTime(context) + 1).apply();
    }

    public static boolean useExtraTime(Context context) {
        int count = getExtraTime(context);
        if (count <= 0) {
            return false;
        }
        getPrefs(context).edit().putInt(KEY_EXTRA_TIME, count - 1).apply();
        return true;
    }

    public static int getBoxReveal(Context context) {
        return getPrefs(context).getInt(KEY_BOX_REVEAL, 0);
    }

    public static void addBoxReveal(Context context) {
        getPrefs(context).edit().putInt(KEY_BOX_REVEAL, getBoxReveal(context) + 1).apply();
    }

    public static boolean useBoxReveal(Context context) {
        int count = getBoxReveal(context);
        if (count <= 0) {
            return false;
        }
        getPrefs(context).edit().putInt(KEY_BOX_REVEAL, count - 1).apply();
        return true;
    }

    public static int getHighestLevel(Context context) {
        return getPrefs(context).getInt(KEY_HIGHEST_LEVEL, 1);
    }

    public static void unlockLevel(Context context, int level) {
        if (level > getHighestLevel(context)) {
            getPrefs(context).edit().putInt(KEY_HIGHEST_LEVEL, level).apply();
        }
    }
}
