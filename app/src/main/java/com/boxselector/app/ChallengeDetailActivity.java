package com.boxselector.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/** Challenge details, including the saved difficulty. */
public class ChallengeDetailActivity extends AppCompatActivity {

    private Challenge challenge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge_detail);

        int challengeId = getIntent().getIntExtra("challengeId", 0);
        challenge = GamePrefs.getChallengeById(this, challengeId);
        if (challenge == null) {
            Toast.makeText(this, "Challenge not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ImageButton btnBack = findViewById(R.id.btnBack);
        TextView tvTitle = findViewById(R.id.tvChallengeTitle);
        TextView tvDifficulty = findViewById(R.id.tvChallengeDifficulty);
        TextView tvDescription = findViewById(R.id.tvChallengeDescription);
        TextView tvAuthor = findViewById(R.id.tvChallengeAuthor);
        Button btnPlay = findViewById(R.id.btnPlayChallenge);
        Button btnEdit = findViewById(R.id.btnEditChallenge);
        Button btnDelete = findViewById(R.id.btnDeleteChallenge);

        tvTitle.setText(challenge.title);
        tvDifficulty.setText(challenge.difficulty);
        if (challenge.description.isEmpty()) {
            tvDescription.setText("No description.");
        } else {
            tvDescription.setText(challenge.description);
        }
        tvAuthor.setText("Created by " + challenge.createdBy);

        btnEdit.setVisibility(View.GONE);
        btnDelete.setVisibility(View.GONE);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SoundPlayer.playClick(ChallengeDetailActivity.this);
                finish();
            }
        });

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SoundPlayer.playClick(ChallengeDetailActivity.this);
                Intent intent = new Intent(ChallengeDetailActivity.this, GameplayActivity.class);
                intent.putExtra("difficulty", challenge.toGameplayDifficulty());
                intent.putExtra("stage", 1);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (challenge == null) {
            return;
        }
        Challenge updated = GamePrefs.getChallengeById(this, challenge.id);
        if (updated == null) {
            finish();
            return;
        }
        challenge = updated;
        TextView tvTitle = findViewById(R.id.tvChallengeTitle);
        TextView tvDifficulty = findViewById(R.id.tvChallengeDifficulty);
        TextView tvDescription = findViewById(R.id.tvChallengeDescription);
        tvTitle.setText(challenge.title);
        tvDifficulty.setText(challenge.difficulty);
        tvDescription.setText(challenge.description.isEmpty() ? "No description." : challenge.description);
    }
}
