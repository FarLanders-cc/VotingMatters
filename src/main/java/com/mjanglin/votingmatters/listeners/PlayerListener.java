package com.mjanglin.votingmatters.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.mjanglin.votingmatters.VotingMatters;
import com.mjanglin.votingmatters.utils.MessageUtils;

public class PlayerListener implements Listener {

    private final VotingMatters plugin;

    public PlayerListener(VotingMatters plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Check for offline votes
        plugin.getDatabaseManager().getPlayerData(player.getUniqueId())
                .thenAccept(playerData -> {
                    if (playerData != null && playerData.getOfflineVotes() > 0) {
                        // Delay the message slightly
                        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                            String message = MessageUtils.getMessage("offline.claim-available")
                                    .replace("%count%", String.valueOf(playerData.getOfflineVotes()));
                            player.sendMessage(message);
                        }, 100L); // 5 seconds delay
                    }
                });

        // Send join reminder if enabled
        if (plugin.getConfigManager().getConfig().getBoolean("notifications.join-reminders.enabled", true)) {
            long delay = plugin.getConfigManager().getConfig().getLong("notifications.join-reminders.delay", 100);

            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                player.sendMessage(MessageUtils.getMessage("reminders.join"));
            }, delay);
        }
    }
}
