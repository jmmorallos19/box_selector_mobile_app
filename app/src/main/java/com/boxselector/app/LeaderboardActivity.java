package com.boxselector.app;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

/** Shows player names and their best scores. */
public class LeaderboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SoundPlayer.playClick(LeaderboardActivity.this);
                finish();
            }
        });

        LinearLayout list = findViewById(R.id.layoutLeaderboardList);
        TextView tvEmpty = findViewById(R.id.tvEmptyLeaderboard);
        List<GamePrefs.LeaderboardEntry> entries = GamePrefs.getLeaderboard(this);

        if (entries.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            return;
        }

        tvEmpty.setVisibility(View.GONE);
        for (int i = 0; i < entries.size(); i++) {
            GamePrefs.LeaderboardEntry entry = entries.get(i);
            list.addView(makeRow((i + 1) + ".  " + entry.username, entry.difficulty, String.valueOf(entry.score)));
        }
    }

    private LinearLayout makeRow(String name, String difficulty, String score) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setBackgroundResource(R.drawable.bg_card);
        row.setPadding(24, 28, 24, 28);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = 16;
        row.setLayoutParams(params);

        row.addView(makeCell(name, 1f, false));
        row.addView(makeCell(difficulty, 0f, false));
        row.addView(makeCell(score, 0f, true));
        return row;
    }

    private TextView makeCell(String text, float weight, boolean endAlign) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextColor(getResources().getColor(R.color.text_dark));
        textView.setTextSize(15);
        textView.setPadding(0, 0, 8, 0);
        LinearLayout.LayoutParams params;
        if (weight > 0) {
            params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weight);
        } else if (endAlign) {
            params = new LinearLayout.LayoutParams(180, LinearLayout.LayoutParams.WRAP_CONTENT);
            textView.setGravity(android.view.Gravity.END);
        } else {
            params = new LinearLayout.LayoutParams(220, LinearLayout.LayoutParams.WRAP_CONTENT);
        }
        textView.setLayoutParams(params);
        return textView;
    }
}
