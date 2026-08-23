package com.boxselector.app;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Switch;
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

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        switchSound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (switchSound.isChecked()) {
                    Toast.makeText(SettingsActivity.this, "Sound on", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SettingsActivity.this, "Sound off", Toast.LENGTH_SHORT).show();
                }
            }
        });

        switchMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (switchMusic.isChecked()) {
                    Toast.makeText(SettingsActivity.this, "Music on", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SettingsActivity.this, "Music off", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
