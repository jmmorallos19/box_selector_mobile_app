package com.boxselector.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

/** Shows every saved challenge and its difficulty. */
public class ChallengeListActivity extends AppCompatActivity {

    private LinearLayout layoutChallengeList;
    private TextView tvEmptyChallenges;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge_list);

        layoutChallengeList = findViewById(R.id.layoutChallengeList);
        tvEmptyChallenges = findViewById(R.id.tvEmptyChallenges);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SoundPlayer.playClick(ChallengeListActivity.this);
                finish();
            }
        });

        Button btnCreate = findViewById(R.id.btnCreateChallenge);
        btnCreate.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        showChallenges();
    }

    private void showChallenges() {
        layoutChallengeList.removeAllViews();
        List<Challenge> challenges = GamePrefs.getChallenges(this);

        if (challenges.isEmpty()) {
            tvEmptyChallenges.setVisibility(View.VISIBLE);
            return;
        }

        tvEmptyChallenges.setVisibility(View.GONE);
        for (int i = 0; i < challenges.size(); i++) {
            layoutChallengeList.addView(makeRow(challenges.get(i)));
        }
    }

    private LinearLayout makeRow(final Challenge challenge) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setBackgroundResource(R.drawable.bg_card);
        row.setPadding(32, 28, 32, 28);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = 16;
        row.setLayoutParams(params);

        TextView tvTitle = new TextView(this);
        tvTitle.setText(challenge.title);
        tvTitle.setTextColor(getResources().getColor(R.color.text_dark));
        tvTitle.setTextSize(18);
        tvTitle.setTypeface(tvTitle.getTypeface(), android.graphics.Typeface.BOLD);

        TextView tvDifficulty = new TextView(this);
        tvDifficulty.setText("Difficulty: " + challenge.difficulty);
        tvDifficulty.setTextColor(getResources().getColor(R.color.text_gray));
        tvDifficulty.setTextSize(14);
        tvDifficulty.setPadding(0, 8, 0, 0);

        row.addView(tvTitle);
        row.addView(tvDifficulty);
        row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SoundPlayer.playClick(ChallengeListActivity.this);
                Intent intent = new Intent(ChallengeListActivity.this, ChallengeDetailActivity.class);
                intent.putExtra("challengeId", challenge.id);
                startActivity(intent);
            }
        });
        return row;
    }
}
