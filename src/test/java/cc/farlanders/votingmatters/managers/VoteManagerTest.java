package cc.farlanders.votingmatters.managers;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import cc.farlanders.votingmatters.VotingMatters;
import cc.farlanders.votingmatters.models.PlayerVoteData;
import cc.farlanders.votingmatters.models.VoteRecord;

@DisplayName("VoteManager Tests")
class VoteManagerTest {

    private VotingMatters plugin;
    private VoteManager voteManager;
    private PlayerMock player;

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
        plugin = MockBukkit.load(VotingMatters.class);

        voteManager = plugin.getVoteManager();
        assertNotNull(voteManager, "VoteManager should be initialized");

        player = MockBukkit.getMock().addPlayer("TestPlayer");
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("Should get enabled vote sites")
    void testGetEnabledSites() {
        // Test getting enabled sites
        var enabledSites = voteManager.getEnabledSites();
        assertNotNull(enabledSites, "Enabled sites should not be null");
        // The set might be empty since we haven't configured any vote sites in test
    }

    @Test
    @DisplayName("Should process vote correctly")
    void testProcessVote() throws Exception {
        UUID playerId = player.getUniqueId();
        String siteName = "testsite";
        String ipAddress = "127.0.0.1";

        // Create initial player data
        PlayerVoteData playerData = new PlayerVoteData(playerId, "TestPlayer");
        plugin.getDatabaseManager().savePlayerData(playerData).get(5, TimeUnit.SECONDS);

        // Create and process vote record
        VoteRecord voteRecord = new VoteRecord(playerId, siteName, ipAddress);
        CompletableFuture<Void> result = voteManager.processVote(voteRecord);

        // Should complete without throwing exceptions
        assertDoesNotThrow(() -> result.get(5, TimeUnit.SECONDS), "Vote processing should not throw exceptions");
    }

    @Test
    @DisplayName("Should check if player voted recently")
    void testHasVotedRecently() throws Exception {
        UUID playerId = player.getUniqueId();
        String siteName = "testsite";

        // Initially should not have voted recently
        CompletableFuture<Boolean> initialCheck = voteManager.hasVotedRecently(playerId, siteName);
        boolean hasVotedInitially = initialCheck.get(5, TimeUnit.SECONDS);
        assertFalse(hasVotedInitially, "Player should not have voted recently initially");

        // Process a vote
        VoteRecord voteRecord = new VoteRecord(playerId, siteName, "127.0.0.1");
        voteManager.processVote(voteRecord).get(5, TimeUnit.SECONDS);

        // Now should have voted recently
        CompletableFuture<Boolean> afterVoteCheck = voteManager.hasVotedRecently(playerId, siteName);
        boolean hasVotedRecently = afterVoteCheck.get(5, TimeUnit.SECONDS);
        assertTrue(hasVotedRecently, "Player should have voted recently after processing vote");
    }

    @Test
    @DisplayName("Should reward offline votes when player joins")
    void testOfflineVoteRewarding() throws Exception {
        UUID playerId = player.getUniqueId();

        // Create player data with offline votes
        PlayerVoteData playerData = new PlayerVoteData(playerId, "TestPlayer");
        playerData.addOfflineVote();
        playerData.addOfflineVote();
        plugin.getDatabaseManager().savePlayerData(playerData).get(5, TimeUnit.SECONDS);

        // Process offline votes
        CompletableFuture<Void> result = voteManager.processOfflineVotes(player);

        // Should complete without exceptions
        assertDoesNotThrow(() -> result.get(5, TimeUnit.SECONDS),
                "Offline vote processing should not throw exceptions");
    }

    @Test
    @DisplayName("Should get voting sites configuration")
    void testGetVotingSites() {
        // Test getting voting sites configuration
        var votingSites = voteManager.getVotingSites();
        assertNotNull(votingSites, "Voting sites should not be null");

        // The map might be empty since we haven't configured any vote sites in test
        // But the method should still work without throwing exceptions
    }

    @Test
    @DisplayName("Should handle vote checking without errors")
    void testCheckForVotes() {
        // Test that vote checking works without errors
        assertDoesNotThrow(() -> {
            CompletableFuture<Void> result = voteManager.checkForVotes();
            result.get(5, TimeUnit.SECONDS);
        }, "Vote checking should not throw exceptions");
    }

    @Test
    @DisplayName("Should reload configuration correctly")
    void testConfigurationReload() {
        // Test that vote manager can reload its configuration
        assertDoesNotThrow(() -> {
            voteManager.reload();
        }, "VoteManager should reload without errors");

        // Verify that the manager is still functional after reload
        assertNotNull(voteManager.getEnabledSites(), "Vote sites should still be available after reload");
    }

    @Test
    @DisplayName("Should handle null parameters gracefully")
    void testNullParameterHandling() {
        // Test that the manager handles null parameters gracefully
        assertDoesNotThrow(() -> {
            // These should not throw uncaught exceptions
            voteManager.hasVotedRecently(null, "testsite");
            voteManager.hasVotedRecently(UUID.randomUUID(), null);
        }, "VoteManager should handle null parameters gracefully");
    }

    @Test
    @DisplayName("Should validate vote records correctly")
    void testVoteRecordValidation() throws Exception {
        UUID playerId = player.getUniqueId();
        String siteName = "testsite";
        String ipAddress = "127.0.0.1";

        // Test with valid vote record
        VoteRecord validRecord = new VoteRecord(playerId, siteName, ipAddress);
        assertNotNull(validRecord, "Valid vote record should be created");
        assertEquals(playerId, validRecord.getPlayerUuid(), "Player UUID should match");
        assertEquals(siteName, validRecord.getSiteName(), "Site name should match");
        assertEquals(ipAddress, validRecord.getIpAddress(), "IP address should match");
        assertFalse(validRecord.isRewarded(), "New vote record should not be rewarded");
    }
}
