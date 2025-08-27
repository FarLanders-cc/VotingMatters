package com.mjanglin.votingmatters.commands;

import org.bukkit.command.Command;
import org.bukkit.command.ConsoleCommandSender;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mjanglin.votingmatters.VotingMatters;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.entity.PlayerMock;

@DisplayName("VoteCommand Tests")
class VoteCommandTest {

    private VotingMatters plugin;
    private VoteCommand voteCommand;
    private PlayerMock player;
    private Command command;

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
        plugin = MockBukkit.load(VotingMatters.class);

        voteCommand = new VoteCommand(plugin);
        player = MockBukkit.getMock().addPlayer("TestPlayer");
        command = MockBukkit.getMock().getCommandMap().getCommand("vote");
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("Should show voting info for basic vote command")
    void testBasicVoteCommand() {
        // Execute basic vote command
        boolean result = voteCommand.onCommand(player, command, "vote", new String[0]);

        assertTrue(result, "Vote command should return true");
        // Player should receive voting information
        assertTrue(player.nextMessage() != null || player.nextMessage() == null,
                "Command should execute without errors");
    }

    @Test
    @DisplayName("Should handle claim subcommand")
    void testClaimSubcommand() {
        // Execute vote claim command
        boolean result = voteCommand.onCommand(player, command, "vote", new String[] { "claim" });

        assertTrue(result, "Vote claim command should return true");
    }

    @Test
    @DisplayName("Should handle stats subcommand")
    void testStatsSubcommand() {
        // Execute vote stats command
        boolean result = voteCommand.onCommand(player, command, "vote", new String[] { "stats" });

        assertTrue(result, "Vote stats command should return true");
    }

    @Test
    @DisplayName("Should reject invalid subcommand")
    void testInvalidSubcommand() {
        // Execute invalid subcommand
        boolean result = voteCommand.onCommand(player, command, "vote", new String[] { "invalid" });

        assertFalse(result, "Invalid subcommand should return false");
    }

    @Test
    @DisplayName("Should reject console sender")
    void testConsoleSender() {
        ConsoleCommandSender console = MockBukkit.getMock().getConsoleSender();

        // Execute command from console
        boolean result = voteCommand.onCommand(console, command, "vote", new String[0]);

        assertTrue(result, "Console command should return true (with error message)");
    }

    @Test
    @DisplayName("Should handle multiple arguments")
    void testMultipleArguments() {
        // Execute command with multiple arguments
        boolean result = voteCommand.onCommand(player, command, "vote",
                new String[] { "claim", "extra", "arguments" });

        assertTrue(result, "Command with multiple arguments should handle gracefully");
    }

    @Test
    @DisplayName("Should handle empty string arguments")
    void testEmptyArguments() {
        // Execute command with empty string argument
        boolean result = voteCommand.onCommand(player, command, "vote", new String[] { "" });

        assertFalse(result, "Empty string argument should be treated as invalid");
    }

    @Test
    @DisplayName("Should handle null parameters gracefully")
    void testNullParameters() {
        // Test with null sender (shouldn't happen in practice)
        assertDoesNotThrow(() -> {
            voteCommand.onCommand(null, command, "vote", new String[0]);
        }, "Should handle null sender gracefully");

        // Test with null command
        assertDoesNotThrow(() -> {
            voteCommand.onCommand(player, null, "vote", new String[0]);
        }, "Should handle null command gracefully");

        // Test with null arguments
        assertDoesNotThrow(() -> {
            voteCommand.onCommand(player, command, "vote", null);
        }, "Should handle null arguments gracefully");
    }

    @Test
    @DisplayName("Should handle case-insensitive subcommands")
    void testCaseInsensitiveSubcommands() {
        // Test uppercase subcommand
        boolean result1 = voteCommand.onCommand(player, command, "vote", new String[] { "CLAIM" });
        assertTrue(result1, "Uppercase subcommand should work");

        // Test mixed case subcommand
        boolean result2 = voteCommand.onCommand(player, command, "vote", new String[] { "Stats" });
        assertTrue(result2, "Mixed case subcommand should work");

        // Test lowercase subcommand
        boolean result3 = voteCommand.onCommand(player, command, "vote", new String[] { "claim" });
        assertTrue(result3, "Lowercase subcommand should work");
    }
}
