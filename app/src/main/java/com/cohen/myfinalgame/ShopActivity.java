package com.cohen.myfinalgame;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ShopActivity extends AppCompatActivity {

    private Button backToGameButton, buyDoublePointsButton, buyExtraTimeButton;
    private TextView coinsText;
    private int coins;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);

        // Initialize UI components
        backToGameButton = findViewById(R.id.backToGameButton);
        buyDoublePointsButton = findViewById(R.id.buyDoublePointsButton);
        buyExtraTimeButton = findViewById(R.id.buyExtraTimeButton);
        coinsText = findViewById(R.id.coinsText);

        // Get the current number of coins from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("game_prefs", MODE_PRIVATE);
        coins = prefs.getInt("coins", 0);
        coinsText.setText("Coins: " + coins);

        // Back to Game button
        backToGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShopActivity.this, GameActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Buy Double Points button
        buyDoublePointsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (coins >= 150) {
                    coins -= 150;
                    updateCoins();

                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("doublePoints", true);
                    editor.apply();

                    Toast.makeText(ShopActivity.this, "Double Points Activated!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ShopActivity.this, "Not enough coins!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Buy Extra Time button
        // Buy Extra Time button click
        buyExtraTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (coins >= 100) { // Check if the player has enough coins
                    coins -= 100; // Deduct coins for the purchase
                    updateCoins(); // Update the displayed coin balance

                    // Save extra time effect in SharedPreferences
                    SharedPreferences prefs = getSharedPreferences("game_prefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("extraTimePurchased", true); // Store that extra time was bought
                    editor.apply();

                    Toast.makeText(ShopActivity.this, "Extra Time Purchased!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ShopActivity.this, "Not enough coins!", Toast.LENGTH_SHORT).show();
                }
            }
        });


    } // <-- Make sure this closing brace is here!

    // ✅ This method should be OUTSIDE onCreate()!
    private void updateCoins() {
        SharedPreferences prefs = getSharedPreferences("game_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("coins", coins);
        editor.apply();

        coinsText.setText("Coins: " + coins);
    }
}

