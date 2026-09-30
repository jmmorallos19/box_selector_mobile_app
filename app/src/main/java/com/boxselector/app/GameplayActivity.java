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
 * Box Selector gameplay: memorize the green boxes, then tap them after they are covered.
 * Harder difficulties add more boxes, a shorter look, and moving highlights.
 */
public class GameplayActivity extends AppCompatActivity {

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

    private String difficulty = DifficultyConfig.EASY;
    private int currentStage = 1;
    private int lifeLimit = 4;
    private int stageLives = 4;
    private int boxCount = 1;
    private int roundsToClear = 2;
    private int memorizeTimeMs = 4000;
    private int correctHits = 0;
    private int streak = 0;
    private int timeLeftMs = 30000;
    private int moveStyle = 0;
    private boolean timedOut = false;
    private boolean targetsMovedThisRound = false;
    private int memorizeTickCount = 0;

    private final ArrayList<Integer> targetBoxes = new ArrayList<>();
    private final ArrayList<Integer> foundBoxes = new ArrayList<>();

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

        difficulty = getIntent().getStringExtra("difficulty");
        if (difficulty == null) {
            difficulty = DifficultyConfig.EASY;
        }
        currentStage = getIntent().getIntExtra("stage", getIntent().getIntExtra("level", 1));
        if (!GamePrefs.isDifficultyUnlocked(this, difficulty)) {
            Toast.makeText(this, "Finish the previous difficulty first.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_gameplay);
        connectViews();
        setupClickListeners();
        startAttempt();
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

        TextView tvHintPrice = findViewById(R.id.tvHintPrice);
        TextView tvExtraTimePrice = findViewById(R.id.tvExtraTimePrice);
        tvHintPrice.setText(DifficultyConfig.HINT_COST + " Coins");
        tvExtraTimePrice.setText("+" + DifficultyConfig.EXTRA_TIME_SECONDS + " sec");

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

    /** Starts or retries the stage using the life cap. */
    private void startAttempt() {
        gameOver = false;
        isPlaying = false;
        timedOut = false;
        correctHits = 0;
        streak = 0;
        lifeLimit = DifficultyConfig.getLifeLimit(difficulty);
        boxCount = DifficultyConfig.getBoxCount(difficulty, currentStage);
        roundsToClear = DifficultyConfig.getRoundsToClear(difficulty, currentStage);
        memorizeTimeMs = DifficultyConfig.getMemorizeMs(difficulty);
        moveStyle = DifficultyConfig.getMoveStyle(difficulty);
        timeLeftMs = DifficultyConfig.getTimeMs(difficulty, currentStage);

        int savedLives = GamePrefs.getSavedLives(this, difficulty);
        if (savedLives <= 0) {
            GamePrefs.restoreStartingLives(this, difficulty);
            savedLives = GamePrefs.getSavedLives(this, difficulty);
        }
        stageLives = Math.min(savedLives, lifeLimit);

        tvScore.setText("0");
        tvStreak.setText("0");
        tvTimer.setText(formatTime(timeLeftMs));
        updateHeader();
        updateProgress();
        updateCoinsText();
        cancelTimers();
        startMemorizeRound();
    }

    private void startMemorizeRound() {
        if (gameOver) {
            return;
        }
        isMemorizing = true;
        isPlaying = false;
        foundBoxes.clear();
        targetsMovedThisRound = false;
        memorizeTickCount = 0;
        pickTargetBoxes();
        setGridEnabled(false);
        setPowerUpsEnabled(false);
        highlightTargets();

        tvMemorizeCount.setVisibility(View.VISIBLE);
        updateMemorizeCountdown((int) Math.ceil(memorizeTimeMs / 1000.0));
        startMemorizeCountdown();
    }

    private void pickTargetBoxes() {
        targetBoxes.clear();
        while (targetBoxes.size() < boxCount) {
            int box = random.nextInt(9);
            if (!targetBoxes.contains(box)) {
                targetBoxes.add(box);
            }
        }
    }

    private void highlightTargets() {
        resetAllBoxes();
        for (int i = 0; i < targetBoxes.size(); i++) {
            boxButtons[targetBoxes.get(i)].setBackgroundResource(R.drawable.bg_box_correct);
        }
    }

    private void startMemorizeCountdown() {
        if (memorizeTimer != null) {
            memorizeTimer.cancel();
        }
        memorizeTimer = new CountDownTimer(memorizeTimeMs, 400) {
            @Override
            public void onTick(long millisUntilFinished) {
                memorizeTickCount = memorizeTickCount + 1;
                int secondsLeft = (int) Math.ceil(millisUntilFinished / 1000.0);
                if (secondsLeft < 1) {
                    secondsLeft = 1;
                }
                updateMemorizeCountdown(secondsLeft);
                maybeMoveTargets(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                updateMemorizeCountdown(0);
                coverBoxesAndStartPlay();
            }
        };
        memorizeTimer.start();
    }

    private void maybeMoveTargets(long millisUntilFinished) {
        if (moveStyle == 0) {
            return;
        }
        if (moveStyle == 1) {
            if (!targetsMovedThisRound && millisUntilFinished <= memorizeTimeMs / 2) {
                targetsMovedThisRound = true;
                pickTargetBoxes();
                highlightTargets();
            }
            return;
        }
        if (memorizeTickCount % 2 == 0) {
            pickTargetBoxes();
            highlightTargets();
        }
    }

    private void updateMemorizeCountdown(int secondsLeft) {
        tvMemorizeCount.setText(String.valueOf(secondsLeft));
        if (boxCount == 1) {
            tvStatus.setText(getString(R.string.memorize_countdown, secondsLeft));
        } else {
            tvStatus.setText("Memorize " + boxCount + " boxes!  " + secondsLeft);
        }
    }

    private void coverBoxesAndStartPlay() {
        if (gameOver) {
            return;
        }
        isMemorizing = false;
        isPlaying = true;
        foundBoxes.clear();
        resetAllBoxes();
        setGridEnabled(true);
        setPowerUpsEnabled(true);
        tvMemorizeCount.setVisibility(View.GONE);
        if (boxCount == 1) {
            tvStatus.setText(R.string.choose_box);
        } else {
            tvStatus.setText("Find " + boxCount + " boxes!");
        }
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
                timeLeftMs = 0;
                if (!gameOver && isPlaying) {
                    timedOut = true;
                    loseLife("Time's up!");
                }
            }
        };
        timer.start();
        tvTimer.setText(formatTime(timeLeftMs));
    }

    private void onBoxClicked(int boxIndex) {
        if (gameOver || isMemorizing || !isPlaying) {
            return;
        }
        if (targetBoxes.contains(boxIndex) && !foundBoxes.contains(boxIndex)) {
            handleCorrectBox(boxIndex);
        } else if (foundBoxes.contains(boxIndex)) {
            return;
        } else {
            handleWrongBox(boxIndex);
        }
    }

    private void handleCorrectBox(int boxIndex) {
        SoundPlayer.playCorrect(this);
        foundBoxes.add(boxIndex);
        boxButtons[boxIndex].setBackgroundResource(R.drawable.bg_box_correct);
        boxButtons[boxIndex].setEnabled(false);

        if (foundBoxes.size() < targetBoxes.size()) {
            int left = targetBoxes.size() - foundBoxes.size();
            tvStatus.setText(left + " box" + (left == 1 ? "" : "es") + " left");
            return;
        }

        isPlaying = false;
        if (timer != null) {
            timer.cancel();
        }

        correctHits = correctHits + 1;
        streak = streak + 1;
        tvScore.setText(String.valueOf(correctHits * 10));
        tvStreak.setText(String.valueOf(streak));
        updateProgress();
        Toast.makeText(this, R.string.correct_box, Toast.LENGTH_SHORT).show();

        if (correctHits >= roundsToClear) {
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
        loseLife("Wrong Box!");
    }

    private void loseLife(String reason) {
        if (gameOver) {
            return;
        }

        stageLives = stageLives - 1;
        int saved = GamePrefs.getSavedLives(this, difficulty) - 1;
        GamePrefs.setSavedLives(this, difficulty, saved);
        updateHeader();

        if (saved <= 0) {
            GamePrefs.restoreStartingLives(this, difficulty);
            endGame(reason);
            return;
        }

        if (stageLives <= 0 || timedOut) {
            failAttempt(reason);
            return;
        }

        Toast.makeText(this, reason + "  Stage lives: " + stageLives, Toast.LENGTH_SHORT).show();
    }

    private void failAttempt(String reason) {
        isPlaying = false;
        isMemorizing = false;
        cancelTimers();
        showRetryDialog("Stage Failed", reason + "\nSaved lives: " + GamePrefs.getSavedLives(this, difficulty), true);
    }

    private void winLevel() {
        gameOver = true;
        isPlaying = false;
        cancelTimers();

        int secondsLeft = Math.max(0, timeLeftMs / 1000);
        int finalScore = DifficultyConfig.getClearScore(difficulty, currentStage, correctHits, secondsLeft);
        int coins = DifficultyConfig.getClearCoins(difficulty, currentStage, secondsLeft);
        GamePrefs.addCoins(this, coins);
        GamePrefs.unlockStage(this, difficulty, currentStage + 1);
        if (currentStage >= DifficultyConfig.STAGE_COUNT) {
            GamePrefs.markDifficultyComplete(this, difficulty);
        }
        GamePrefs.addLeaderboardEntry(this, GamePrefs.getUsername(this), finalScore, difficulty);
        progressBar.setProgress(100);
        SoundPlayer.playWin(this);
        showLevelCompleteDialog(coins, finalScore);
    }

    private void endGame(String reason) {
        gameOver = true;
        isPlaying = false;
        cancelTimers();
        setGridEnabled(false);
        setPowerUpsEnabled(false);
        tvStatus.setText(R.string.game_over);
        SoundPlayer.playGameOver(this);
        int secondsLeft = Math.max(0, timeLeftMs / 1000);
        int finalScore = DifficultyConfig.getClearScore(difficulty, currentStage, correctHits, secondsLeft);
        GamePrefs.addLeaderboardEntry(this, GamePrefs.getUsername(this), finalScore, difficulty);
        showRetryDialog("Game Over", reason + " Saved lives are restored.", false);
    }

    private void useHint() {
        if (!canUsePowerUp()) {
            return;
        }
        boolean usedSaved = GamePrefs.getHints(this) > 0 && GamePrefs.useHint(this);
        if (!usedSaved && !GamePrefs.spendCoins(this, DifficultyConfig.HINT_COST)) {
            Toast.makeText(this, "Need " + DifficultyConfig.HINT_COST + " coins for a Hint.", Toast.LENGTH_SHORT).show();
            return;
        }
        updateCoinsText();
        for (int i = 0; i < targetBoxes.size(); i++) {
            int index = targetBoxes.get(i);
            if (!foundBoxes.contains(index)) {
                boxButtons[index].setBackgroundResource(R.drawable.bg_box_hint);
                Toast.makeText(this, "Hint: the yellow box is correct!", Toast.LENGTH_SHORT).show();
                return;
            }
        }
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
        timeLeftMs = timeLeftMs + (DifficultyConfig.EXTRA_TIME_SECONDS * 1000);
        startTimer();
        Toast.makeText(this, "Added " + DifficultyConfig.EXTRA_TIME_SECONDS + " seconds!", Toast.LENGTH_SHORT).show();
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
            if (!targetBoxes.contains(i) && boxButtons[i].isEnabled()) {
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
        ivStar2.setImageResource(secondsLeft >= 8 ? R.drawable.ic_star : R.drawable.ic_star_empty);
        ivStar3.setImageResource(secondsLeft >= 16 ? R.drawable.ic_star : R.drawable.ic_star_empty);

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
                startAttempt();
            }
        });
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SoundPlayer.playClick(GameplayActivity.this);
                dialog.dismiss();
                if (currentStage >= DifficultyConfig.STAGE_COUNT) {
                    Toast.makeText(GameplayActivity.this, "Difficulty complete! Next mode unlocked.", Toast.LENGTH_LONG).show();
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

    private void showRetryDialog(String title, String message, final boolean retrySameStage) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_game_over);
        dialog.setCancelable(false);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView tvTitle = dialog.findViewById(R.id.tvGameOverTitle);
        TextView tvScore = dialog.findViewById(R.id.tvGameOverScore);
        TextView tvReason = dialog.findViewById(R.id.tvGameOverReason);
        Button btnHome = dialog.findViewById(R.id.btnGameOverHome);
        Button btnRetry = dialog.findViewById(R.id.btnGameOverRetry);

        tvTitle.setText(title);
        tvScore.setText("Rounds: " + correctHits + " / " + roundsToClear);
        tvReason.setText(message);

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
                startAttempt();
            }
        });
        if (!retrySameStage) {
            btnRetry.setText("Play again");
        }
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
        int progress = roundsToClear == 0 ? 0 : (correctHits * 100) / roundsToClear;
        progressBar.setProgress(progress);
        tvTarget.setText(correctHits + " / " + roundsToClear);
    }

    private void updateHeader() {
        tvLevel.setText(difficulty + "  " + currentStage);
        tvLives.setText("x " + Math.max(stageLives, 0));
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
