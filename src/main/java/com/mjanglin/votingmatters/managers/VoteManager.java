package com.mjanglin.votingmatters.managers;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.mjanglin.votingmatters.VotingMatters;
import com.mjanglin.votingmatters.api.VoteAPI;
import com.mjanglin.votingmatters.api.impl.MinecraftMPAPI;
import com.mjanglin.votingmatters.api.impl.MinecraftServersAPI;
import com.mjanglin.votingmatters.api.impl.PlanetMinecraftAPI;
import com.mjanglin.votingmatters.api.impl.TopMinecraftServersAPI;
import com.mjanglin.votingmatters.database.DatabaseManager;
import com.mjanglin.votingmatters.events.PlayerVoteEvent;
import com.mjanglin.votingmatters.models.PlayerVoteData;
import com.mjanglin.votingmatters.models.VoteRecord;

public class VoteManager {

    private final VotingMatters plugin;
    private final Map<String, VoteAPI> voteAPIs;

    public VoteManager(VotingMatters plugin) {
        this.plugin = plugin;
        this.voteAPIs = new HashMap<>();
        initializeAPIs();
    }

    private void initializeAPIs() {
        // Initialize voting site APIs based on configuration
        if (plugin.getConfig().getBoolean("vote-sites.minecraftservers.enabled", false)) {
            voteAPIs.put("minecraftservers", new MinecraftServersAPI(plugin));
        }

        if (plugin.getConfig().getBoolean("vote-sites.minecraftmp.enabled", false)) {
            voteAPIs.put("minecraftmp", new MinecraftMPAPI(plugin));
        }

        if (plugin.getConfig().getBoolean("vote-sites.planetminecraft.enabled", false)) {
            voteAPIs.put("planetminecraft", new PlanetMinecraftAPI(plugin));
        }

        if (plugin.getConfig().getBoolean("vote-sites.topminecraftservers.enabled", false)) {
            voteAPIs.put("topminecraftservers", new TopMinecraftServersAPI(plugin));
        }

        plugin.getLogger().log(Level.INFO, "Initialized {0} voting site APIs", voteAPIs.size());
    }

    public CompletableFuture<Void> checkForVotes() {
        return CompletableFuture.runAsync(() -> {
            for (Map.Entry<String, VoteAPI> entry : voteAPIs.entrySet()) {
                String siteName = entry.getKey();
                VoteAPI api = entry.getValue();

                try {
                    List<VoteRecord> newVotes = api.checkForNewVotes();
                    for (VoteRecord vote : newVotes) {
                        processVote(vote);
                    }
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "Failed to check votes for {0}: {1}",
                            new Object[] { siteName, e.getMessage() });
                }
            }
        });
    }

    public CompletableFuture<Void> processVote(VoteRecord voteRecord) {
        return CompletableFuture.runAsync(() -> {
            // Store the vote record
            DatabaseManager dbManager = plugin.getDatabaseManager();
            dbManager.addVoteRecord(voteRecord);

            // Get player data
            dbManager.getPlayerData(voteRecord.getPlayerUuid())
                    .thenAccept(existingPlayerData -> {
                        PlayerVoteData playerData = existingPlayerData;
                        if (playerData == null) {
                            // Create new player data if doesn't exist
                            Player player = Bukkit.getPlayer(voteRecord.getPlayerUuid());
                            String username = player != null ? player.getName() : "Unknown";
                            playerData = new PlayerVoteData(voteRecord.getPlayerUuid(), username);
                        }

                        // Update vote statistics
                        updatePlayerVoteStats(playerData);

                        // Save updated data
                        dbManager.savePlayerData(playerData);

                        // Fire vote event
                        PlayerVoteEvent voteEvent = new PlayerVoteEvent(
                                voteRecord.getPlayerUuid(),
                                voteRecord.getSiteName(),
                                playerData);

                        final PlayerVoteData finalPlayerData = playerData;
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            Bukkit.getPluginManager().callEvent(voteEvent);

                            // Give rewards if player is online
                            Player player = Bukkit.getPlayer(voteRecord.getPlayerUuid());
                            if (player != null && player.isOnline()) {
                                plugin.getRewardManager().giveVoteRewards(player, voteRecord.getSiteName(),
                                        finalPlayerData);
                            } else {
                                // Store as offline vote
                                finalPlayerData.addOfflineVote();
                                dbManager.savePlayerData(finalPlayerData);
                            }
                        });
                    });
        });
    }

    private void updatePlayerVoteStats(PlayerVoteData playerData) {
        playerData.addVote();
        playerData.setLastVoteTime(new Timestamp(System.currentTimeMillis()));

        // Update streak
        if (shouldIncrementStreak(playerData)) {
            playerData.incrementStreak();
        } else if (shouldResetStreak(playerData)) {
            playerData.resetStreak();
            playerData.incrementStreak(); // Start new streak
        }
    }

    private boolean shouldIncrementStreak(PlayerVoteData playerData) {
        if (playerData.getLastVoteTime() == null) {
            return true; // First vote
        }

        long hoursSinceLastVote = (System.currentTimeMillis() - playerData.getLastVoteTime().getTime())
                / (1000 * 60 * 60);
        return hoursSinceLastVote >= 20 && hoursSinceLastVote <= 48; // 20-48 hours is valid for streak
    }

    private boolean shouldResetStreak(PlayerVoteData playerData) {
        if (playerData.getLastVoteTime() == null) {
            return false;
        }

        long hoursSinceLastVote = (System.currentTimeMillis() - playerData.getLastVoteTime().getTime())
                / (1000 * 60 * 60);
        return hoursSinceLastVote > 48; // More than 48 hours resets streak
    }

    public CompletableFuture<Boolean> hasVotedRecently(UUID playerUuid, String siteName) {
        return plugin.getDatabaseManager().hasVotedOnSite(playerUuid, siteName, 24)
                .exceptionally(throwable -> {
                    plugin.getLogger()
                            .log(Level.WARNING, "Failed to check if player has voted recently: {0}",
                                    throwable.getMessage());
                    return false;
                });
    }

    public CompletableFuture<Void> processOfflineVotes(Player player) {
        return CompletableFuture.runAsync(() -> plugin.getDatabaseManager().getUnrewardedVotes(player.getUniqueId())
                .thenAccept(unrewardedVotes -> {
                    if (!unrewardedVotes.isEmpty()) {
                        // Get player data
                        plugin.getDatabaseManager().getPlayerData(player.getUniqueId())
                                .thenAccept(playerData -> {
                                    if (playerData != null) {
                                        Bukkit.getScheduler().runTask(plugin, () -> {
                                            // Give rewards for each unrewarded vote
                                            for (VoteRecord vote : unrewardedVotes) {
                                                plugin.getRewardManager().giveVoteRewards(player,
                                                        vote.getSiteName(), playerData);
                                                plugin.getDatabaseManager().markVoteRewarded(vote.getId());
                                            }

                                            // Clear offline votes
                                            playerData.clearOfflineVotes();
                                            plugin.getDatabaseManager().savePlayerData(playerData);
                                        });
                                    }
                                });
                    }
                }));
    }

    public Map<String, String> getVotingSites() {
        Map<String, String> sites = new HashMap<>();

        for (String siteName : voteAPIs.keySet()) {
            String url = plugin.getConfig().getString("vote-sites." + siteName + ".url", "");
            if (url != null && !url.isEmpty()) {
                sites.put(siteName, url);
            }
        }

        return sites;
    }

    public Set<String> getEnabledSites() {
        return voteAPIs.keySet();
    }

    public void reload() {
        voteAPIs.clear();
        initializeAPIs();
    }
}
