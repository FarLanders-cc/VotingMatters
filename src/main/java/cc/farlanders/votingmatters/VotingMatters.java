package cc.farlanders.votingmatters;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import cc.farlanders.votingmatters.commands.VoteCheckCommand;
import cc.farlanders.votingmatters.commands.VoteCommand;
import cc.farlanders.votingmatters.commands.VoteReloadCommand;
import cc.farlanders.votingmatters.commands.VoteRewardCommand;
import cc.farlanders.votingmatters.commands.VoteStatsCommand;
import cc.farlanders.votingmatters.commands.VoteTopCommand;
import cc.farlanders.votingmatters.config.ConfigManager;
import cc.farlanders.votingmatters.database.DatabaseManager;
import cc.farlanders.votingmatters.listeners.PlayerListener;
import cc.farlanders.votingmatters.listeners.VoteListener;
import cc.farlanders.votingmatters.managers.RewardManager;
import cc.farlanders.votingmatters.managers.VoteManager;
import cc.farlanders.votingmatters.placeholders.VotingPlaceholders;
import cc.farlanders.votingmatters.tasks.VoteCheckTask;
import cc.farlanders.votingmatters.utils.MessageUtils;
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
        getLogger().info("VotingMatters has been disabled!");
    }

    private void registerCommands() {
        if (getCommand("vote") != null) {
            getCommand("vote").setExecutor(new VoteCommand(this));
        }
        if (getCommand("votereward") != null) {
            getCommand("votereward").setExecutor(new VoteRewardCommand());
        }
        if (getCommand("votecheck") != null) {
            getCommand("votecheck").setExecutor(new VoteCheckCommand(this));
        }
        if (getCommand("votestats") != null) {
            getCommand("votestats").setExecutor(new VoteStatsCommand(this));
        }
        if (getCommand("votereload") != null) {
            getCommand("votereload").setExecutor(new VoteReloadCommand(this));
        }
        if (getCommand("votetop") != null) {
            getCommand("votetop").setExecutor(new VoteTopCommand(this));
        }
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
            getLogger().log(Level.SEVERE, "Error reloading configuration: {0}", e.getMessage());
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
