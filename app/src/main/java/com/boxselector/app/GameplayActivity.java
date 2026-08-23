package com.boxselector.app;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Main game screen of Box Selector.
 *
 * Game flow (easy to explain in defense):
 * 1. Memorize Phase - show the correct box for 3 seconds. Clicks are off.
 * 2. Cover Phase    - hide all boxes, then start/resume the 45s timer.
 * 3. Tap Phase      - player picks a box. Correct = coins + progress.
 * 4. Win            - when the player finds the target number of boxes.
 */
public class GameplayActivity extends AppCompatActivity {

    // How long the player can see the correct box at the start
    private static final int MEMORIZE_TIME_MS = 3000;
    // Starting time for each level
    private static final int LEVEL_TIME_MS = 45000;
    // Time removed when the player taps a wrong box
    private static final int WRONG_PENALTY_MS = 5000;
    // Coins given for every correct box
    private static final int CORRECT_COINS = 150;
    // How many correct boxes are needed to finish the level
    private static final int TARGET_HITS = 3;

    private TextView tvLevel;
    private TextView tvCoins;
    private TextView tvTimer;
    private TextView tvStatus;
    private TextView tvMemorizeCount;
    private TextView tvTarget;
    private TextView tvScore;
    private TextView tvStreak;
    private ProgressBar progressBar;
    private Button btnHint;
    private Button btnExtraTime;
    private Button btnBoxReveal;
    private Button[] boxButtons = new Button[9];

    private int currentLevel = 1;
    private int correctBox = 0;
    private int correctHits = 0;
    private int score = 0;
    private int streak = 0;
    private int timeLeftMs = LEVEL_TIME_MS;
    private int coinsEarnedThisLevel = 0;

    private boolean isMemorizing = false;
    private boolean isPlaying = false;
    private boolean gameOver = false;

