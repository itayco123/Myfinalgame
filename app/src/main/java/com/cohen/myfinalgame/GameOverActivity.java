package com.cohen.myfinalgame;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Button;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import java.util.HashMap;
import android.util.Log;


public class GameOverActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_over);

        // Find UI elements
        TextView finalScoreText = findViewById(R.id.finalScoreText);
        TextView highScoreText = findViewById(R.id.highScoreText);
        TextView coinsText = findViewById(R.id.coinsText);

        // Get the score from the Intent
        int score = getIntent().getIntExtra("score", 0);
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

        // Convert score to coins
        totalCoins += score;
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("coins", totalCoins);
        editor.apply();

        highScoreText.setText("High Score: " + highScore);
        coinsText.setText("Total Coins: " + totalCoins);

        // Buttons
        Button playAgainButton = findViewById(R.id.playAgainButton);
        Button exitButton = findViewById(R.id.exitButton);
        Button shopButton = findViewById(R.id.shopButton);
        Button backToMainButton = findViewById(R.id.backToMainButton);

        playAgainButton.setOnClickListener(v -> {
            startActivity(new Intent(GameOverActivity.this, GameActivity.class));
            finish();
        });

        exitButton.setOnClickListener(v -> finishAffinity());

        shopButton.setOnClickListener(v -> {
            startActivity(new Intent(GameOverActivity.this, ShopActivity.class));
        });



        backToMainButton.setOnClickListener(v -> {
            startActivity(new Intent(GameOverActivity.this, MainActivity.class));
            finish();
        });
    }

    // Save High Score to Firebase
    private void saveHighScore(int score) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference scoresRef = database.getReference("leaderboard");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = (user != null) ? user.getUid() : "guest_" + System.currentTimeMillis();

        HashMap<String, Object> scoreData = new HashMap<>();
        scoreData.put("username", userId); // You should replace this with an actual username
        scoreData.put("score", score);

        scoresRef.push().setValue(scoreData)
                .addOnSuccessListener(aVoid -> Log.d("Firebase", "✅ Score added successfully!"))
                .addOnFailureListener(e -> Log.e("Firebase", "❌ Failed to add score", e));
    }



}
