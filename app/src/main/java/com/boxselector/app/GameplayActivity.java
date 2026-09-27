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
 * Game screen.
 *
 * Flow:
 * 1. Memorize the green box while the 3-2-1 countdown runs.
 * 2. Boxes are covered and the stage timer starts.
 * 3. Correct tap = coins + progress. Wrong tap or timeout = lose 1 life.
 * 4. 0 lives = Game Over. Target reached = Level Complete.
 */
public class GameplayActivity extends AppCompatActivity {

    private static final int WRONG_PENALTY_MS = 5000;

    private TextView tvLevel;
    private TextView tvLives;
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

    private String difficulty = DifficultyConfig.NORMAL;
    private int currentStage = 1;
    private int lives = 3;
    private int targetHits = 3;
    private int memorizeTimeMs = 3000;
    private int levelTimeMs = 45000;
    private int coinReward = 150;

    private int correctBox = 0;
    private int correctHits = 0;
    private int score = 0;
    private int streak = 0;
    private int timeLeftMs = 45000;
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

        difficulty = getIntent().getStringExtra("difficulty");
        if (difficulty == null) {
            difficulty = DifficultyConfig.NORMAL;
        }
        currentStage = getIntent().getIntExtra("stage", getIntent().getIntExtra("level", 1));
        applyDifficultySettings();

