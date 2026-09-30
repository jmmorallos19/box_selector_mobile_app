package com.boxselector.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

/** Shows 15 stages for the selected difficulty. */
public class LevelsActivity extends AppCompatActivity {

    private String selected = DifficultyConfig.EASY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_levels);

        String difficulty = getIntent().getStringExtra("difficulty");
        selected = difficulty == null ? DifficultyConfig.EASY : difficulty;
        if (!GamePrefs.isDifficultyUnlocked(this, selected)) {
            Toast.makeText(this, "Finish the previous difficulty first.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        TextView tvTitle = findViewById(R.id.tvLevelsTitle);
        tvTitle.setText(selected.toUpperCase());

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SoundPlayer.playClick(LevelsActivity.this);
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        showStages();
    }

    private void showStages() {
        GridLayout grid = findViewById(R.id.gridLevels);
        if (grid == null) {
            return;
        }
        grid.removeAllViews();
        int unlocked = GamePrefs.getHighestStage(this, selected);

        for (int i = 1; i <= DifficultyConfig.STAGE_COUNT; i++) {
            final int stage = i;
            Button button = new Button(this);
            button.setText(String.valueOf(stage));
            button.setTextSize(18);
            button.setAllCaps(false);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = (int) (72 * getResources().getDisplayMetrics().density);
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(12, 12, 12, 12);
            button.setLayoutParams(params);

            if (stage <= unlocked) {
                button.setBackgroundResource(R.drawable.bg_button_green);
                button.setTextColor(ContextCompat.getColor(this, R.color.white));
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        SoundPlayer.playClick(LevelsActivity.this);
                        Intent intent = new Intent(LevelsActivity.this, GameplayActivity.class);
                        intent.putExtra("difficulty", selected);
                        intent.putExtra("stage", stage);
                        startActivity(intent);
                    }
                });
            } else {
                button.setBackgroundResource(R.drawable.bg_item_locked);
                button.setTextColor(ContextCompat.getColor(this, R.color.text_gray));
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(LevelsActivity.this, "Stage locked!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            grid.addView(button);
        }
    }
}
