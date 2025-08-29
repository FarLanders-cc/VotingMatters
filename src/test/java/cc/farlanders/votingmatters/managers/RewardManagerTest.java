package cc.farlanders.votingmatters.managers;

import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import cc.farlanders.votingmatters.VotingMatters;
import cc.farlanders.votingmatters.models.PlayerVoteData;

@DisplayName("RewardManager Tests")
class RewardManagerTest {

    private VotingMatters plugin;
    private RewardManager rewardManager;
    private PlayerMock player;

    @BeforeEach
    void setUp() {
        try {
            // Set system properties to help with MockBukkit initialization
            System.setProperty("MockBukkit.enableStackTraces", "true");

            MockBukkit.mock();
            plugin = MockBukkit.load(VotingMatters.class);

            rewardManager = plugin.getRewardManager();
            assertNotNull(rewardManager, "RewardManager should be initialized");

            var mockServer = MockBukkit.getMock();
            if (mockServer != null) {
                player = mockServer.addPlayer("TestPlayer");
                assertNotNull(player, "Player should be created successfully");
            } else {
                throw new RuntimeException("MockBukkit server is null");
            }
        } catch (Exception e) {
            // If MockBukkit fails, clean up and rethrow
            try {
                MockBukkit.unmock();
            } catch (Exception ignored) {
                // Ignore cleanup errors
            }
            throw new RuntimeException("Failed to initialize MockBukkit for RewardManagerTest: " + e.getMessage(), e);
        }
    }

    @AfterEach
    void tearDown() {
        try {
            MockBukkit.unmock();
        } catch (Exception e) {
            // Log but don't fail the test on cleanup errors
            System.err.println("Warning: Error during MockBukkit cleanup in RewardManagerTest: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should give vote rewards without errors")
    void testGiveVoteRewards() {
        UUID playerId = player.getUniqueId();
        PlayerVoteData playerData = new PlayerVoteData(playerId, "TestPlayer");

        // Test giving rewards for a vote
        assertDoesNotThrow(() -> {
            rewardManager.giveVoteRewards(player, "testsite", playerData);
        }, "Giving vote rewards should not throw exceptions");
    }

    @Test
    @DisplayName("Should handle offline votes")
    void testOfflineVotes() {
        UUID playerId = player.getUniqueId();
        PlayerVoteData playerData = new PlayerVoteData(playerId, "TestPlayer");
        playerData.addOfflineVote();
        playerData.addOfflineVote();

        // Test giving rewards when player has offline votes
        assertDoesNotThrow(() -> {
            rewardManager.giveVoteRewards(player, "testsite", playerData);
        }, "Handling offline votes should not throw exceptions");
    }

    @Test
    @DisplayName("Should handle milestone rewards")
    void testMilestoneRewards() {
        UUID playerId = player.getUniqueId();
        PlayerVoteData playerData = new PlayerVoteData(playerId, "TestPlayer");

        // Set vote count to a milestone (e.g., 10)
        playerData.setTotalVotes(10);

        // Test giving milestone rewards
        assertDoesNotThrow(() -> {
            rewardManager.giveVoteRewards(player, "testsite", playerData);
        }, "Milestone rewards should be given without errors");
    }

    @Test
    @DisplayName("Should handle streak rewards")
    void testStreakRewards() {
        UUID playerId = player.getUniqueId();
        PlayerVoteData playerData = new PlayerVoteData(playerId, "TestPlayer");

        // Set a streak
        playerData.setCurrentStreak(5);

        // Test giving streak rewards
        assertDoesNotThrow(() -> {
            rewardManager.giveVoteRewards(player, "testsite", playerData);
        }, "Streak rewards should be given without errors");
    }

    @Test
    @DisplayName("Should handle VIP rewards")
    void testVIPRewards() {
        // Give player VIP permission
        player.addAttachment(plugin, "votingmatters.vip", true);

        UUID playerId = player.getUniqueId();
        PlayerVoteData playerData = new PlayerVoteData(playerId, "TestPlayer");

        // Test giving VIP rewards
        assertDoesNotThrow(() -> {
            rewardManager.giveVoteRewards(player, "testsite", playerData);
        }, "VIP rewards should be given without errors");
    }

    @Test
    @DisplayName("Should handle weekend rewards")
    void testWeekendRewards() {
        UUID playerId = player.getUniqueId();
        PlayerVoteData playerData = new PlayerVoteData(playerId, "TestPlayer");

        // Test giving weekend rewards (if it's weekend)
        assertDoesNotThrow(() -> {
            rewardManager.giveVoteRewards(player, "testsite", playerData);
        }, "Weekend rewards should be handled without errors");
    }

    @Test
    @DisplayName("Should handle site-specific rewards")
    void testSiteSpecificRewards() {
        UUID playerId = player.getUniqueId();
        PlayerVoteData playerData = new PlayerVoteData(playerId, "TestPlayer");

        // Test giving rewards for specific site
        assertDoesNotThrow(() -> {
            rewardManager.giveVoteRewards(player, "minecraftservers", playerData);
            rewardManager.giveVoteRewards(player, "minecraft-mp", playerData);
            rewardManager.giveVoteRewards(player, "planetminecraft", playerData);
        }, "Site-specific rewards should be given without errors");
    }

    @Test
    @DisplayName("Should reload rewards configuration")
    void testReloadRewards() {
        // Test reloading rewards configuration
        assertDoesNotThrow(() -> {
            rewardManager.loadRewards();
        }, "Reloading rewards should not throw exceptions");
    }

    @Test
    @DisplayName("Should handle null parameters gracefully")
    void testNullParameterHandling() {
        UUID playerId = player.getUniqueId();
        PlayerVoteData playerData = new PlayerVoteData(playerId, "TestPlayer");

        // Test with null player
        assertDoesNotThrow(() -> {
            rewardManager.giveVoteRewards(null, "testsite", playerData);
        }, "Should handle null player gracefully");

        // Test with null site name
        assertDoesNotThrow(() -> {
            rewardManager.giveVoteRewards(player, null, playerData);
        }, "Should handle null site name gracefully");

        // Test with null player data
        assertDoesNotThrow(() -> {
            rewardManager.giveVoteRewards(player, "testsite", null);
        }, "Should handle null player data gracefully");
    }

    @Test
    @DisplayName("Should handle economy integration")
    void testEconomyIntegration() {
        UUID playerId = player.getUniqueId();
        PlayerVoteData playerData = new PlayerVoteData(playerId, "TestPlayer");

        // Test economy rewards (if economy is available)
        assertDoesNotThrow(() -> {
            rewardManager.giveVoteRewards(player, "testsite", playerData);
        }, "Economy integration should work without errors");

        // Check if player has initial balance (MockBukkit might not have full Vault
        // support)
        // This test mainly ensures no exceptions are thrown
    }

    @Test
    @DisplayName("Should handle reward multipliers")
    void testRewardMultipliers() {
        UUID playerId = player.getUniqueId();
        PlayerVoteData playerData = new PlayerVoteData(playerId, "TestPlayer");

        // Give player multiplier permission
        player.addAttachment(plugin, "votingmatters.multiplier.2x", true);

        // Test giving rewards with multiplier
        assertDoesNotThrow(() -> {
            rewardManager.giveVoteRewards(player, "testsite", playerData);
        }, "Reward multipliers should work without errors");
    }
}