        connectViews();
        setupClickListeners();
        updateHeader();
        updateCoinsText();
        startLevel();
    }

    /** Loads timer, lives, and target based on difficulty and stage. */
    private void applyDifficultySettings() {
        lives = DifficultyConfig.getStartLives(difficulty);
        targetHits = DifficultyConfig.getTargetHits(difficulty, currentStage);
        memorizeTimeMs = DifficultyConfig.getMemorizeMs(difficulty);
        levelTimeMs = DifficultyConfig.getTimeMs(difficulty, currentStage);
        coinReward = DifficultyConfig.getCoinReward(difficulty);
        timeLeftMs = levelTimeMs;
    }

    private void connectViews() {
        tvLevel = findViewById(R.id.tvLevel);
        tvLives = findViewById(R.id.tvLives);
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
                SoundPlayer.playClick(GameplayActivity.this);
                useHint();
            }
        });
        btnExtraTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SoundPlayer.playClick(GameplayActivity.this);
                useExtraTime();
            }
        });
        btnBoxReveal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SoundPlayer.playClick(GameplayActivity.this);
                useBoxReveal();
            }
        });
    }

    private void startLevel() {
        gameOver = false;
        isPlaying = false;
        correctHits = 0;
        score = 0;
        streak = 0;
        coinsEarnedThisLevel = 0;
        applyDifficultySettings();

        tvScore.setText("0");
        tvStreak.setText("0");
        tvTimer.setText(formatTime(timeLeftMs));
        updateHeader();
        updateProgress();
        cancelTimers();
        startMemorizeRound();
    }

    private void startMemorizeRound() {
        isMemorizing = true;
        isPlaying = false;
        correctBox = random.nextInt(9);

        resetAllBoxes();
        setGridEnabled(false);
        setPowerUpsEnabled(false);

        tvMemorizeCount.setVisibility(View.VISIBLE);
        updateMemorizeCountdown(memorizeTimeMs / 1000);
        boxButtons[correctBox].setBackgroundResource(R.drawable.bg_box_correct);
        startMemorizeCountdown();
    }

    private void startMemorizeCountdown() {
        if (memorizeTimer != null) {
            memorizeTimer.cancel();
        }
        memorizeTimer = new CountDownTimer(memorizeTimeMs, 1000) {
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
                    loseLife("Time's up!");
                }
            }
        };
        timer.start();
    }

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

        SoundPlayer.playCorrect(this);
        boxButtons[boxIndex].setBackgroundResource(R.drawable.bg_box_correct);
        Toast.makeText(this, R.string.correct_box, Toast.LENGTH_SHORT).show();

        correctHits = correctHits + 1;
        streak = streak + 1;
        score = score + 100 + (streak * 10);
        coinsEarnedThisLevel = coinsEarnedThisLevel + coinReward;
        GamePrefs.addCoins(this, coinReward);
        updateCoinsText();
        tvScore.setText(String.valueOf(score));
        tvStreak.setText(String.valueOf(streak));
        updateProgress();

        if (correctHits >= targetHits) {
            winLevel();
        } else {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startMemorizeRound();
                }
            }, 700);
        }
    }

    private void handleWrongBox(int boxIndex) {
        SoundPlayer.playWrong(this);
        boxButtons[boxIndex].setBackgroundResource(R.drawable.bg_box_wrong);
        boxButtons[boxIndex].setEnabled(false);
        streak = 0;
        tvStreak.setText("0");

        timeLeftMs = timeLeftMs - WRONG_PENALTY_MS;
        if (timeLeftMs <= 0) {
            tvTimer.setText("0:00");
            loseLife("Time's up!");
            return;
        }
        tvTimer.setText(formatTime(timeLeftMs));
        loseLife("Wrong Box!");
        if (!gameOver) {
            startTimer();
        }
    }

    /** Removes 1 life. If no lives remain, the game ends. */
    private void loseLife(String reason) {
        if (gameOver) {
            return;
        }
        lives = lives - 1;
        updateHeader();

        if (lives <= 0) {
            endGame(reason);
            return;
        }

        Toast.makeText(this, reason + "  Lives left: " + lives, Toast.LENGTH_SHORT).show();
        if ("Time's up!".equals(reason)) {
            timeLeftMs = levelTimeMs;
            tvTimer.setText(formatTime(timeLeftMs));
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!gameOver) {
                        startMemorizeRound();
                    }
                }
            }, 800);
        }
    }

    private void winLevel() {
        gameOver = true;
        isPlaying = false;
        cancelTimers();

        int finalScore = calculateFinalScore();
        GamePrefs.unlockStage(this, difficulty, currentStage + 1);
        GamePrefs.addLeaderboardEntry(this, GamePrefs.getUsername(this), finalScore, difficulty);
        progressBar.setProgress(100);
        SoundPlayer.playWin(this);
        showLevelCompleteDialog(coinsEarnedThisLevel, finalScore);
    }

    private void endGame(String reason) {
        gameOver = true;
        isPlaying = false;
        cancelTimers();
        setGridEnabled(false);
        setPowerUpsEnabled(false);
        tvStatus.setText(R.string.game_over);
        SoundPlayer.playGameOver(this);

        int finalScore = calculateFinalScore();
        GamePrefs.addLeaderboardEntry(this, GamePrefs.getUsername(this), finalScore, difficulty);
        showGameOverDialog(reason, finalScore);
    }

    /** Score = points + leftover time + leftover lives + difficulty bonus. */
    private int calculateFinalScore() {
        int secondsLeft = Math.max(0, timeLeftMs / 1000);
        return score + (secondsLeft * 5) + (Math.max(lives, 0) * 40)
                + DifficultyConfig.getScoreBonus(difficulty);
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
            Toast.makeText(this, "Not enough coins or hints.", Toast.LENGTH_SHORT).show();
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
        boolean usedSaved = GamePrefs.getExtraTime(this) > 0 && GamePrefs.useExtraTime(this);
        if (!usedSaved && !GamePrefs.spendCoins(this, 100)) {
            Toast.makeText(this, "Not enough coins for Extra Time.", Toast.LENGTH_SHORT).show();
            return;
        }
        updateCoinsText();
        timeLeftMs = timeLeftMs + 20000;
        startTimer();
        Toast.makeText(this, "Added 20 seconds!", Toast.LENGTH_SHORT).show();
    }

    private void useBoxReveal() {
        if (!canUsePowerUp()) {
            return;
        }
        boolean usedSaved = GamePrefs.getBoxReveal(this) > 0 && GamePrefs.useBoxReveal(this);
        if (!usedSaved && !GamePrefs.spendCoins(this, 150)) {
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

    private void showLevelCompleteDialog(int coinsEarned, int finalScore) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_level_complete);
        dialog.setCancelable(false);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView tvCoinsEarned = dialog.findViewById(R.id.tvCoinsEarned);
        TextView tvDialogTitle = dialog.findViewById(R.id.tvDialogTitle);
        ImageView ivStar1 = dialog.findViewById(R.id.ivStar1);
        ImageView ivStar2 = dialog.findViewById(R.id.ivStar2);
        ImageView ivStar3 = dialog.findViewById(R.id.ivStar3);
        Button btnHome = dialog.findViewById(R.id.btnHome);
        Button btnNext = dialog.findViewById(R.id.btnNext);
        Button btnReplay = dialog.findViewById(R.id.btnReplay);

        tvDialogTitle.setText("Stage Complete!");
        tvCoinsEarned.setText("+" + coinsEarned + " Coins   •   Score " + finalScore);

        int secondsLeft = timeLeftMs / 1000;
        ivStar1.setImageResource(R.drawable.ic_star);
        ivStar2.setImageResource(secondsLeft >= 10 ? R.drawable.ic_star : R.drawable.ic_star_empty);
        ivStar3.setImageResource(secondsLeft >= 20 ? R.drawable.ic_star : R.drawable.ic_star_empty);

        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SoundPlayer.playClick(GameplayActivity.this);
                dialog.dismiss();
                finish();
            }
        });
        btnReplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SoundPlayer.playClick(GameplayActivity.this);
                dialog.dismiss();
                startLevel();
            }
        });
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SoundPlayer.playClick(GameplayActivity.this);
                dialog.dismiss();
                if (currentStage >= DifficultyConfig.STAGE_COUNT) {
                    Toast.makeText(GameplayActivity.this, "All 15 stages done!", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
                Intent intent = new Intent(GameplayActivity.this, GameplayActivity.class);
                intent.putExtra("difficulty", difficulty);
                intent.putExtra("stage", currentStage + 1);
                startActivity(intent);
                finish();
            }
        });
        dialog.show();
    }

    private void showGameOverDialog(String reason, int finalScore) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_game_over);
        dialog.setCancelable(false);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView tvScore = dialog.findViewById(R.id.tvGameOverScore);
        TextView tvReason = dialog.findViewById(R.id.tvGameOverReason);
        Button btnHome = dialog.findViewById(R.id.btnGameOverHome);
        Button btnRetry = dialog.findViewById(R.id.btnGameOverRetry);

        tvScore.setText("Score: " + finalScore);
        tvReason.setText(reason + " You ran out of lives.");

        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SoundPlayer.playClick(GameplayActivity.this);
                dialog.dismiss();
                finish();
            }
        });
        btnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SoundPlayer.playClick(GameplayActivity.this);
                dialog.dismiss();
                startLevel();
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
        int progress = (correctHits * 100) / targetHits;
        progressBar.setProgress(progress);
        tvTarget.setText(correctHits + " / " + targetHits);
    }

    private void updateHeader() {
        tvLevel.setText(difficulty + "  " + currentStage + "/" + DifficultyConfig.STAGE_COUNT);
        tvLives.setText("x " + Math.max(lives, 0));
    }

    private void updateCoinsText() {
        tvCoins.setText(String.valueOf(GamePrefs.getCoins(this)));
    }

    private String formatTime(int milliseconds) {
        int seconds = Math.max(0, milliseconds / 1000);
        return "0:" + String.format("%02d", seconds);
    }

    private void cancelTimers() {
        if (timer != null) {
            timer.cancel();
        }
        if (memorizeTimer != null) {
            memorizeTimer.cancel();
        }
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelTimers();
    }
}
