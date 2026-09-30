package com.boxselector.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/** Home screen after login. PLAY opens difficulty select. */
public class MainActivity extends AppCompatActivity {

    private TextView tvCoins;
    private TextView tvUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!GamePrefs.isLoggedIn(this)) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);
        tvCoins = findViewById(R.id.tvCoins);
        tvUsername = findViewById(R.id.tvUsername);
        Button btnPlay = findViewById(R.id.btnPlay);
        Button btnChallenges = findViewById(R.id.btnChallenges);
        Button btnLevels = findViewById(R.id.btnLevels);
        Button btnShop = findViewById(R.id.btnShop);
        Button btnCollection = findViewById(R.id.btnCollection);
        Button btnLeaderboard = findViewById(R.id.btnLeaderboard);
        ImageButton btnSettings = findViewById(R.id.btnSettings);

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SoundPlayer.playClick(MainActivity.this);
                Intent intent = new Intent(MainActivity.this, DifficultyActivity.class);
                intent.putExtra("selectStage", false);
                startActivity(intent);
            }
        });

        btnChallenges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SoundPlayer.playClick(MainActivity.this);
                startActivity(new Intent(MainActivity.this, ChallengeListActivity.class));
            }
        });

        btnLevels.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SoundPlayer.playClick(MainActivity.this);
                Intent intent = new Intent(MainActivity.this, DifficultyActivity.class);
                intent.putExtra("selectStage", true);
                startActivity(intent);
            }
        });

        btnShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SoundPlayer.playClick(MainActivity.this);
                startActivity(new Intent(MainActivity.this, ShopActivity.class));
            }
        });

        btnCollection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SoundPlayer.playClick(MainActivity.this);
                startActivity(new Intent(MainActivity.this, CollectionActivity.class));
            }
        });

        btnLeaderboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SoundPlayer.playClick(MainActivity.this);
                startActivity(new Intent(MainActivity.this, LeaderboardActivity.class));
            }
        });

        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SoundPlayer.playClick(MainActivity.this);
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (tvCoins != null) {
            tvCoins.setText(String.valueOf(GamePrefs.getCoins(this)));
        }
        if (tvUsername != null) {
            tvUsername.setText(GamePrefs.getUsername(this));
        }
    }
}
