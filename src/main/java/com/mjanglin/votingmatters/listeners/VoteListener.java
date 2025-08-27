package com.mjanglin.votingmatters.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.mjanglin.votingmatters.VotingMatters;
import com.mjanglin.votingmatters.events.PlayerVoteEvent;
import com.mjanglin.votingmatters.utils.MessageUtils;

public class VoteListener implements Listener {

    private final VotingMatters plugin;

    public VoteListener(VotingMatters plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerVote(PlayerVoteEvent event) {
        if (event.isCancelled())
            return;

        Player player = Bukkit.getPlayer(event.getPlayerUuid());
        if (player == null || !player.isOnline())
            return;

        // Send thank you message
        String thankYou = MessageUtils.getMessage("detection.thank-you")
                .replace("%player%", player.getName());
        player.sendMessage(thankYou);

        // Broadcast vote if enabled
        if (plugin.getConfigManager().getConfig().getBoolean("notifications.broadcast-votes", true)) {
            String broadcast = MessageUtils.getMessage("detection.vote-detected")
                    .replace("%player%", player.getName())
                    .replace("%site%", event.getSiteName());
            Bukkit.getServer().broadcast(net.kyori.adventure.text.Component.text(broadcast));
        }
    }
}
