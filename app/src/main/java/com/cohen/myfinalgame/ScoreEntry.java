package com.cohen.myfinalgame;

public class ScoreEntry {
    public String username;
    public int score;
    public String city;
    public double latitude;
    public double longitude;

    public ScoreEntry(String username, int score, String city, double latitude, double longitude) {
        this.username = username;
        this.score = score;
        this.city = city;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
