package com.boxselector.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

/** Player picks Easy, Normal, Hard, Expert, or Extreme. */
public class DifficultyActivity extends AppCompatActivity {

    private boolean selectStageMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_difficulty);

        selectStageMode = getIntent().getBooleanExtra("selectStage", false);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SoundPlayer.playClick(DifficultyActivity.this);
                finish();
            }
        });

        setupDifficultyButton(R.id.btnEasy, DifficultyConfig.EASY);
        setupDifficultyButton(R.id.btnMedium, DifficultyConfig.MEDIUM);
        setupDifficultyButton(R.id.btnHard, DifficultyConfig.HARD);
        setupDifficultyButton(R.id.btnExpert, DifficultyConfig.EXPERT);
        setupDifficultyButton(R.id.btnExtreme, DifficultyConfig.EXTREME);
    }

    private void setupDifficultyButton(int buttonId, final String difficulty) {
        Button button = findViewById(buttonId);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SoundPlayer.playClick(DifficultyActivity.this);
                openDifficulty(difficulty);
            }
        });
    }

    private void openDifficulty(String difficulty) {
        if (selectStageMode) {
            Intent intent = new Intent(this, LevelsActivity.class);
            intent.putExtra("difficulty", difficulty);
            startActivity(intent);
        } else {
            int stage = GamePrefs.getHighestStage(this, difficulty);
            Intent intent = new Intent(this, GameplayActivity.class);
            intent.putExtra("difficulty", difficulty);
            intent.putExtra("stage", stage);
            startActivity(intent);
        }
    }
}