    private CountDownTimer timer;
    private CountDownTimer memorizeTimer;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gameplay);

        // Level number comes from PLAY or the Levels screen
        currentLevel = getIntent().getIntExtra("level", 1);

        connectViews();
        setupClickListeners();

        tvLevel.setText("Level " + currentLevel);
        updateCoinsText();
        startLevel();
    }

    /** Finds every widget from activity_gameplay.xml using its ID. */
    private void connectViews() {
        tvLevel = findViewById(R.id.tvLevel);
        tvCoins = findViewById(R.id.tvCoins);
        tvTimer = findViewById(R.id.tvTimer);
        tvStatus = findViewById(R.id.tvStatus);
        tvMemorizeCount = findViewById(R.id.tvMemorizeCount);
        tvTarget = findViewById(R.id.tvTarget);
        tvScore = findViewById(R.id.tvScore);
        tvStreak = findViewById(R.id.tvStreak);
        progressBar = findViewById(R.id.progressBar);
        btnHint = findViewById(R.id.btnHint);
        btnExtraTime = findViewById(R.id.btnExtraTime);
        btnBoxReveal = findViewById(R.id.btnBoxReveal);

        boxButtons[0] = findViewById(R.id.btnBox1);
        boxButtons[1] = findViewById(R.id.btnBox2);
        boxButtons[2] = findViewById(R.id.btnBox3);
        boxButtons[3] = findViewById(R.id.btnBox4);
        boxButtons[4] = findViewById(R.id.btnBox5);
        boxButtons[5] = findViewById(R.id.btnBox6);
        boxButtons[6] = findViewById(R.id.btnBox7);
        boxButtons[7] = findViewById(R.id.btnBox8);
        boxButtons[8] = findViewById(R.id.btnBox9);
    }

    /** Sets OnClickListener for the 9 boxes and the 3 power-up buttons. */
    private void setupClickListeners() {
        for (int i = 0; i < boxButtons.length; i++) {
            final int boxIndex = i;
            boxButtons[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onBoxClicked(boxIndex);
                }
            });
        }

        btnHint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                useHint();
            }
        });

        btnExtraTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                useExtraTime();
            }
        });

        btnBoxReveal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                useBoxReveal();
            }
        });
    }

    /** Resets score, progress, and timer, then starts the first memorize round. */
    private void startLevel() {
        gameOver = false;
        isPlaying = false;
        correctHits = 0;
        score = 0;
        streak = 0;
        coinsEarnedThisLevel = 0;
        timeLeftMs = LEVEL_TIME_MS;

        tvScore.setText("0");
        tvStreak.setText("0");
        tvTimer.setText(formatTime(timeLeftMs));
        updateProgress();

        if (timer != null) {
            timer.cancel();
        }
        if (memorizeTimer != null) {
            memorizeTimer.cancel();
        }
        handler.removeCallbacksAndMessages(null);

        startMemorizeRound();
    }

    /**
     * MEMORIZE PHASE
     * Picks a random correct box, shows it for 3 seconds,
     * and blocks all taps so the player can only look.
     * A 3-2-1 countdown tells the player when this phase ends.
     */
    private void startMemorizeRound() {
        isMemorizing = true;
        isPlaying = false;
        correctBox = random.nextInt(9);

        resetAllBoxes();
        setGridEnabled(false);
        setPowerUpsEnabled(false);

        tvMemorizeCount.setVisibility(View.VISIBLE);
        updateMemorizeCountdown(3);
        boxButtons[correctBox].setBackgroundResource(R.drawable.bg_box_correct);

        startMemorizeCountdown();
    }

    /** Updates the big 3-2-1 number every second during memorize. */
    private void startMemorizeCountdown() {
        if (memorizeTimer != null) {
            memorizeTimer.cancel();
        }

        memorizeTimer = new CountDownTimer(MEMORIZE_TIME_MS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int secondsLeft = (int) Math.ceil(millisUntilFinished / 1000.0);
                if (secondsLeft < 1) {
                    secondsLeft = 1;
                }
                updateMemorizeCountdown(secondsLeft);
            }

            @Override
            public void onFinish() {
                updateMemorizeCountdown(0);
                coverBoxesAndStartPlay();
            }
        };
        memorizeTimer.start();
    }

    private void updateMemorizeCountdown(int secondsLeft) {
        tvMemorizeCount.setText(String.valueOf(secondsLeft));
        if (secondsLeft > 0) {
            tvStatus.setText(getString(R.string.memorize_countdown, secondsLeft));
        }
    }

    /**
     * COVER / SHUFFLE PHASE
     * All boxes look the same again. Then the countdown timer starts.
     */
    private void coverBoxesAndStartPlay() {
        if (gameOver) {
            return;
        }

        isMemorizing = false;
        isPlaying = true;

        resetAllBoxes();
        setGridEnabled(true);
        setPowerUpsEnabled(true);

        tvMemorizeCount.setVisibility(View.GONE);
        tvStatus.setText(R.string.choose_box);
        startTimer();
    }

    /** Starts or restarts CountDownTimer using the remaining milliseconds. */
    private void startTimer() {
        if (timer != null) {
            timer.cancel();
        }

        timer = new CountDownTimer(timeLeftMs, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftMs = (int) millisUntilFinished;
                tvTimer.setText(formatTime(timeLeftMs));
            }

            @Override
            public void onFinish() {
                tvTimer.setText("0:00");
                if (!gameOver) {
                    endGameByTimeout();
                }
            }
        };
        timer.start();
    }

    /**
     * TAP PHASE
     * Correct box: +150 coins and more progress.
     * Wrong box: lose 5 seconds and show "Wrong Box!".
     */
    private void onBoxClicked(int boxIndex) {
        if (gameOver || isMemorizing || !isPlaying) {
            return;
        }

        if (boxIndex == correctBox) {
            handleCorrectBox(boxIndex);
        } else {
            handleWrongBox(boxIndex);
        }
    }

    private void handleCorrectBox(int boxIndex) {
        isPlaying = false;
        if (timer != null) {
            timer.cancel();
        }

        boxButtons[boxIndex].setBackgroundResource(R.drawable.bg_box_correct);
        Toast.makeText(this, R.string.correct_box, Toast.LENGTH_SHORT).show();

        correctHits = correctHits + 1;
        streak = streak + 1;
        score = score + 10 + (streak * 2);
        coinsEarnedThisLevel = coinsEarnedThisLevel + CORRECT_COINS;

        GamePrefs.addCoins(this, CORRECT_COINS);
        updateCoinsText();
        tvScore.setText(String.valueOf(score));
        tvStreak.setText(String.valueOf(streak));
        updateProgress();

        // Level is finished when the player hits the target (progress 100%)
        if (correctHits >= TARGET_HITS) {
            winLevel();
        } else {
            // Next memorize round, timer continues from remaining time
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startMemorizeRound();
                }
            }, 800);
        }
    }

    private void handleWrongBox(int boxIndex) {
        boxButtons[boxIndex].setBackgroundResource(R.drawable.bg_box_wrong);
        boxButtons[boxIndex].setEnabled(false);
        streak = 0;
        tvStreak.setText("0");
        Toast.makeText(this, R.string.wrong_box, Toast.LENGTH_SHORT).show();

        // Penalty: remove 5 seconds from the countdown
        timeLeftMs = timeLeftMs - WRONG_PENALTY_MS;
        if (timeLeftMs <= 0) {
            tvTimer.setText("0:00");
            endGameByTimeout();
            return;
        }

        tvTimer.setText(formatTime(timeLeftMs));
        startTimer();
    }

    /** Shows the Level Complete dialog and unlocks the next level. */
    private void winLevel() {
        gameOver = true;
        isPlaying = false;
        if (timer != null) {
            timer.cancel();
        }

        GamePrefs.unlockLevel(this, currentLevel + 1);
        progressBar.setProgress(100);
        showLevelCompleteDialog(coinsEarnedThisLevel);
    }

    private void endGameByTimeout() {
        gameOver = true;
        isPlaying = false;
        if (timer != null) {
            timer.cancel();
        }
        setGridEnabled(false);
        setPowerUpsEnabled(false);
        tvStatus.setText("Time's up!");
        Toast.makeText(this, "Time's up!", Toast.LENGTH_SHORT).show();
    }

    private void useHint() {
        if (!canUsePowerUp()) {
            return;
        }

        if (GamePrefs.getHints(this) > 0) {
            GamePrefs.useHint(this);
            highlightCorrectBox();
            return;
        }

        if (GamePrefs.spendCoins(this, 50)) {
            updateCoinsText();
            highlightCorrectBox();
        } else {
            Toast.makeText(this, "Not enough coins for a Hint.", Toast.LENGTH_SHORT).show();
        }
    }

    private void highlightCorrectBox() {
        boxButtons[correctBox].setBackgroundResource(R.drawable.bg_box_hint);
        Toast.makeText(this, "Hint: the yellow box is correct!", Toast.LENGTH_SHORT).show();
    }

    private void useExtraTime() {
        if (!canUsePowerUp()) {
            return;
        }

        boolean usedSavedItem = GamePrefs.getExtraTime(this) > 0 && GamePrefs.useExtraTime(this);
        if (!usedSavedItem && !GamePrefs.spendCoins(this, 100)) {
            Toast.makeText(this, "Not enough coins for Extra Time.", Toast.LENGTH_SHORT).show();
            return;
        }

        updateCoinsText();
        timeLeftMs = timeLeftMs + 20000;
        startTimer();
        Toast.makeText(this, "Added 20 seconds!", Toast.LENGTH_SHORT).show();
    }

    /** Reveals 3 wrong boxes so it is easier to find the correct one. */
    private void useBoxReveal() {
        if (!canUsePowerUp()) {
            return;
        }

        boolean usedSavedItem = GamePrefs.getBoxReveal(this) > 0 && GamePrefs.useBoxReveal(this);
        if (!usedSavedItem && !GamePrefs.spendCoins(this, 150)) {
            Toast.makeText(this, "Not enough coins for Box Reveal.", Toast.LENGTH_SHORT).show();
            return;
        }

        updateCoinsText();

        ArrayList<Integer> wrongBoxes = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            if (i != correctBox && boxButtons[i].isEnabled()) {
                wrongBoxes.add(i);
            }
        }
        Collections.shuffle(wrongBoxes);

        int revealCount = Math.min(3, wrongBoxes.size());
        for (int i = 0; i < revealCount; i++) {
            int index = wrongBoxes.get(i);
            boxButtons[index].setBackgroundResource(R.drawable.bg_box_wrong);
            boxButtons[index].setEnabled(false);
        }

        Toast.makeText(this, "3 boxes revealed!", Toast.LENGTH_SHORT).show();
    }

    private boolean canUsePowerUp() {
        return !gameOver && isPlaying && !isMemorizing;
    }

    private void showLevelCompleteDialog(int coinsEarned) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_level_complete);
        dialog.setCancelable(false);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView tvCoinsEarned = dialog.findViewById(R.id.tvCoinsEarned);
        ImageView ivStar1 = dialog.findViewById(R.id.ivStar1);
        ImageView ivStar2 = dialog.findViewById(R.id.ivStar2);
        ImageView ivStar3 = dialog.findViewById(R.id.ivStar3);
        Button btnHome = dialog.findViewById(R.id.btnHome);
        Button btnNext = dialog.findViewById(R.id.btnNext);
        Button btnReplay = dialog.findViewById(R.id.btnReplay);

        tvCoinsEarned.setText("+" + coinsEarned + " Coins");

        // Stars depend on how much time is left
        int secondsLeft = timeLeftMs / 1000;
        ivStar1.setImageResource(R.drawable.ic_star);
        ivStar2.setImageResource(secondsLeft >= 15 ? R.drawable.ic_star : R.drawable.ic_star_empty);
        ivStar3.setImageResource(secondsLeft >= 30 ? R.drawable.ic_star : R.drawable.ic_star_empty);

        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                finish();
            }
        });

        btnReplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                startLevel();
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Intent intent = new Intent(GameplayActivity.this, GameplayActivity.class);
                intent.putExtra("level", currentLevel + 1);
                startActivity(intent);
                finish();
            }
        });

        dialog.show();
    }

    private void resetAllBoxes() {
        for (Button button : boxButtons) {
            button.setBackgroundResource(R.drawable.bg_box);
        }
    }

    private void setGridEnabled(boolean enabled) {
        for (Button button : boxButtons) {
            button.setEnabled(enabled);
        }
    }

    private void setPowerUpsEnabled(boolean enabled) {
        btnHint.setEnabled(enabled);
        btnExtraTime.setEnabled(enabled);
        btnBoxReveal.setEnabled(enabled);
    }

    private void updateProgress() {
        int progress = (correctHits * 100) / TARGET_HITS;
        progressBar.setProgress(progress);
        tvTarget.setText(correctHits + " / " + TARGET_HITS);
    }

    private void updateCoinsText() {
        tvCoins.setText(String.valueOf(GamePrefs.getCoins(this)));
    }

    /** Turns milliseconds into a 0:45 style text. */
    private String formatTime(int milliseconds) {
        int seconds = Math.max(0, milliseconds / 1000);
        return "0:" + String.format("%02d", seconds);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop timer and delayed memorize callback when the screen closes
        if (timer != null) {
            timer.cancel();
        }
        if (memorizeTimer != null) {
            memorizeTimer.cancel();
        }
        handler.removeCallbacksAndMessages(null);
    }
}
