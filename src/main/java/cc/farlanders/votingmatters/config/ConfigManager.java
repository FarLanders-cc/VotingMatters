package cc.farlanders.votingmatters.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import cc.farlanders.votingmatters.VotingMatters;

public class ConfigManager {

    private final VotingMatters plugin;
    private FileConfiguration config;
    private FileConfiguration rewards;
    private FileConfiguration messages;

    public ConfigManager(VotingMatters plugin) {
        this.plugin = plugin;
        setupConfigs();
    }

    private void setupConfigs() {
        // Main config
        plugin.saveDefaultConfig();
        this.config = plugin.getConfig();

        // Rewards config
        createCustomConfig("rewards.yml");
        this.rewards = getCustomConfig("rewards.yml");

        // Messages config
        createCustomConfig("messages.yml");
        this.messages = getCustomConfig("messages.yml");

        // Update configs with defaults
        updateConfigDefaults();
    }

    private void createCustomConfig(String fileName) {
        File file = new File(plugin.getDataFolder(), fileName);
        if (!file.exists()) {
            plugin.saveResource(fileName, false);
        }
    }

    private FileConfiguration getCustomConfig(String fileName) {
        File file = new File(plugin.getDataFolder(), fileName);
        return YamlConfiguration.loadConfiguration(file);
    }

    private void updateConfigDefaults() {
        // Update config.yml with defaults from resource
        try (InputStream defaultStream = plugin.getResource("config.yml")) {
            if (defaultStream != null) {
                YamlConfiguration defaultConfig = YamlConfiguration
                        .loadConfiguration(new InputStreamReader(defaultStream));
                config.setDefaults(defaultConfig);
                config.options().copyDefaults(true);
                plugin.saveConfig();
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Could not load default config: {0}", e.getMessage());
        }

        // Update rewards.yml with defaults
        try (InputStream defaultStream = plugin.getResource("rewards.yml")) {
            if (defaultStream != null) {
                YamlConfiguration defaultRewards = YamlConfiguration
                        .loadConfiguration(new InputStreamReader(defaultStream));
                rewards.setDefaults(defaultRewards);
                rewards.options().copyDefaults(true);
                saveCustomConfig(rewards, "rewards.yml");
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Could not load default rewards config: {0}", e.getMessage());
        }

        // Update messages.yml with defaults
        try (InputStream defaultStream = plugin.getResource("messages.yml")) {
            if (defaultStream != null) {
                YamlConfiguration defaultMessages = YamlConfiguration
                        .loadConfiguration(new InputStreamReader(defaultStream));
                messages.setDefaults(defaultMessages);
                messages.options().copyDefaults(true);
                saveCustomConfig(messages, "messages.yml");
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Could not load default messages config: {0}", e.getMessage());
        }
    }

    private void saveCustomConfig(FileConfiguration config, String fileName) {
        try {
            config.save(new File(plugin.getDataFolder(), fileName));
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save {0}: {1}", new Object[] { fileName, e.getMessage() });
        }
    }

    public void reload() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
        this.rewards = getCustomConfig("rewards.yml");
        this.messages = getCustomConfig("messages.yml");
        updateConfigDefaults();
    }

    // Getters
    public FileConfiguration getConfig() {
        return config;
    }

    public FileConfiguration getRewards() {
        return rewards;
    }

    public FileConfiguration getMessages() {
        return messages;
    }
}
