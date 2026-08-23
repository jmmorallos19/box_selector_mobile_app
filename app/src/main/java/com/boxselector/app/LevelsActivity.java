package com.boxselector.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class LevelsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_levels);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        int highestLevel = GamePrefs.getHighestLevel(this);
        int[] buttonIds = {
                R.id.btnLevel1, R.id.btnLevel2, R.id.btnLevel3,
                R.id.btnLevel4, R.id.btnLevel5, R.id.btnLevel6,
                R.id.btnLevel7, R.id.btnLevel8, R.id.btnLevel9
        };

        for (int i = 0; i < buttonIds.length; i++) {
            final int levelNumber = i + 1;
            Button levelButton = findViewById(buttonIds[i]);

            if (levelNumber <= highestLevel) {
                levelButton.setBackgroundResource(R.drawable.bg_button_green);
                levelButton.setTextColor(ContextCompat.getColor(LevelsActivity.this, R.color.white));
                levelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(LevelsActivity.this, GameplayActivity.class);
                        intent.putExtra("level", levelNumber);
                        startActivity(intent);
                    }
                });
            } else {
                levelButton.setBackgroundResource(R.drawable.bg_item_locked);
                levelButton.setTextColor(ContextCompat.getColor(LevelsActivity.this, R.color.text_gray));
                levelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(LevelsActivity.this, "Level locked!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }
}
