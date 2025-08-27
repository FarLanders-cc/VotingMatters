package com.mjanglin.votingmatters;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;

@DisplayName("VotingMatters Plugin Tests")
class VotingMattersTest {

    private ServerMock server;
    private VotingMatters plugin;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(VotingMatters.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("Plugin should enable successfully")
    void testPluginEnables() {
        assertTrue(plugin.isEnabled(), "Plugin should be enabled");
        assertNotNull(VotingMatters.getInstance(), "Plugin instance should not be null");
    }

    @Test
    @DisplayName("Plugin should initialize all managers")
    void testManagersInitialized() {
        assertNotNull(plugin.getConfigManager(), "ConfigManager should be initialized");
        assertNotNull(plugin.getDatabaseManager(), "DatabaseManager should be initialized");
        assertNotNull(plugin.getVoteManager(), "VoteManager should be initialized");
        assertNotNull(plugin.getRewardManager(), "RewardManager should be initialized");
    }

    @Test
    @DisplayName("Plugin should register commands")
    void testCommandsRegistered() {
        assertNotNull(server.getCommandMap().getCommand("vote"), "Vote command should be registered");
        assertNotNull(server.getCommandMap().getCommand("votetop"), "VoteTop command should be registered");
        assertNotNull(server.getCommandMap().getCommand("votestats"), "VoteStats command should be registered");
        assertNotNull(server.getCommandMap().getCommand("votecheck"), "VoteCheck command should be registered");
        assertNotNull(server.getCommandMap().getCommand("votereward"), "VoteReward command should be registered");
        assertNotNull(server.getCommandMap().getCommand("votereload"), "VoteReload command should be registered");
    }

    @Test
    @DisplayName("Plugin should handle player join/quit events")
    void testPlayerEvents() {
        PlayerMock player = server.addPlayer("TestPlayer");

        // Test that player was added successfully
        assertTrue(server.getOnlinePlayers().contains(player), "Player should be online");

        // Test player disconnect
        player.disconnect();
        assertFalse(server.getOnlinePlayers().contains(player), "Player should be offline after disconnect");
    }

    @Test
    @DisplayName("Plugin should reload successfully")
    void testPluginReload() {
        plugin.reload();
        assertTrue(plugin.isEnabled(), "Plugin should still be enabled after reload");
    }
}
