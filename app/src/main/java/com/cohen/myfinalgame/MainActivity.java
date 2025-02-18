package com.cohen.myfinalgame;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private Button startGameButton, logInButton, signUpButton, logoutButton;
    private TextView welcomeTextView;
    private FirebaseAuth mAuth;

    // Constants for the inactivity notification
    private static final int INACTIVITY_NOTIFICATION_REQUEST_CODE = 0;
    // Inactivity delay set to 3 hours (in milliseconds)
    private static final long INACTIVITY_DELAY = 3 * 60 * 60 * 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        1001); // Request code can be any int you choose
            }
        }
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Reference UI elements
        welcomeTextView = findViewById(R.id.welcomeTextView);
        startGameButton = findViewById(R.id.startGameButton);
        logInButton = findViewById(R.id.logInButton);
        signUpButton = findViewById(R.id.signUpButton);
        logoutButton = findViewById(R.id.logoutButton);
        Button leaderboardButton = findViewById(R.id.leaderboardButton);

        leaderboardButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LeaderboardActivity.class);
            startActivity(intent);
        });

        // If a user is logged in, extract their username from their email
        if (currentUser != null) {
            String email = currentUser.getEmail();
            String username = "User";
            if (email != null && email.contains("@")) {
                username = email.substring(0, email.indexOf("@"));
            }
            welcomeTextView.setText("Welcome, " + username + "!");
            logInButton.setVisibility(View.GONE);
            signUpButton.setVisibility(View.GONE);
            logoutButton.setVisibility(View.VISIBLE);
        } else {
            welcomeTextView.setText("Welcome, Guest!");
            logInButton.setVisibility(View.VISIBLE);
            signUpButton.setVisibility(View.VISIBLE);
            logoutButton.setVisibility(View.GONE);
        }

        // Start Game button click
        startGameButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GameActivity.class);
            startActivity(intent);
        });

        // Log In button click
        logInButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        // Sign Up button click
        signUpButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
            startActivity(intent);
        });

        // Log Out button click
        logoutButton.setOnClickListener(v -> {
            mAuth.signOut();
            // Refresh MainActivity to update UI
            Intent intent = new Intent(MainActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Cancel the inactivity notification when the user returns to the app
        cancelInactivityNotification();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Schedule an inactivity notification if the user leaves the app
        scheduleInactivityNotification();
    }

    // Schedule a notification to be triggered after 3 hours of inactivity
    private void scheduleInactivityNotification() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, NotificationReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(
                this,
                INACTIVITY_NOTIFICATION_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        long triggerTime = System.currentTimeMillis() + INACTIVITY_DELAY;
        if (alarmManager != null) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, alarmIntent);
        }
    }

    // Cancel any scheduled inactivity notification
    private void cancelInactivityNotification() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, NotificationReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(
                this,
                INACTIVITY_NOTIFICATION_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        if (alarmManager != null) {
            alarmManager.cancel(alarmIntent);
        }
    }
}
