package com.cohen.myfinalgame;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ShopActivity extends AppCompatActivity {

    private Button backToGameButton, buyDoublePointsButton, buyExtraTimeButton, buyExtraLifeButton, buyTriplePointsButton;
    private TextView coinsText;
    private int coins;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);

        // Initialize UI components
        backToGameButton = findViewById(R.id.backToGameButton);
        buyDoublePointsButton = findViewById(R.id.buyDoublePointsButton);
        buyExtraTimeButton = findViewById(R.id.buyExtraTimeButton);
        buyExtraLifeButton = findViewById(R.id.buyExtraLifeButton);
        buyTriplePointsButton = findViewById(R.id.buyTriplePointsButton);
        coinsText = findViewById(R.id.coinsText);

        prefs = getSharedPreferences("game_prefs", MODE_PRIVATE);
        coins = prefs.getInt("coins", 0);
        updateCoinsText();

        // Back to Game button
        backToGameButton.setOnClickListener(v -> {
            Intent intent = new Intent(ShopActivity.this, GameActivity.class);
            startActivity(intent);
            finish();
        });

        // Buy power-ups
        buyDoublePointsButton.setOnClickListener(v -> purchasePowerUp("doublePoints", 150, "Double Points Activated!"));
        buyExtraTimeButton.setOnClickListener(v -> purchasePowerUp("extraTimePurchased", 100, "Extra Time Purchased!"));
        buyExtraLifeButton.setOnClickListener(v -> purchasePowerUp("extraLife", 120, "Extra Life Purchased!"));
        buyTriplePointsButton.setOnClickListener(v -> purchasePowerUp("triplePoints", 200, "Triple Points Activated!"));
    }

    private void purchasePowerUp(String powerUpKey, int cost, String successMessage) {
        if (coins >= cost) {
            coins -= cost;
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("coins", coins);
            editor.putBoolean(powerUpKey, true);
            editor.apply();
            updateCoinsText();
            Toast.makeText(this, successMessage, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Not enough coins!", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateCoinsText() {
        coinsText.setText("Coins: " + coins);
    }
}
