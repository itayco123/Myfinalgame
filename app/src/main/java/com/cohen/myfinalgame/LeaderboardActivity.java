package com.cohen.myfinalgame;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LeaderboardActivity extends Activity {

    private ListView leaderboardList;
    private ArrayAdapter<String> leaderboardAdapter;
    private List<String> leaderboardData = new ArrayList<>();
    private Animation fadeInAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        leaderboardList = findViewById(R.id.leaderboardList);
        Button backButton = findViewById(R.id.backButton);

        // Load fade-in animation for smooth leaderboard updates
        fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);

        // Set up ListView adapter
        leaderboardAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, leaderboardData);
        leaderboardList.setAdapter(leaderboardAdapter);

        // Load leaderboard data from Firebase
        loadLeaderboard();

        // Back button to Main Menu
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(LeaderboardActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void loadLeaderboard() {
        DatabaseReference scoresRef = FirebaseDatabase.getInstance().getReference("leaderboard");

        scoresRef.orderByChild("score").limitToLast(10).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                leaderboardData.clear();
                List<ScoreEntry> scoreList = new ArrayList<>();

                for (DataSnapshot data : snapshot.getChildren()) {
                    String username = data.child("username").getValue(String.class);
                    Integer score = data.child("score").getValue(Integer.class);

                    if (username == null || username.trim().isEmpty()) {
                        username = "Guest"; // Default username if missing
                    }

                    if (score != null) {
                        scoreList.add(new ScoreEntry(username, score));
                    }
                }

                // Sort from highest to lowest score
                Collections.sort(scoreList, (a, b) -> Integer.compare(b.score, a.score));

                // Convert to display format
                int rank = 1;
                for (ScoreEntry entry : scoreList) {
                    leaderboardData.add(rank + ". " + entry.username + " - " + entry.score);
                    rank++;
                }

                // Update UI with animation
                leaderboardList.startAnimation(fadeInAnimation);
                leaderboardAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Leaderboard", "Failed to load scores", error.toException());
                Toast.makeText(LeaderboardActivity.this, "Failed to load leaderboard.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Helper class for sorting scores
    static class ScoreEntry {
        String username;
        int score;

        ScoreEntry(String username, int score) {
            this.username = username;
            this.score = score;
        }
    }
}
