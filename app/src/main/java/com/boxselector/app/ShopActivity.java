package com.boxselector.app;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/** Shop screen: player spends coins to buy Hint, Extra Time, or Box Reveal. */
public class ShopActivity extends AppCompatActivity {

    private TextView tvCoins;
    private Button btnBuyHint;
    private Button btnBuyExtraTime;
    private Button btnBuyBoxReveal;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);

        tvCoins = findViewById(R.id.tvCoins);
        btnBuyHint = findViewById(R.id.btnBuyHint);
        btnBuyExtraTime = findViewById(R.id.btnBuyExtraTime);
        btnBuyBoxReveal = findViewById(R.id.btnBuyBoxReveal);
        btnBack = findViewById(R.id.btnBack);

        updateCoinsText();

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnBuyHint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buyItem(50, "Hint");
            }
        });

        btnBuyExtraTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buyItem(100, "Extra Time");
            }
        });

        btnBuyBoxReveal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buyItem(150, "Box Reveal");
            }
        });
    }

    /** Deducts coins and saves the purchased power-up if the player can afford it. */
    private void buyItem(int cost, String itemName) {
        if (GamePrefs.spendCoins(this, cost)) {
            if (itemName.equals("Hint")) {
                GamePrefs.addHint(this);
            } else if (itemName.equals("Extra Time")) {
                GamePrefs.addExtraTime(this);
            } else if (itemName.equals("Box Reveal")) {
                GamePrefs.addBoxReveal(this);
            }
            updateCoinsText();
            Toast.makeText(this, "Purchased " + itemName + "!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Not enough coins!", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateCoinsText() {
        tvCoins.setText(String.valueOf(GamePrefs.getCoins(this)));
    }
}
