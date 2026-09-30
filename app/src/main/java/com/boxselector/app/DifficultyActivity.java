package com.boxselector.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

/** Player picks a difficulty, then a stage. Locked modes stay closed. */
public class DifficultyActivity extends AppCompatActivity {

    private LinearLayout layoutDifficulties;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_difficulty);

        layoutDifficulties = findViewById(R.id.layoutDifficulties);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SoundPlayer.playClick(DifficultyActivity.this);
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        showDifficulties();
    }

    private void showDifficulties() {
        layoutDifficulties.removeAllViews();
        String[] names = DifficultyConfig.allNames();
        int[] backgrounds = {
                R.drawable.bg_button_green,
                R.drawable.bg_button_yellow,
                R.drawable.bg_button_white,
                R.drawable.bg_button_white,
                R.drawable.bg_box_wrong,
                R.drawable.bg_button_white,
                R.drawable.bg_box_wrong,
                R.drawable.bg_box_wrong
        };

        for (int i = 0; i < names.length; i++) {
            layoutDifficulties.addView(makeDifficultyBlock(names[i], backgrounds[i]));
        }
    }

    private LinearLayout makeDifficultyBlock(final String difficulty, int background) {
        LinearLayout block = new LinearLayout(this);
        block.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams blockParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        blockParams.topMargin = 16;
        block.setLayoutParams(blockParams);

        boolean unlocked = GamePrefs.isDifficultyUnlocked(this, difficulty);
        Button button = new Button(this);
        button.setAllCaps(false);
        button.setTextSize(16);
        button.setTypeface(button.getTypeface(), android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (int) (56 * getResources().getDisplayMetrics().density));
        button.setLayoutParams(buttonParams);

        TextView description = new TextView(this);
        description.setTextSize(13);
        description.setPadding(8, 8, 8, 0);

        if (unlocked) {
            button.setText(difficulty);
            button.setBackgroundResource(background);
            if (background == R.drawable.bg_button_yellow) {
                button.setTextColor(ContextCompat.getColor(this, R.color.text_dark));
            } else if (background == R.drawable.bg_button_white) {
                button.setTextColor(ContextCompat.getColor(this, R.color.deep_blue));
            } else {
                button.setTextColor(ContextCompat.getColor(this, R.color.white));
            }
            description.setText(DifficultyConfig.getButtonDescription(difficulty));
            description.setTextColor(ContextCompat.getColor(this, R.color.white));
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SoundPlayer.playClick(DifficultyActivity.this);
                    openDifficulty(difficulty);
                }
            });
        } else {
            button.setText(difficulty + "  (Locked)");
            button.setBackgroundResource(R.drawable.bg_item_locked);
            button.setTextColor(ContextCompat.getColor(this, R.color.text_gray));
            description.setText("Finish the previous difficulty first.");
            description.setTextColor(ContextCompat.getColor(this, R.color.yellow_accent));
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(DifficultyActivity.this, "Finish the previous difficulty first.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        block.addView(button);
        block.addView(description);
        return block;
    }

    private void openDifficulty(String difficulty) {
        Intent intent = new Intent(this, LevelsActivity.class);
        intent.putExtra("difficulty", difficulty);
        startActivity(intent);
    }
}
