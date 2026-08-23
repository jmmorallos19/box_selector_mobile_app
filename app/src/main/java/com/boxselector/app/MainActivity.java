package com.boxselector.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/** Home screen: shows coins and opens Play, Shop, Collection, and other menus. */
public class MainActivity extends AppCompatActivity {

    private TextView tvCoins;
    private Button btnPlay;
    private Button btnLevels;
    private Button btnShop;
    private Button btnCollection;
    private Button btnAchievements;
    private ImageButton btnSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvCoins = findViewById(R.id.tvCoins);
        btnPlay = findViewById(R.id.btnPlay);
        btnLevels = findViewById(R.id.btnLevels);
        btnShop = findViewById(R.id.btnShop);
        btnCollection = findViewById(R.id.btnCollection);
        btnAchievements = findViewById(R.id.btnAchievements);
        btnSettings = findViewById(R.id.btnSettings);

        // PLAY opens the game. We send the current unlocked level.
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, GameplayActivity.class);
                intent.putExtra("level", GamePrefs.getHighestLevel(MainActivity.this));
                startActivity(intent);
            }
        });

        btnLevels.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, LevelsActivity.class));
            }
        });

        btnShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ShopActivity.class));
            }
        });

        btnCollection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, CollectionActivity.class));
            }
        });

        btnAchievements.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, AchievementsActivity.class));
            }
        });

        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        tvCoins.setText(String.valueOf(GamePrefs.getCoins(this)));
    }
}
