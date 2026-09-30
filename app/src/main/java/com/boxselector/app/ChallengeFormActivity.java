package com.boxselector.app;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/** Create Challenge and Edit Challenge use the same form. */
public class ChallengeFormActivity extends AppCompatActivity {

    private EditText etTitle;
    private EditText etDescription;
    private Spinner spDifficulty;
    private int challengeId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge_form);

        etTitle = findViewById(R.id.etChallengeTitle);
        etDescription = findViewById(R.id.etChallengeDescription);
        spDifficulty = findViewById(R.id.spDifficulty);
        TextView tvFormTitle = findViewById(R.id.tvFormTitle);
        Button btnSave = findViewById(R.id.btnSaveChallenge);
        ImageButton btnBack = findViewById(R.id.btnBack);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                Challenge.difficulties()
        );
        spDifficulty.setAdapter(adapter);

        challengeId = getIntent().getIntExtra("challengeId", 0);
        if (challengeId > 0) {
            tvFormTitle.setText(R.string.edit_challenge);
            fillForm(GamePrefs.getChallengeById(this, challengeId));
        } else {
            tvFormTitle.setText(R.string.create_challenge);
        }

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SoundPlayer.playClick(ChallengeFormActivity.this);
                finish();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SoundPlayer.playClick(ChallengeFormActivity.this);
                saveForm();
            }
        });
    }

    private void fillForm(Challenge challenge) {
        if (challenge == null) {
            Toast.makeText(this, "Challenge not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        etTitle.setText(challenge.title);
        etDescription.setText(challenge.description);
        String[] options = Challenge.difficulties();
        for (int i = 0; i < options.length; i++) {
            if (options[i].equals(challenge.difficulty)) {
                spDifficulty.setSelection(i);
                break;
            }
        }
    }

    private void saveForm() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String difficulty = (String) spDifficulty.getSelectedItem();

        if (title.isEmpty()) {
            Toast.makeText(this, "Please enter a title.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Challenge.isValidDifficulty(difficulty)) {
            Toast.makeText(this, "Pick a valid difficulty.", Toast.LENGTH_SHORT).show();
            return;
        }

        Challenge challenge = new Challenge();
        challenge.id = challengeId;
        challenge.title = title;
        challenge.description = description;
        challenge.difficulty = difficulty;
        challenge.createdBy = GamePrefs.getUsername(this);

        Challenge existing = GamePrefs.getChallengeById(this, challengeId);
        if (existing != null) {
            challenge.createdBy = existing.createdBy;
        }

        Challenge saved = GamePrefs.saveChallenge(this, challenge);
        if (saved == null) {
            Toast.makeText(this, "Could not save. Check the difficulty value.", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Challenge saved!", Toast.LENGTH_SHORT).show();
        finish();
    }
}
