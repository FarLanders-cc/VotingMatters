package com.mjanglin.votingmatters.models;

import java.sql.Timestamp;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("PlayerVoteData Model Tests")
class PlayerVoteDataTest {

    @Test
    @DisplayName("Should create player vote data correctly")
    void testPlayerVoteDataCreation() {
        UUID uuid = UUID.randomUUID();
        String username = "TestPlayer";

        PlayerVoteData data = new PlayerVoteData(uuid, username);

        assertEquals(uuid, data.getUuid());
        assertEquals(username, data.getUsername());
        assertEquals(0, data.getTotalVotes());
        assertEquals(0, data.getCurrentStreak());
        assertEquals(0, data.getBestStreak());
        assertEquals(0, data.getOfflineVotes());
        assertNull(data.getLastVoteTime());
    }

    @Test
    @DisplayName("Should handle vote addition correctly")
    void testVoteAddition() {
        UUID uuid = UUID.randomUUID();
        PlayerVoteData data = new PlayerVoteData(uuid, "TestPlayer");

        data.addVote();
        assertEquals(1, data.getTotalVotes());

        data.addVote();
        assertEquals(2, data.getTotalVotes());
    }

    @Test
    @DisplayName("Should handle streak management")
    void testStreakManagement() {
        UUID uuid = UUID.randomUUID();
        PlayerVoteData data = new PlayerVoteData(uuid, "TestPlayer");

        data.incrementStreak();
        assertEquals(1, data.getCurrentStreak());
        assertEquals(1, data.getBestStreak());

        data.incrementStreak();
        assertEquals(2, data.getCurrentStreak());
        assertEquals(2, data.getBestStreak());

        data.resetStreak();
        assertEquals(0, data.getCurrentStreak());
        assertEquals(2, data.getBestStreak()); // Best streak should remain
    }

    @Test
    @DisplayName("Should handle offline votes")
    void testOfflineVotes() {
        UUID uuid = UUID.randomUUID();
        PlayerVoteData data = new PlayerVoteData(uuid, "TestPlayer");

        data.addOfflineVote();
        assertEquals(1, data.getOfflineVotes());

        data.addOfflineVote();
        assertEquals(2, data.getOfflineVotes());

        data.clearOfflineVotes();
        assertEquals(0, data.getOfflineVotes());
    }

    @Test
    @DisplayName("Should handle vote time tracking")
    void testVoteTimeTracking() {
        UUID uuid = UUID.randomUUID();
        PlayerVoteData data = new PlayerVoteData(uuid, "TestPlayer");

        Timestamp now = new Timestamp(System.currentTimeMillis());
        data.setLastVoteTime(now);

        assertEquals(now, data.getLastVoteTime());

        // Test hasVotedToday (should be true since we just set it to now)
        assertTrue(data.hasVotedToday());

        // Test with old timestamp (more than 24 hours ago)
        Timestamp oldTime = new Timestamp(System.currentTimeMillis() - (25 * 60 * 60 * 1000));
        data.setLastVoteTime(oldTime);
        assertFalse(data.hasVotedToday());
    }

    @Test
    @DisplayName("Should calculate hours until next vote correctly")
    void testHoursUntilNextVote() {
        UUID uuid = UUID.randomUUID();
        PlayerVoteData data = new PlayerVoteData(uuid, "TestPlayer");

        // No last vote time
        assertEquals(0, data.getHoursUntilNextVote());

        // Recent vote (should have hours remaining)
        Timestamp recentTime = new Timestamp(System.currentTimeMillis() - (5 * 60 * 60 * 1000)); // 5 hours ago
        data.setLastVoteTime(recentTime);
        long hoursUntilNext = data.getHoursUntilNextVote();
        assertTrue(hoursUntilNext >= 0 && hoursUntilNext <= 24);

        // Old vote (should be 0)
        Timestamp oldTime = new Timestamp(System.currentTimeMillis() - (25 * 60 * 60 * 1000)); // 25 hours ago
        data.setLastVoteTime(oldTime);
        assertEquals(0, data.getHoursUntilNextVote());
    }

    @Test
    @DisplayName("Should detect streak at risk")
    void testStreakAtRisk() {
        UUID uuid = UUID.randomUUID();
        PlayerVoteData data = new PlayerVoteData(uuid, "TestPlayer");

        // No streak, should not be at risk
        assertFalse(data.isStreakAtRisk());

        // Set a streak but no last vote time
        data.setCurrentStreak(5);
        assertFalse(data.isStreakAtRisk());

        // Recent vote, should not be at risk
        Timestamp recentTime = new Timestamp(System.currentTimeMillis() - (5 * 60 * 60 * 1000)); // 5 hours ago
        data.setLastVoteTime(recentTime);
        assertFalse(data.isStreakAtRisk());

        // Old vote (21 hours ago), should be at risk
        Timestamp oldTime = new Timestamp(System.currentTimeMillis() - (21 * 60 * 60 * 1000));
        data.setLastVoteTime(oldTime);
        assertTrue(data.isStreakAtRisk());
    }
}
