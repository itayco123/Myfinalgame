package com.cohen.myfinalgame;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import androidx.gridlayout.widget.GridLayout;
import androidx.gridlayout.widget.GridLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;

public class GameActivity extends AppCompatActivity {

    private TextView timerText, scoreText;
    private GridLayout gridLayout;
    private Button[][] gridButtons = new Button[3][3];
    private int score = 0;
    private CountDownTimer mainTimer, popUpTimer;
    private long timeLeft = 30000; // Default 30 seconds
    private Random random = new Random();

    // Game settings
    SharedPreferences prefs;
    int totalCoins;
    boolean isDoublePointsActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // Initialize UI elements
        timerText = findViewById(R.id.timerText);
        scoreText = findViewById(R.id.scoreText);
        gridLayout = findViewById(R.id.gridLayout);

        // Load game preferences
        prefs = getSharedPreferences("game_prefs", MODE_PRIVATE);
        totalCoins = prefs.getInt("coins", 0);

        // Load power-ups
        boolean hasDoublePoints = prefs.getBoolean("doublePoints", false);
        boolean hasExtraTime = prefs.getBoolean("extraTimePurchased", false);
        timeLeft = (hasExtraTime ? 35 : 30) * 1000; // Apply extra time if purchased

        // Reset power-ups after using
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("extraTimePurchased", false);
        editor.putBoolean("doublePoints", false);
        editor.apply();

        // Activate Double Points if purchased
        isDoublePointsActive = hasDoublePoints;

        // Set up the grid buttons dynamically
        setupGrid();

        // Start the game timer
        startGame();
    }

    private void setupGrid() {
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int buttonSize = screenWidth / 4; // Bigger buttons (1/4th of screen width)

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                Button button = new Button(this);

                // Set button size and margins
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = buttonSize;
                params.height = buttonSize;
                params.setMargins(24, 24, 24, 24); // Increase margin for spacing
                button.setLayoutParams(params);

                // Set default appearance
                button.setBackgroundResource(android.R.color.transparent); // Default background
                button.setTag(new int[]{i, j}); // Store button position
                button.setOnClickListener(this::handleButtonClick);

                gridLayout.addView(button);
                gridButtons[i][j] = button;
            }
        }
    }


    private void startGame() {
        mainTimer = new CountDownTimer(timeLeft, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeft = millisUntilFinished;
                timerText.setText("Time: " + timeLeft / 1000);
            }

            @Override
            public void onFinish() {
                endGame();
            }
        }.start();

        startPopUpTimer();
    }

    private void startPopUpTimer() {
        popUpTimer = new CountDownTimer(timeLeft, 2000) { // Spawns every 2 seconds
            @Override
            public void onTick(long millisUntilFinished) {
                popSquare();
            }

            @Override
            public void onFinish() {
                // Main timer will stop the game
            }
        }.start();
    }

    private void popSquare() {
        // Clear all buttons
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                gridButtons[i][j].setBackgroundResource(android.R.color.transparent); // Reset
                gridButtons[i][j].setEnabled(false);
            }
        }

        // Choose a random button to activate
        int i = random.nextInt(3);
        int j = random.nextInt(3);
        Button randomButton = gridButtons[i][j];
        randomButton.setEnabled(true);

        // Randomly assign square type with images
        int squareType = random.nextInt(10);
        if (squareType < 7) {
            randomButton.setBackgroundResource(R.drawable.point1); // +1 image
        } else if (squareType == 7) {
            randomButton.setBackgroundResource(R.drawable.time_bonus); // +3 time image
        } else if (squareType == 8) {
            randomButton.setBackgroundResource(R.drawable.point2); // +2 image
        } else {
            randomButton.setBackgroundResource(R.drawable.bomb); // Bomb image
        }
    }


    private void handleButtonClick(View view) {
        Button clickedButton = (Button) view;

        // Check which image is used and assign points correctly
        if (clickedButton.getBackground().getConstantState() == getResources().getDrawable(R.drawable.bomb).getConstantState()) {
            // 💣 Bomb → End Game
            mainTimer.cancel();
            popUpTimer.cancel();
            endGame();
        } else if (clickedButton.getBackground().getConstantState() == getResources().getDrawable(R.drawable.time_bonus).getConstantState()) {
            // ⏳ +3s → Add 3 seconds to time
            timeLeft += 3000;
            mainTimer.cancel();
            startGame(); // Restart timer with added time
        } else if (clickedButton.getBackground().getConstantState() == getResources().getDrawable(R.drawable.point2).getConstantState()) {
            // 🟢 +2 Points
            score += isDoublePointsActive ? 4 : 2;
        } else {
            // 🔵 +1 Point (Default)
            score += isDoublePointsActive ? 2 : 1;
        }

        // Update Score Display
        scoreText.setText("Score: " + score);

        // Reset button and pop a new one
        popSquare();
    }


    private void endGame() {
        if (mainTimer != null) mainTimer.cancel();
        if (popUpTimer != null) popUpTimer.cancel();

        // Save earned coins
        totalCoins += score;
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("coins", totalCoins);
        editor.apply();

        // Navigate to Game Over screen
        Intent intent = new Intent(GameActivity.this, GameOverActivity.class);
        intent.putExtra("score", score);
        startActivity(intent);
        finish();
    }
}
