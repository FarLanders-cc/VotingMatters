package com.mjanglin.votingmatters.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mjanglin.votingmatters.VotingMatters;
import com.mjanglin.votingmatters.utils.MessageUtils;

public class VoteStatsCommand implements CommandExecutor {

    private final VotingMatters plugin;

    public VoteStatsCommand(VotingMatters plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender == null) {
            return false;
        }

        Player target;
        if (args.length > 0) {
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                String message = MessageUtils.getMessage("errors.player-not-found")
                        .replace("%player%", args[0]);
                sender.sendMessage(message);
                return true;
            }
        } else if (sender instanceof Player player) {
            target = player;
        } else {
            sender.sendMessage("Console must specify a player name.");
            return true;
        }

        // Show detailed stats for target player
        plugin.getDatabaseManager().getPlayerData(target.getUniqueId())
                .thenAccept(playerData -> {
                    if (playerData == null) {
                        sender.sendMessage(MessageUtils.getMessage("stats.no-data"));
                        return;
                    }

                    sender.sendMessage(MessageUtils.getMessage("stats.header"));
                    sender.sendMessage(MessageUtils.getMessage("stats.player-stats")
                            .replace("%player%", target.getName()));
                    sender.sendMessage(MessageUtils.getMessage("stats.total-votes")
                            .replace("%total%", String.valueOf(playerData.getTotalVotes())));
                    sender.sendMessage(MessageUtils.getMessage("stats.current-streak")
                            .replace("%streak%", String.valueOf(playerData.getCurrentStreak())));
                    sender.sendMessage(MessageUtils.getMessage("stats.best-streak")
                            .replace("%best_streak%", String.valueOf(playerData.getBestStreak())));

                    if (playerData.getLastVoteTime() != null) {
                        sender.sendMessage(MessageUtils.getMessage("stats.last-vote")
                                .replace("%last_vote%", playerData.getLastVoteTime().toString()));
                    }
                });

        return true;
    }
}
