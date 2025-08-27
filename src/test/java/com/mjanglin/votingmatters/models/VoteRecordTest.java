package com.mjanglin.votingmatters.models;

import java.sql.Timestamp;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("VoteRecord Model Tests")
class VoteRecordTest {

    @Test
    @DisplayName("Should create vote record correctly")
    void testVoteRecordCreation() {
        UUID playerUuid = UUID.randomUUID();
        String siteName = "testsite";
        String ipAddress = "127.0.0.1";

        VoteRecord voteRecord = new VoteRecord(playerUuid, siteName, ipAddress);

        assertEquals(0, voteRecord.getId()); // New record should have ID 0
        assertEquals(playerUuid, voteRecord.getPlayerUuid());
        assertEquals(siteName, voteRecord.getSiteName());
        assertEquals(ipAddress, voteRecord.getIpAddress());
        assertFalse(voteRecord.isRewarded()); // New record should not be rewarded
        assertNotNull(voteRecord.getVoteTime()); // Should have a timestamp
    }

    @Test
    @DisplayName("Should create vote record with full constructor")
    void testFullVoteRecordCreation() {
        int id = 123;
        UUID playerUuid = UUID.randomUUID();
        String siteName = "testsite";
        Timestamp voteTime = new Timestamp(System.currentTimeMillis());
        String ipAddress = "127.0.0.1";
        boolean rewarded = true;

        VoteRecord voteRecord = new VoteRecord(id, playerUuid, siteName, voteTime, ipAddress, rewarded);

        assertEquals(id, voteRecord.getId());
        assertEquals(playerUuid, voteRecord.getPlayerUuid());
        assertEquals(siteName, voteRecord.getSiteName());
        assertEquals(voteTime, voteRecord.getVoteTime());
        assertEquals(ipAddress, voteRecord.getIpAddress());
        assertTrue(voteRecord.isRewarded());
    }

    @Test
    @DisplayName("Should handle reward status correctly")
    void testRewardStatus() {
        UUID playerUuid = UUID.randomUUID();
        VoteRecord voteRecord = new VoteRecord(playerUuid, "testsite", "127.0.0.1");

        // Initially not rewarded
        assertFalse(voteRecord.isRewarded());

        // Mark as rewarded
        voteRecord.setRewarded(true);
        assertTrue(voteRecord.isRewarded());

        // Mark as not rewarded
        voteRecord.setRewarded(false);
        assertFalse(voteRecord.isRewarded());
    }

    @Test
    @DisplayName("Should handle vote time correctly")
    void testVoteTime() {
        UUID playerUuid = UUID.randomUUID();
        VoteRecord voteRecord = new VoteRecord(playerUuid, "testsite", "127.0.0.1");

        Timestamp originalTime = voteRecord.getVoteTime();
        assertNotNull(originalTime);

        // Vote time should be close to current time (within last minute)
        long timeDiff = System.currentTimeMillis() - originalTime.getTime();
        assertTrue(timeDiff < 60000, "Vote time should be recent"); // Less than 1 minute
    }

    @Test
    @DisplayName("Should handle null parameters gracefully")
    void testNullParameterHandling() {
        // Test with null player UUID
        assertDoesNotThrow(() -> {
            new VoteRecord(null, "testsite", "127.0.0.1");
        });

        // Test with null site name
        assertDoesNotThrow(() -> {
            new VoteRecord(UUID.randomUUID(), null, "127.0.0.1");
        });

        // Test with null IP address
        assertDoesNotThrow(() -> {
            new VoteRecord(UUID.randomUUID(), "testsite", null);
        });
    }

    @Test
    @DisplayName("Should create records with timestamps")
    void testTimestamps() {
        UUID playerUuid = UUID.randomUUID();

        VoteRecord record1 = new VoteRecord(playerUuid, "testsite", "127.0.0.1");
        VoteRecord record2 = new VoteRecord(playerUuid, "testsite", "127.0.0.1");

        // Both records should have valid timestamps
        assertNotNull(record1.getVoteTime());
        assertNotNull(record2.getVoteTime());

        // The timestamps should be recent (within last minute)
        long currentTime = System.currentTimeMillis();
        assertTrue(currentTime - record1.getVoteTime().getTime() < 60000);
        assertTrue(currentTime - record2.getVoteTime().getTime() < 60000);
    }
}
