package com.boxselector.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ImageButton btnBack = findViewById(R.id.btnBack);
        Switch switchSound = findViewById(R.id.switchSound);
        Switch switchMusic = findViewById(R.id.switchMusic);
        TextView tvAbout = findViewById(R.id.tvAbout);
        Button btnLogout = findViewById(R.id.btnLogout);

        switchSound.setChecked(GamePrefs.isSoundOn(this));
        switchMusic.setChecked(GamePrefs.isMusicOn(this));
        tvAbout.setText("Logged in as " + GamePrefs.getUsername(this)
                + "\nBest score: " + GamePrefs.getBestScore(this)
                + "\nBox Selector  •  College Project");

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SoundPlayer.playClick(SettingsActivity.this);
                finish();
            }
        });

        switchSound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GamePrefs.setSoundOn(SettingsActivity.this, switchSound.isChecked());
                Toast.makeText(SettingsActivity.this,
                        switchSound.isChecked() ? "Sound on" : "Sound off",
                        Toast.LENGTH_SHORT).show();
            }
        });

        switchMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GamePrefs.setMusicOn(SettingsActivity.this, switchMusic.isChecked());
                Toast.makeText(SettingsActivity.this,
                        switchMusic.isChecked() ? "Music on" : "Music off",
                        Toast.LENGTH_SHORT).show();
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SoundPlayer.playClick(SettingsActivity.this);
                GamePrefs.logout(SettingsActivity.this);
                Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }
}
