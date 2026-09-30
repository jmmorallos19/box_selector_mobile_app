package com.boxselector.app;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/** Players cannot add or edit challenges. */
public class ChallengeFormActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toast.makeText(this, "Adding and editing challenges is turned off.", Toast.LENGTH_SHORT).show();
        finish();
    }
}
