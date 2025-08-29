package cc.farlanders.votingmatters.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import cc.farlanders.votingmatters.VotingMatters;
import cc.farlanders.votingmatters.utils.MessageUtils;

public class VoteCheckCommand implements CommandExecutor {

    private final VotingMatters plugin;

    public VoteCheckCommand(VotingMatters plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("votingmatters.check")) {
            sender.sendMessage(MessageUtils.getMessage("errors.no-permission"));
            return true;
        }

        Player target;
        if (args.length > 0) {
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                String message = MessageUtils.getMessage("errors.player-not-found")
                        .replace("%player%", args[0]);
                sender.sendMessage(message);
                return false;
            }
        } else if (sender instanceof Player player) {
            target = player;
        } else {
            sender.sendMessage("Console must specify a player name.");
            return false;
        }

        // Show vote check info for target player
        plugin.getDatabaseManager().getPlayerData(target.getUniqueId())
                .thenAccept(playerData -> {
                    if (playerData == null) {
                        sender.sendMessage("No voting data found for " + target.getName());
                        return;
                    }

                    sender.sendMessage("Vote Check for " + target.getName() + ":");
                    sender.sendMessage("Total Votes: " + playerData.getTotalVotes());
                    sender.sendMessage("Current Streak: " + playerData.getCurrentStreak() + " days");
                    sender.sendMessage("Offline Votes: " + playerData.getOfflineVotes());

                    if (playerData.getLastVoteTime() != null) {
                        sender.sendMessage("Last Vote: " + playerData.getLastVoteTime());
                    }
                });

        return true;
    }
}
