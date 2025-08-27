package com.mjanglin.votingmatters.models;

import java.sql.Timestamp;
import java.util.UUID;

public class PlayerVoteData {

    private final UUID uuid;
    private String username;
    private int totalVotes;
    private int currentStreak;
    private int bestStreak;
    private Timestamp lastVoteTime;
    private int offlineVotes;

    public PlayerVoteData(UUID uuid, String username, int totalVotes, int currentStreak,
            int bestStreak, Timestamp lastVoteTime, int offlineVotes) {
        this.uuid = uuid;
        this.username = username;
        this.totalVotes = totalVotes;
        this.currentStreak = currentStreak;
        this.bestStreak = bestStreak;
        this.lastVoteTime = lastVoteTime;
        this.offlineVotes = offlineVotes;
    }

    // Create new player data
    public PlayerVoteData(UUID uuid, String username) {
        this(uuid, username, 0, 0, 0, null, 0);
    }

    // Getters
    public UUID getUuid() {
        return uuid;
    }

    public String getUsername() {
        return username;
    }

    public int getTotalVotes() {
        return totalVotes;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public int getBestStreak() {
        return bestStreak;
    }

    public Timestamp getLastVoteTime() {
        return lastVoteTime;
    }

    public int getOfflineVotes() {
        return offlineVotes;
    }

    // Setters
    public void setUsername(String username) {
        this.username = username;
    }

    public void setTotalVotes(int totalVotes) {
        this.totalVotes = totalVotes;
    }

    public void addVote() {
        this.totalVotes++;
    }

    public void setCurrentStreak(int currentStreak) {
        this.currentStreak = currentStreak;
        if (currentStreak > bestStreak) {
            this.bestStreak = currentStreak;
        }
    }

    public void incrementStreak() {
        this.currentStreak++;
        if (currentStreak > bestStreak) {
            this.bestStreak = currentStreak;
        }
    }

    public void resetStreak() {
        this.currentStreak = 0;
    }

    public void setBestStreak(int bestStreak) {
        this.bestStreak = bestStreak;
    }

    public void setLastVoteTime(Timestamp lastVoteTime) {
        this.lastVoteTime = lastVoteTime;
    }

    public void setOfflineVotes(int offlineVotes) {
        this.offlineVotes = offlineVotes;
    }

    public void addOfflineVote() {
        this.offlineVotes++;
    }

    public void clearOfflineVotes() {
        this.offlineVotes = 0;
    }

    // Utility methods
    public boolean hasVotedToday() {
        if (lastVoteTime == null)
            return false;

        long twentyFourHours = 24 * 60 * 60 * 1000;
        return (System.currentTimeMillis() - lastVoteTime.getTime()) < twentyFourHours;
    }

    public long getHoursUntilNextVote() {
        if (lastVoteTime == null)
            return 0;

        long twentyFourHours = 24 * 60 * 60 * 1000;
        long timeSinceLastVote = System.currentTimeMillis() - lastVoteTime.getTime();
        long timeUntilNext = twentyFourHours - timeSinceLastVote;

        return timeUntilNext > 0 ? timeUntilNext / (60 * 60 * 1000) : 0;
    }

    public boolean isStreakAtRisk() {
        if (lastVoteTime == null || currentStreak == 0)
            return false;

        // Streak is at risk if it's been more than 20 hours since last vote
        long twentyHours = 20 * 60 * 60 * 1000;
        return (System.currentTimeMillis() - lastVoteTime.getTime()) > twentyHours;
    }
}
