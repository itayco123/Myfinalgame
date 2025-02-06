package com.cohen.myfinalgame;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class GameActivity extends AppCompatActivity {

    private TextView timerText, scoreText;
    private GridLayout gridLayout;
    private Button[][] gridButtons = new Button[3][3];
    private int score = 0;
    private CountDownTimer mainTimer, popUpTimer;
    private long timeLeft = 30000; // 30 seconds
    private Random random = new Random();

    // Add SharedPreferences for coins and item status
    SharedPreferences prefs;
    int totalCoins;
     boolean isDoublePointsActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // Initialize UI components
        timerText = findViewById(R.id.timerText);
        scoreText = findViewById(R.id.scoreText);
        gridLayout = findViewById(R.id.gridLayout);

        // Load total coins from SharedPreferences
        prefs = getSharedPreferences("game_prefs", MODE_PRIVATE);
        totalCoins = prefs.getInt("coins", 0);

        // Dynamically add buttons to the grid
        setupGrid();

        // Load power-ups from SharedPreferences
        boolean hasDoublePoints = prefs.getBoolean("doublePoints", false);
        boolean extraTime = prefs.getBoolean("extraTimePurchased", false);

        // Set timer to 35 seconds if extra time was bought, otherwise 30 seconds
        timeLeft = (extraTime ? 35 : 30) * 1000;

        // Reset the power-up after use so it doesn't apply next time automatically
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("extraTimePurchased", false);
        editor.apply();

        // Apply Double Points (if bought)
        if (hasDoublePoints) {
            isDoublePointsActive = true;
            prefs.edit().putBoolean("doublePoints", false).apply(); // Reset after use
        }

        // **🔥 Start the game timer AFTER updating timeLeft**
        startGame();
    }


    private void setupGrid() {
        int buttonSize = getResources().getDisplayMetrics().widthPixels / 4; // Adjust size based on screen width

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                Button button = new Button(this);

                // Set button size and margins
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = buttonSize;
                params.height = buttonSize;
                params.setMargins(16, 16, 16, 16); // Margin between buttons
                button.setLayoutParams(params);

                // Set default appearance
                button.setTextSize(20);
                button.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
                button.setTag(new int[]{i, j}); // Store button position
                button.setOnClickListener(this::handleButtonClick);

                gridLayout.addView(button);
                gridButtons[i][j] = button;
            }
        }
    }

    private void startGame() {
        // Start the main countdown timer
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

        // Start the square pop-up timer with a 3-second interval
        startPopUpTimer();
    }

    private void startPopUpTimer() {

        
        popUpTimer = new CountDownTimer(timeLeft, 3000) {
            @Override
            public void onTick(long millisUntilFinished) {
                popSquare(); // Pops a new square every 3 seconds
            }

            @Override
            public void onFinish() {
                // No action needed here since the main timer handles the game end
            }
        }.start();
    }

    private void popSquare() {
        // Clear all buttons
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                gridButtons[i][j].setText("");
                gridButtons[i][j].setEnabled(false);
            }
        }

        // Choose a random button to activate
        int i = random.nextInt(3);
        int j = random.nextInt(3);

        Button randomButton = gridButtons[i][j];
        randomButton.setEnabled(true);

        // Randomly assign square type
        int squareType = random.nextInt(10);
        if (squareType < 7) {
            randomButton.setText("1"); // Normal square
        } else if (squareType == 7) {
            randomButton.setText("+3"); // Add 3 seconds
        } else if (squareType == 8) {
            randomButton.setText("2"); // Two-point square
        } else {
            randomButton.setText("💣"); // Bomb square
        }
    }

    private void handleButtonClick(View view) {
        Button clickedButton = (Button) view;
        String squareType = clickedButton.getText().toString();

        // Load double points status from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("game_prefs", MODE_PRIVATE);
        boolean hasDoublePoints = prefs.getBoolean("doublePoints", false);

        if (squareType.equals("💣")) {
            mainTimer.cancel();
            popUpTimer.cancel();
            endGame();
        } else if (squareType.equals("+3")) {
            timeLeft += 3000;
            mainTimer.cancel();
            startGame();
        } else if (squareType.equals("2")) {
            score += hasDoublePoints ? 4 : 2; // Double points effect
        } else {
            score += hasDoublePoints ? 2 : 1; // Double points effect
        }
        if (isDoublePointsActive) {
            score += 1; // Double the points

        }

        // Reset double points after first game use
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("doublePoints", false);
        editor.apply();

        scoreText.setText("Score: " + score);
        popSquare();

    }


    private void endGame() {
        // Stop both timers
        if (mainTimer != null) mainTimer.cancel();
        if (popUpTimer != null) popUpTimer.cancel();

        // Add earned coins
        totalCoins += score; // Reward player with coins equal to their score

        // Save total coins to SharedPreferences
        // After the game ends, add the score as coins
        int earnedCoins = score; // For now, we'll use the score as coins

// Get SharedPreferences and update the coins
        SharedPreferences prefs = getSharedPreferences("game_prefs", MODE_PRIVATE);
        int currentCoins = prefs.getInt("coins", 0); // Get current coins, default to 0
        int newCoinBalance = currentCoins + earnedCoins; // Add the earned coins to current balance

// Save the updated coin balance
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("coins", newCoinBalance);
        editor.apply();


        // Apply fade-out animation before transitioning
        // Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        // gridLayout.startAnimation(fadeOut);

        // Start the GameOverActivity with the final score
        Intent intent = new Intent(GameActivity.this, GameOverActivity.class);
        intent.putExtra("score", score);
        startActivity(intent);
        finish();
    }
}
