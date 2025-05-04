package com.cohen.myfinalgame;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {

    private final List<ScoreEntry> scoreList;
    private int lastPosition = -1;

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
        
        // Set rank with medal emoji for top 3
        String rankText;
        switch (position) {
            case 0:
                rankText = "🥇";
                break;
            case 1:
                rankText = "🥈";
                break;
            case 2:
                rankText = "🥉";
                break;
            default:
                rankText = String.valueOf(position + 1);
        }
        holder.rankText.setText(rankText);
        
        holder.usernameText.setText(entry.username);
        holder.scoreText.setText(String.valueOf(entry.score));
        holder.cityText.setText(entry.city);

        // Make city text clickable and open Google Maps
        holder.cityText.setOnClickListener(v -> {
            if (entry.latitude != 0 && entry.longitude != 0) {
                String uri = String.format("geo:%f,%f?q=%f,%f(%s)",
                        entry.latitude, entry.longitude,
                        entry.latitude, entry.longitude,
                        entry.city);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                intent.setPackage("com.google.android.apps.maps");
                v.getContext().startActivity(intent);
            }
        });

        // Set animation
        setAnimation(holder.itemView, position);
    }

    private void setAnimation(View viewToAnimate, int position) {
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(viewToAnimate.getContext(), 
                    android.R.anim.slide_in_left);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
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
