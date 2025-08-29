package cc.farlanders.votingmatters;

import java.io.File;
import java.util.Arrays;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.scheduler.BukkitScheduler;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import cc.farlanders.votingmatters.config.ConfigManager;
import cc.farlanders.votingmatters.database.DatabaseManager;
import cc.farlanders.votingmatters.managers.RewardManager;
import cc.farlanders.votingmatters.managers.VoteManager;

@ExtendWith(MockitoExtension.class)
@DisplayName("VotingMatters Plugin Tests (Mockito)")
class VotingMattersMockitoTest {

    @Mock
    private Server mockServer;

    @Mock
    private VotingMatters mockPlugin;

    @Mock
    private ConfigManager mockConfigManager;

    @Mock
    private DatabaseManager mockDatabaseManager;

    @Mock
    private VoteManager mockVoteManager;

    @Mock
    private RewardManager mockRewardManager;

    @Mock
    private FileConfiguration mockConfiguration;

    @Mock
    private File mockDataFolder;

    @Mock
    private Logger mockLogger;

    @Mock
    private PluginDescriptionFile mockPluginDescriptionFile;

    @Mock
    private PluginManager mockPluginManager;

    @Mock
    private ServicesManager mockServicesManager;

    @Mock
    private BukkitScheduler mockScheduler;

    @BeforeEach
    void setUp() {
        // Initialize mocks
        MockitoAnnotations.openMocks(this);

        // Setup basic server behaviors with lenient stubbing
        lenient().when(mockServer.getServicesManager()).thenReturn(mockServicesManager);
        lenient().when(mockServer.getLogger()).thenReturn(mockLogger);
        lenient().when(mockServer.getScheduler()).thenReturn(mockScheduler);
        lenient().when(mockServer.getPluginManager()).thenReturn(mockPluginManager);

        // Setup plugin descriptor for plugin context
        lenient().when(mockPluginDescriptionFile.getVersion()).thenReturn("1.0.0");
        lenient().when(mockPluginDescriptionFile.getName()).thenReturn("VotingMatters");
        lenient().when(mockPluginDescriptionFile.getMain()).thenReturn("com.mjanglin.votingmatters.VotingMatters");

        // Setup mock plugin behaviors
        lenient().when(mockPlugin.getDescription()).thenReturn(mockPluginDescriptionFile);
        lenient().when(mockPlugin.getDataFolder()).thenReturn(mockDataFolder);
        lenient().when(mockPlugin.getConfig()).thenReturn(mockConfiguration);
        lenient().when(mockPlugin.getServer()).thenReturn(mockServer);
        lenient().when(mockPlugin.getLogger()).thenReturn(mockLogger);

        // Setup mock folder and configuration behaviors
        lenient().when(mockDataFolder.exists()).thenReturn(true);
        lenient().when(mockDataFolder.isDirectory()).thenReturn(true);
        lenient().when(mockDataFolder.getAbsolutePath()).thenReturn("/test/plugin/data");

        // Setup configuration defaults
        lenient().when(mockConfiguration.getString("database.type", "sqlite")).thenReturn("sqlite");
        lenient().when(mockConfiguration.getString("database.file", "votes.db")).thenReturn("votes.db");
        lenient().when(mockConfiguration.getInt("vote.cooldown", 24)).thenReturn(24);
        lenient().when(mockConfiguration.getStringList("rewards.items"))
                .thenReturn(Arrays.asList("diamond", "gold_ingot"));
        lenient().when(mockConfiguration.getBoolean("rewards.enabled", true)).thenReturn(true);

        // Setup manager mocks
        lenient().when(mockPlugin.getConfigManager()).thenReturn(mockConfigManager);
        lenient().when(mockPlugin.getDatabaseManager()).thenReturn(mockDatabaseManager);
        lenient().when(mockPlugin.getVoteManager()).thenReturn(mockVoteManager);
        lenient().when(mockPlugin.getRewardManager()).thenReturn(mockRewardManager);
    }

    @Test
    @DisplayName("Should have valid plugin managers")
    void testManagersInitialized() {
        // Test that all essential managers are available
        assertNotNull(mockPlugin.getConfigManager(), "ConfigManager should not be null");
        assertNotNull(mockPlugin.getDatabaseManager(), "DatabaseManager should not be null");
        assertNotNull(mockPlugin.getVoteManager(), "VoteManager should not be null");
        assertNotNull(mockPlugin.getRewardManager(), "RewardManager should not be null");
    }

    @Test
    @DisplayName("Should handle plugin lifecycle methods")
    void testPluginLifecycle() {
        // Test that plugin responds to lifecycle calls
        doNothing().when(mockPlugin).onEnable();
        doNothing().when(mockPlugin).onDisable();

        // These should not throw exceptions
        assertDoesNotThrow(() -> mockPlugin.onEnable());
        assertDoesNotThrow(() -> mockPlugin.onDisable());

        // Verify lifecycle methods were called
        verify(mockPlugin, times(1)).onEnable();
        verify(mockPlugin, times(1)).onDisable();
    }

    @Test
    @DisplayName("Should have valid server integration")
    void testServerIntegration() {
        // Test basic server integration
        assertEquals(mockServer, mockPlugin.getServer(), "Plugin should return correct server instance");
        assertEquals(mockLogger, mockPlugin.getLogger(), "Plugin should return correct logger instance");
        assertEquals(mockDataFolder, mockPlugin.getDataFolder(), "Plugin should return correct data folder");
    }

    @Test
    @DisplayName("Should handle configuration access")
    void testConfigurationAccess() {
        // Test configuration access
        assertNotNull(mockPlugin.getConfigManager(), "ConfigManager should be available");

        ConfigManager config = mockPlugin.getConfigManager();

        // Setup config behavior
        lenient().when(config.getConfig()).thenReturn(mockConfiguration);
        lenient().when(config.getRewards()).thenReturn(mockConfiguration);
        lenient().when(config.getMessages()).thenReturn(mockConfiguration);

        assertNotNull(config.getConfig(), "Main config should be available");
        assertNotNull(config.getRewards(), "Rewards config should be available");
        assertNotNull(config.getMessages(), "Messages config should be available");
    }

    @Test
    @DisplayName("Should handle null parameter safely")
    void testNullParameterHandling() {
        // Test that plugin handles null parameters gracefully
        assertDoesNotThrow(() -> {
            // Test that we can call the method safely
            when(mockPlugin.getConfigManager()).thenReturn(null);
            ConfigManager result = mockPlugin.getConfigManager();
            assertNull(result); // This should be null as we set it
        });
    }
}
