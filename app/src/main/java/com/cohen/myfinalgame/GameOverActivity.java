package com.cohen.myfinalgame;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.location.Location;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import android.os.Looper;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;



public class GameOverActivity extends Activity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private TextView finalScoreText, highScoreText, coinsText;
    private Button playAgainButton, shopButton, backToMainButton;

    private int pendingScore; // Used if permission is granted later

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_over);

        finalScoreText = findViewById(R.id.finalScoreText);
        highScoreText = findViewById(R.id.highScoreText);
        coinsText = findViewById(R.id.coinsText);
        playAgainButton = findViewById(R.id.playAgainButton);
        shopButton = findViewById(R.id.shopButton);
        backToMainButton = findViewById(R.id.backToMainButton);

        int score = getIntent().getIntExtra("score", 0);
        finalScoreText.setText("Final Score: " + score);
        pendingScore = score;

        SharedPreferences prefs = getSharedPreferences("game_prefs", MODE_PRIVATE);
        int highScore = prefs.getInt("highScore", 0);
        int totalCoins = prefs.getInt("coins", 0);

        // Request location permissions first
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            saveHighScore(score);
        }

        if (score > highScore) {
            highScore = score;
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("highScore", highScore);
            editor.apply();
            Log.d("LeaderboardDebug", "New high score detected: " + highScore);
        }

        totalCoins += score;
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("coins", totalCoins);
        editor.apply();

        highScoreText.setText("High Score: " + highScore);
        coinsText.setText("Total Coins: " + totalCoins);

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

    // Handle permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveHighScore(pendingScore);
            } else {
                Toast.makeText(this, "Location permission denied. City won't be saved.", Toast.LENGTH_SHORT).show();
                saveHighScoreWithoutLocation(pendingScore);
            }
        }
    }

    private void saveHighScore(int score) {
        Log.d("LeaderboardDebug", "🔥 saveHighScore() called with score: " + score);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String username = (user != null && user.getDisplayName() != null) ? user.getDisplayName() : "Guest";

        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000); // Optional
        locationRequest.setFastestInterval(500); // Optional

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null || locationResult.getLastLocation() == null) {
                    Log.w("GeoDebug", "High accuracy location is null");
                    saveHighScoreWithoutLocation(score);
                    fusedLocationClient.removeLocationUpdates(this);
                    return;
                }

                Location location = locationResult.getLastLocation();
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                String city = "Unknown";

                Geocoder geocoder = new Geocoder(GameOverActivity.this, Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        city = addresses.get(0).getLocality();
                        Log.d("GeoDebug", "City obtained: " + city);
                    } else {
                        Log.d("GeoDebug", "Geocoder returned no address");
                    }
                } catch (IOException e) {
                    Log.e("GeoDebug", "Geocoder error", e);
                }

                DatabaseReference scoresRef = FirebaseDatabase.getInstance().getReference("leaderboard");
                Map<String, Object> scoreEntry = new HashMap<>();
                scoreEntry.put("username", username);
                scoreEntry.put("score", score);
                scoreEntry.put("city", city);
                scoreEntry.put("latitude", latitude);
                scoreEntry.put("longitude", longitude);

                scoresRef.push().setValue(scoreEntry)
                        .addOnSuccessListener(aVoid -> Log.d("LeaderboardDebug", "✅ Score with location added successfully!"))
                        .addOnFailureListener(e -> Log.e("LeaderboardDebug", "❌ Failed to save score", e));

                fusedLocationClient.removeLocationUpdates(this); // stop updates to save battery
            }
        };

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        } catch (SecurityException e) {
            Log.e("GeoDebug", "Location permission error", e);
            saveHighScoreWithoutLocation(score);
        }
    }

    private void saveHighScoreWithoutLocation(int score) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String username = (user != null && user.getDisplayName() != null) ? user.getDisplayName() : "Guest";

        DatabaseReference scoresRef = FirebaseDatabase.getInstance().getReference("leaderboard");
        Map<String, Object> scoreEntry = new HashMap<>();
        scoreEntry.put("username", username);
        scoreEntry.put("score", score);

        scoresRef.push().setValue(scoreEntry)
                .addOnSuccessListener(aVoid -> Log.d("LeaderboardDebug", "✅ Score saved without location"))
                .addOnFailureListener(e -> Log.e("LeaderboardDebug", "❌ Failed to save score", e));
    }
}
