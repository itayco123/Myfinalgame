package com.cohen.myfinalgame;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Button;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;

public class GameOverActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_over);

        // Find the TextViews
        TextView finalScoreText = findViewById(R.id.finalScoreText);
        TextView highScoreText = findViewById(R.id.highScoreText);
        TextView coinsText = findViewById(R.id.coinsText);

        // Get the score from the Intent
        int score = getIntent().getIntExtra("score", 0);

        // Display the score
        finalScoreText.setText("Final Score: " + score);

        // Retrieve high score and total coins from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("game_prefs", MODE_PRIVATE);
        int highScore = prefs.getInt("highScore", 0);
        int totalCoins = prefs.getInt("coins", 0);

        // Update high score if necessary
        if (score > highScore) {
            highScore = score;
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("highScore", highScore);
            editor.apply();

            // Save new high score to Firebase
            saveHighScore(highScore);
        }

        // Convert score to coins (1 point = 1 coin)
        totalCoins += score; // Add current score to total coins
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("coins", totalCoins);
        editor.apply();

        // Display updated high score and coins
        highScoreText.setText("High Score: " + highScore);
        coinsText.setText("Total Coins: " + totalCoins);

        // Play Again Button
        Button playAgainButton = findViewById(R.id.playAgainButton);
        playAgainButton.setOnClickListener(v -> {
            Intent intent = new Intent(GameOverActivity.this, GameActivity.class);
            startActivity(intent);
            finish();
        });

        // Exit Button
        Button exitButton = findViewById(R.id.exitButton);
        exitButton.setOnClickListener(v -> finishAffinity());

        // Shop Button
        Button shopButton = findViewById(R.id.shopButton);
        shopButton.setOnClickListener(v -> {
            Intent intent = new Intent(GameOverActivity.this, ShopActivity.class);
            startActivity(intent);
        });
        // Back to Main Menu Button
        Button backToMainButton = findViewById(R.id.backToMainButton);
        backToMainButton.setOnClickListener(v -> {
            Intent intent = new Intent(GameOverActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Close GameOverActivity to prevent going back with the back button
        });


    }


    // Save high score to Firebase
    private void saveHighScore(int score) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference scoresRef = database.getReference("leaderboard");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = (user != null) ? user.getUid() : "guest_" + System.currentTimeMillis();

        HashMap<String, Object> scoreData = new HashMap<>();
        scoreData.put("username", userId); // Later, we can replace this with actual usernames
        scoreData.put("score", score);

        scoresRef.push().setValue(scoreData);
    }
}
