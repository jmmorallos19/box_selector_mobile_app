package com.boxselector.app;

import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;

/**
 * Simple sound effects using Android's ToneGenerator.
 * No MP3 files are needed, so this is easy to explain in defense.
 */
public class SoundPlayer {

    public static void playClick(Context context) {
        play(context, ToneGenerator.TONE_PROP_BEEP, 80);
    }

    public static void playCorrect(Context context) {
        play(context, ToneGenerator.TONE_PROP_ACK, 180);
    }

    public static void playWrong(Context context) {
        play(context, ToneGenerator.TONE_PROP_NACK, 220);
    }

    public static void playWin(Context context) {
        play(context, ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 300);
    }

    public static void playGameOver(Context context) {
        play(context, ToneGenerator.TONE_CDMA_ABBR_REORDER, 400);
    }

    private static void play(Context context, int tone, int durationMs) {
        if (context == null || !GamePrefs.isSoundOn(context)) {
            return;
        }
        try {
            ToneGenerator generator = new ToneGenerator(AudioManager.STREAM_MUSIC, 80);
            generator.startTone(tone, durationMs);
        } catch (Exception ignored) {
            // Some devices block ToneGenerator. The game should still run.
        }
    }
}
