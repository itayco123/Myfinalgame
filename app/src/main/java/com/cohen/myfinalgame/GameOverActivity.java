package com.cohen.myfinalgame;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class GameOverActivity extends Activity {

    private TextView finalScoreText, highScoreText, coinsText;
    private Button playAgainButton, shopButton, backToMainButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_over);

        // Initialize UI components
        finalScoreText = findViewById(R.id.finalScoreText);
        highScoreText = findViewById(R.id.highScoreText);
        coinsText = findViewById(R.id.coinsText);
        playAgainButton = findViewById(R.id.playAgainButton);
        shopButton = findViewById(R.id.shopButton);
        backToMainButton = findViewById(R.id.backToMainButton);

        // Get the score from the Intent
        int score = getIntent().getIntExtra("score", 0);
        finalScoreText.setText("Final Score: " + score);

        // Retrieve high score and total coins from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("game_prefs", MODE_PRIVATE);
        int highScore = prefs.getInt("highScore", 0);
        int totalCoins = prefs.getInt("coins", 0);

        // For testing, we record every score.
        // (If you prefer to save only new high scores, move this call into the if-block below.)
        saveHighScore(score);

        // Update high score if necessary
        if (score > highScore) {
            highScore = score;
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("highScore", highScore);
            editor.apply();
            Log.d("LeaderboardDebug", "New high score detected: " + highScore);
        }

        // Convert score to coins (1 point = 1 coin)
        totalCoins += score;
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("coins", totalCoins);
        editor.apply();

        // Display updated high score and coins
        highScoreText.setText("High Score: " + highScore);
        coinsText.setText("Total Coins: " + totalCoins);

        // Button Listeners

        playAgainButton.setOnClickListener(v -> {
            Intent intent = new Intent(GameOverActivity.this, GameActivity.class);
            startActivity(intent);
            finish();
        });


        shopButton.setOnClickListener(v -> {
            Intent intent = new Intent(GameOverActivity.this, ShopActivity.class);
            startActivity(intent);
        });

        backToMainButton.setOnClickListener(v -> {
            Intent intent = new Intent(GameOverActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });


    }

    // Save score to Firebase under the "leaderboard" node
    private void saveHighScore(int score) {
        Log.d("LeaderboardDebug", "🔥 saveHighScore() called with score: " + score);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference scoresRef = database.getReference("leaderboard");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String username = (user != null && user.getDisplayName() != null) ? user.getDisplayName() : "Guest";


        HashMap<String, Object> scoreData = new HashMap<>();
        scoreData.put("username", username);
        scoreData.put("score", score);

        scoresRef.push().setValue(scoreData)
                .addOnSuccessListener(aVoid -> Log.d("LeaderboardDebug", "✅ Score added successfully!"))
                .addOnFailureListener(e -> Log.e("LeaderboardDebug", "❌ Failed to add score", e));
    }
}
