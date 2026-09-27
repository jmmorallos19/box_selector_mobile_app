package com.boxselector.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/** First screen: the player creates a username or logs in. */
public class LoginActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (GamePrefs.isLoggedIn(this)) {
            openHome();
            return;
        }

        setContentView(R.layout.activity_login);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnCreateAccount = findViewById(R.id.btnCreateAccount);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SoundPlayer.playClick(LoginActivity.this);
                loginAccount();
            }
        });

        btnCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SoundPlayer.playClick(LoginActivity.this);
                createAccount();
            }
        });
    }

    private void loginAccount() {
        String username = etUsername.getText().toString();
        String password = etPassword.getText().toString();
        if (GamePrefs.login(this, username, password)) {
            openHome();
        } else {
            Toast.makeText(this, "Wrong username or password.", Toast.LENGTH_SHORT).show();
        }
    }

    private void createAccount() {
        String username = etUsername.getText().toString();
        String password = etPassword.getText().toString();
        if (username.trim().isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Enter a username and password.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (GamePrefs.userExists(this, username)) {
            Toast.makeText(this, "That username is already taken.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (GamePrefs.createAccount(this, username, password)) {
            Toast.makeText(this, "Account created!", Toast.LENGTH_SHORT).show();
            openHome();
        }
    }

    private void openHome() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
