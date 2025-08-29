package cc.farlanders.votingmatters.database;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import be.seeseemelk.mockbukkit.MockBukkit;
import cc.farlanders.votingmatters.VotingMatters;
import cc.farlanders.votingmatters.models.PlayerVoteData;
import cc.farlanders.votingmatters.models.VoteRecord;

@DisplayName("DatabaseManager Tests")
class DatabaseManagerTest {

    private VotingMatters plugin;
    private DatabaseManager databaseManager;

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
        plugin = MockBukkit.load(VotingMatters.class);

        plugin.getDataFolder().mkdirs();
        databaseManager = plugin.getDatabaseManager();
        assertNotNull(databaseManager, "DatabaseManager should be initialized");
    }

    @AfterEach
    void tearDown() {
        if (databaseManager != null) {
            databaseManager.close();
        }
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("Should get player data for existing player")
    void testGetPlayerData() throws Exception {
        UUID playerId = UUID.randomUUID();

        // Create player data first
        PlayerVoteData newPlayerData = new PlayerVoteData(playerId, "TestPlayer");
        databaseManager.savePlayerData(newPlayerData).get(5, TimeUnit.SECONDS);

        CompletableFuture<PlayerVoteData> future = databaseManager.getPlayerData(playerId);
        PlayerVoteData data = future.get(5, TimeUnit.SECONDS);

        assertNotNull(data, "Player data should not be null");
        assertEquals(playerId, data.getUuid(), "Player UUID should match");
        assertEquals("TestPlayer", data.getUsername(), "Player name should match");
        assertEquals(0, data.getTotalVotes(), "New player should have 0 votes");
    }

    @Test
    @DisplayName("Should record vote correctly")
    void testAddVoteRecord() throws Exception {
        UUID playerId = UUID.randomUUID();
        String siteName = "testsite";

        // First create player data
        PlayerVoteData playerData = new PlayerVoteData(playerId, "TestPlayer");
        databaseManager.savePlayerData(playerData).get(5, TimeUnit.SECONDS);

        // Add vote record
        VoteRecord voteRecord = new VoteRecord(playerId, siteName, "127.0.0.1");
        CompletableFuture<Void> addFuture = databaseManager.addVoteRecord(voteRecord);
        addFuture.get(5, TimeUnit.SECONDS);

        // Update player vote count
        playerData.addVote();
        databaseManager.savePlayerData(playerData).get(5, TimeUnit.SECONDS);

        // Verify vote was recorded
        CompletableFuture<PlayerVoteData> dataFuture = databaseManager.getPlayerData(playerId);
        PlayerVoteData data = dataFuture.get(5, TimeUnit.SECONDS);

        assertEquals(1, data.getTotalVotes(), "Player should have 1 vote after recording");
    }

    @Test
    @DisplayName("Should check if player voted on site within time limit")
    void testHasVotedOnSite() throws Exception {
        UUID playerId = UUID.randomUUID();
        String siteName = "testsite";
        int hoursLimit = 24;

        // Initially should not have voted
        CompletableFuture<Boolean> initialCheck = databaseManager.hasVotedOnSite(playerId, siteName, hoursLimit);
        assertFalse(initialCheck.get(5, TimeUnit.SECONDS), "Player should not have voted initially");

        // Add a vote
        VoteRecord voteRecord = new VoteRecord(playerId, siteName, "127.0.0.1");
        databaseManager.addVoteRecord(voteRecord).get(5, TimeUnit.SECONDS);

        // Now should have voted
        CompletableFuture<Boolean> afterVoteCheck = databaseManager.hasVotedOnSite(playerId, siteName, hoursLimit);
        assertTrue(afterVoteCheck.get(5, TimeUnit.SECONDS), "Player should have voted after recording vote");
    }

    @Test
    @DisplayName("Should get top voters")
    void testGetTopVoters() throws Exception {
        UUID player1 = UUID.randomUUID();
        UUID player2 = UUID.randomUUID();

        // Create player data and add votes for player1 (3 votes)
        PlayerVoteData playerData1 = new PlayerVoteData(player1, "Player1");
        playerData1.addVote();
        playerData1.addVote();
        playerData1.addVote();
        databaseManager.savePlayerData(playerData1).get(5, TimeUnit.SECONDS);

        // Create player data and add votes for player2 (1 vote)
        PlayerVoteData playerData2 = new PlayerVoteData(player2, "Player2");
        playerData2.addVote();
        databaseManager.savePlayerData(playerData2).get(5, TimeUnit.SECONDS);

        // Get top voters
        CompletableFuture<List<PlayerVoteData>> topVotersFuture = databaseManager.getTopVoters(10);
        List<PlayerVoteData> topVoters = topVotersFuture.get(5, TimeUnit.SECONDS);

        assertNotNull(topVoters, "Top voters list should not be null");
        assertTrue(topVoters.size() >= 2, "Should have at least 2 voters");

        // Verify sorting (highest votes first)
        PlayerVoteData firstPlayer = topVoters.get(0);
        assertEquals(3, firstPlayer.getTotalVotes(), "First player should have most votes");
    }

    @Test
    @DisplayName("Should get unrewarded votes")
    void testGetUnrewardedVotes() throws Exception {
        UUID playerId = UUID.randomUUID();
        String siteName = "testsite";

        // Add a vote record
        VoteRecord voteRecord = new VoteRecord(playerId, siteName, "127.0.0.1");
        databaseManager.addVoteRecord(voteRecord).get(5, TimeUnit.SECONDS);

        // Get unrewarded votes
        CompletableFuture<List<VoteRecord>> unrewardedFuture = databaseManager.getUnrewardedVotes(playerId);
        List<VoteRecord> unrewarded = unrewardedFuture.get(5, TimeUnit.SECONDS);

        assertNotNull(unrewarded, "Unrewarded votes list should not be null");
        assertEquals(1, unrewarded.size(), "Should have 1 unrewarded vote");

        VoteRecord retrievedRecord = unrewarded.get(0);
        assertEquals(playerId, retrievedRecord.getPlayerUuid(), "Vote record should have correct player ID");
        assertEquals(siteName, retrievedRecord.getSiteName(), "Vote record should have correct site name");
        assertFalse(retrievedRecord.isRewarded(), "Vote should not be rewarded yet");
    }

    @Test
    @DisplayName("Should mark vote as rewarded")
    void testMarkVoteRewarded() throws Exception {
        UUID playerId = UUID.randomUUID();
        String siteName = "testsite";

        // Add a vote record
        VoteRecord voteRecord = new VoteRecord(playerId, siteName, "127.0.0.1");
        databaseManager.addVoteRecord(voteRecord).get(5, TimeUnit.SECONDS);

        // Get the vote record ID
        List<VoteRecord> unrewarded = databaseManager.getUnrewardedVotes(playerId).get(5, TimeUnit.SECONDS);
        assertEquals(1, unrewarded.size(), "Should have 1 unrewarded vote");

        int voteId = unrewarded.get(0).getId();

        // Mark as rewarded
        databaseManager.markVoteRewarded(voteId).get(5, TimeUnit.SECONDS);

        // Verify no more unrewarded votes
        List<VoteRecord> afterReward = databaseManager.getUnrewardedVotes(playerId).get(5, TimeUnit.SECONDS);
        assertEquals(0, afterReward.size(), "Should have no unrewarded votes after marking as rewarded");
    }

    @Test
    @DisplayName("Should handle database connection errors gracefully")
    void testDatabaseErrorHandling() {
        // This test verifies that the database manager handles errors gracefully
        assertDoesNotThrow(() -> {
            databaseManager.close();
            // Attempting operations after close should not throw uncaught exceptions
            CompletableFuture<PlayerVoteData> future = databaseManager.getPlayerData(UUID.randomUUID());
            // The future should complete exceptionally or return null
            PlayerVoteData result = future.get(5, TimeUnit.SECONDS);
            assertNull(result, "Operations on closed database should return null");
        });
    }
}
