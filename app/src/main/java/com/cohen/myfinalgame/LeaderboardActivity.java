package com.cohen.myfinalgame;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LeaderboardActivity extends AppCompatActivity {

    private RecyclerView leaderboardRecyclerView;
    private LeaderboardAdapter leaderboardAdapter;
    private List<ScoreEntry> scoreList = new ArrayList<>();
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        leaderboardRecyclerView = findViewById(R.id.leaderboardRecyclerView);
        leaderboardRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        leaderboardAdapter = new LeaderboardAdapter(scoreList);
        leaderboardRecyclerView.setAdapter(leaderboardAdapter);

        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        loadLeaderboardData();
    }

    private void loadLeaderboardData() {
        DatabaseReference scoresRef = FirebaseDatabase.getInstance().getReference("leaderboard");

        scoresRef.orderByChild("score").limitToLast(50)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        scoreList.clear();

                        for (DataSnapshot data : snapshot.getChildren()) {
                            String username = data.child("username").getValue(String.class);
                            Long scoreLong = data.child("score").getValue(Long.class);
                            String city = data.child("city").getValue(String.class);
                            Double latitude = data.child("latitude").getValue(Double.class);
                            Double longitude = data.child("longitude").getValue(Double.class);

                            if (username != null && scoreLong != null) {
                                int score = scoreLong.intValue();
                                double lat = latitude != null ? latitude : 0.0;
                                double lng = longitude != null ? longitude : 0.0;
                                scoreList.add(new ScoreEntry(username, score, city != null ? city : "Unknown", lat, lng));
                            }
                        }

                        // Sort from highest to lowest
                        Collections.sort(scoreList, (a, b) -> Integer.compare(b.score, a.score));
                        leaderboardAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Leaderboard", "Failed to load scores", error.toException());
                        Toast.makeText(LeaderboardActivity.this, "Error loading leaderboard", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
