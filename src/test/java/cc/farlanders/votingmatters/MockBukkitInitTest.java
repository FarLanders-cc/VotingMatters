package cc.farlanders.votingmatters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;

@DisplayName("MockBukkit Initialization Test")
class MockBukkitInitTest {

    @Test
    @DisplayName("Should initialize MockBukkit without errors")
    void testMockBukkitInitialization() {
        ServerMock server = null;
        try {
            // Test basic MockBukkit initialization
            System.setProperty("MockBukkit.enableStackTraces", "true");

            server = MockBukkit.mock();

            // Simple verification that server is working
            assertNotNull(server, "Server should not be null");
            assertEquals("MockBukkit", server.getName(), "Server name should be MockBukkit");

            // Test that we can get basic server info
            assertNotNull(server.getVersion(), "Server version should not be null");

            System.out.println("✅ MockBukkit basic initialization successful!");

        } catch (Exception e) {
            fail("MockBukkit initialization failed: " + e.getMessage());
        } finally {
            if (server != null) {
                try {
                    MockBukkit.unmock();
                } catch (Exception e) {
                    System.err.println("Warning: Cleanup error: " + e.getMessage());
                }
            }
        }
    }

    @Test
    @DisplayName("Should create players without errors")
    void testPlayerCreation() {
        ServerMock server = null;
        try {
            server = MockBukkit.mock();

            // Try to create a player - this is where PotionEffectType issues often occur
            var player = server.addPlayer("TestPlayer");
            assertNotNull(player, "Player should not be null");
            assertEquals("TestPlayer", player.getName(), "Player name should match");

            System.out.println("✅ MockBukkit player creation successful!");

        } catch (Exception e) {
            // If player creation fails due to PotionEffectType, we'll know that's the issue
            System.err.println("❌ Player creation failed (likely PotionEffectType issue): " + e.getMessage());
            fail("Player creation failed: " + e.getMessage());
        } finally {
            if (server != null) {
                try {
                    MockBukkit.unmock();
                } catch (Exception e) {
                    System.err.println("Warning: Cleanup error: " + e.getMessage());
                }
            }
        }
    }
}
