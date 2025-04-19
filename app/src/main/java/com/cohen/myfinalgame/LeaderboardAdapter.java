package com.cohen.myfinalgame;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {

    private final List<ScoreEntry> scoreList;

    public LeaderboardAdapter(List<ScoreEntry> scoreList) {
        this.scoreList = scoreList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_leaderboard_entry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScoreEntry entry = scoreList.get(position);
        holder.rankText.setText(String.valueOf(position + 1));
        holder.usernameText.setText(entry.username);
        holder.scoreText.setText(String.valueOf(entry.score));
        holder.cityText.setText(entry.city);
    }

    @Override
    public int getItemCount() {
        return scoreList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView rankText, usernameText, scoreText, cityText;

        ViewHolder(View itemView) {
            super(itemView);
            rankText = itemView.findViewById(R.id.rankText);
            usernameText = itemView.findViewById(R.id.usernameText);
            scoreText = itemView.findViewById(R.id.scoreText);
            cityText = itemView.findViewById(R.id.cityText);
        }
    }
}
