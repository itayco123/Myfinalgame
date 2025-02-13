package com.cohen.myfinalgame;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Toast;
import android.view.View;
import android.widget.Button;
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
    private long timeLeft;
    private Random random = new Random();

    // Game settings
    SharedPreferences prefs;
    int totalCoins;
    boolean isDoublePointsActive, isTriplePointsActive, hasExtraLife;
    boolean isBombTriggered = false; // Track if a bomb was hit

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
        boolean hasTriplePoints = prefs.getBoolean("triplePoints", false);
        hasExtraLife = prefs.getBoolean("extraLife", false);

        // Apply extra time if purchased
        timeLeft = (hasExtraTime ? 35 : 30) * 1000;

        // Reset power-ups after using them
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("extraTimePurchased", false);
        editor.putBoolean("doublePoints", false);
        editor.putBoolean("triplePoints", false);
        editor.putBoolean("extraLife", false);
        editor.apply();

        // Activate power-ups
        isDoublePointsActive = hasDoublePoints;
        isTriplePointsActive = hasTriplePoints;

        // Set up the grid buttons dynamically
        setupGrid();

        // Start the game timer
        startGame();
    }

    private void setupGrid() {
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int buttonSize = (screenWidth / 3) - 24; // Adjust to avoid overlap

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                Button button = new Button(this);

                // Set button size and margins
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = buttonSize;
                params.height = buttonSize;
                params.setMargins(10, 10, 10, 10);
                button.setLayoutParams(params);

                // Set default appearance
                button.setBackgroundResource(android.R.color.transparent);
                button.setTag(new int[]{i, j});
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
                gridButtons[i][j].setAlpha(0f);
                gridButtons[i][j].setEnabled(false);
                gridButtons[i][j].setText("");
            }
        }

        // Choose a random button
        int i = random.nextInt(3);
        int j = random.nextInt(3);
        Button randomButton = gridButtons[i][j];

        // Assign a square type
        int squareType = random.nextInt(10);
        if (squareType < 7) {
            randomButton.setText("1");
            randomButton.setBackgroundResource(R.drawable.square_normal);
        } else if (squareType == 7) {
            randomButton.setText("+3");
            randomButton.setBackgroundResource(R.drawable.square_time);
        } else if (squareType == 8) {
            randomButton.setText("2");
            randomButton.setBackgroundResource(R.drawable.square_double);
        } else {
            randomButton.setText("💣");
            randomButton.setBackgroundResource(R.drawable.square_bomb);
        }

        // Enable the button and apply fade-in animation
        randomButton.setEnabled(true);
        randomButton.animate().alpha(1f).setDuration(300);
    }

    private void handleButtonClick(View view) {
        Button clickedButton = (Button) view;
        animateButton(clickedButton);

        if (clickedButton.getText().equals("💣")) {
            if (hasExtraLife) {
                hasExtraLife = false;
                Toast.makeText(GameActivity.this, "Extra Life Used!", Toast.LENGTH_SHORT).show();
            } else {
                mainTimer.cancel();
                popUpTimer.cancel();
                endGame();
            }
        } else if (clickedButton.getText().equals("+3")) {
            timeLeft += 3000;
            mainTimer.cancel();
            startGame();
        } else if (clickedButton.getText().equals("2")) {
            score += isDoublePointsActive ? 4 : (isTriplePointsActive ? 6 : 2);
        } else {
            score += isDoublePointsActive ? 2 : (isTriplePointsActive ? 3 : 1);
        }

        // ✅ If Triple Points is active, start a timer to disable it after 5 seconds
        if (isTriplePointsActive) {
            new android.os.Handler().postDelayed(() -> {
                isTriplePointsActive = false; // Disable Triple Points
                Toast.makeText(GameActivity.this, "Triple Points expired!", Toast.LENGTH_SHORT).show();
            }, 5000);
        }

        scoreText.setText("Score: " + score);
        popSquare();
    }


    private void animateButton(Button button) {
        button.animate()
                .scaleX(0.8f)
                .scaleY(0.8f)
                .setDuration(100)
                .withEndAction(() -> button.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100));
    }

    private void endGame() {
        if (mainTimer != null) mainTimer.cancel();
        if (popUpTimer != null) popUpTimer.cancel();

        totalCoins += score;
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("coins", totalCoins);
        editor.apply();

        gridLayout.animate().alpha(0f).setDuration(500).withEndAction(() -> {
            Intent intent = new Intent(GameActivity.this, GameOverActivity.class);
            intent.putExtra("score", score);
            startActivity(intent);
            finish();
        });
    }
}
