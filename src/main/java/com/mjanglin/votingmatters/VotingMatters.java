package com.mjanglin.votingmatters;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.mjanglin.votingmatters.commands.VoteCheckCommand;
import com.mjanglin.votingmatters.commands.VoteCommand;
import com.mjanglin.votingmatters.commands.VoteReloadCommand;
import com.mjanglin.votingmatters.commands.VoteRewardCommand;
import com.mjanglin.votingmatters.commands.VoteStatsCommand;
import com.mjanglin.votingmatters.commands.VoteTopCommand;
import com.mjanglin.votingmatters.config.ConfigManager;
import com.mjanglin.votingmatters.database.DatabaseManager;
import com.mjanglin.votingmatters.listeners.PlayerListener;
import com.mjanglin.votingmatters.listeners.VoteListener;
import com.mjanglin.votingmatters.managers.RewardManager;
import com.mjanglin.votingmatters.managers.VoteManager;
import com.mjanglin.votingmatters.placeholders.VotingPlaceholders;
import com.mjanglin.votingmatters.tasks.VoteCheckTask;
import com.mjanglin.votingmatters.utils.MessageUtils;

import net.milkbowl.vault.economy.Economy;

public class VotingMatters extends JavaPlugin {

    private static volatile VotingMatters instance;

    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private VoteManager voteManager;
    private RewardManager rewardManager;
    private Economy economy;
    private boolean placeholderAPIEnabled;

    @Override
    public void onEnable() {
        if (instance != null) {
            getLogger().warning("Plugin instance already exists!");
        }
        instance = this;

        // Initialize managers
        this.configManager = new ConfigManager(this);
        this.databaseManager = new DatabaseManager(this);
        this.voteManager = new VoteManager(this);
        this.rewardManager = new RewardManager(this);

        // Initialize MessageUtils
        MessageUtils.initialize(this);

        // Setup economy
        if (!setupEconomy()) {
            getLogger().warning("Vault economy not found! Some rewards may not work.");
        }

        // Check for PlaceholderAPI
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholderAPIEnabled = true;
            new VotingPlaceholders(this).register();
            getLogger().info("PlaceholderAPI support enabled!");
        }

        // Register commands
        registerCommands();

        // Register listeners
        registerListeners();

        // Start vote checking task
        new VoteCheckTask(this).runTaskTimerAsynchronously(this, 20L,
                configManager.getConfig().getInt("vote-check.interval", 300) * 20L);
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.close();
        }
        instance = null;
        getLogger().info("VotingMatters has been disabled!");
    }

    private void registerCommands() {
        getCommand("vote").setExecutor(new VoteCommand(this));
        getCommand("votereward").setExecutor(new VoteRewardCommand());
        getCommand("votecheck").setExecutor(new VoteCheckCommand(this));
        getCommand("votestats").setExecutor(new VoteStatsCommand(this));
        getCommand("votereload").setExecutor(new VoteReloadCommand(this));
        getCommand("votetop").setExecutor(new VoteTopCommand(this));
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
        Bukkit.getPluginManager().registerEvents(new VoteListener(this), this);
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    public void reload() {
        try {
            configManager.reload();
            getLogger().info("Configuration reloaded successfully!");
        } catch (Exception e) {
            getLogger().severe("Error reloading configuration: " + e.getMessage());
        }
    }
    // Getters

    // Getters
    public static VotingMatters getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Plugin not initialized");
        }
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public VoteManager getVoteManager() {
        return voteManager;
    }

    public RewardManager getRewardManager() {
        return rewardManager;
    }

    public Economy getEconomy() {
        return economy;
    }

    public boolean isPlaceholderAPIEnabled() {
        return placeholderAPIEnabled;
    }
}
