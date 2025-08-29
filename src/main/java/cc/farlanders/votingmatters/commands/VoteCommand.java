package cc.farlanders.votingmatters.commands;

import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import cc.farlanders.votingmatters.VotingMatters;
import cc.farlanders.votingmatters.utils.MessageUtils;

public class VoteCommand implements CommandExecutor {

    private final VotingMatters plugin;

    public VoteCommand(VotingMatters plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            String message = MessageUtils.getMessage("errors.player-only");
            if (message != null && sender != null) {
                sender.sendMessage(message);
            }
            return true;
        }

        Player player = (Player) sender;

        // Handle subcommands
        if (args.length > 0) {
            return switch (args[0].toLowerCase()) {
                case "claim" -> {
                    handleClaimCommand(player);
                    yield true;
                }
                case "stats" -> {
                    handleStatsCommand(player);
                    yield true;
                }
                default -> false; // Invalid subcommand
            };
        }

        // Show voting information
        showVotingInfo(player);
        return true;
    }

    private void showVotingInfo(Player player) {
        player.sendMessage(MessageUtils.getMessage("vote.header"));

        Map<String, String> votingSites = plugin.getVoteManager().getVotingSites();
        if (votingSites.isEmpty()) {
            player.sendMessage(MessageUtils.getMessage("vote.no-sites"));
            return;
        }

        player.sendMessage(MessageUtils.getMessage("vote.sites-header"));
        for (Map.Entry<String, String> site : votingSites.entrySet()) {
            String message = MessageUtils.getMessage("vote.site-format")
                    .replace("%site%", site.getKey())
                    .replace("%url%", site.getValue());
            player.sendMessage(message);
        }

        // Show player stats
        plugin.getDatabaseManager().getPlayerData(player.getUniqueId())
                .thenAccept(playerData -> {
                    if (playerData != null) {
                        String stats = MessageUtils.getMessage("vote.stats")
                                .replace("%total_votes%", String.valueOf(playerData.getTotalVotes()))
                                .replace("%streak%", String.valueOf(playerData.getCurrentStreak()));
                        player.sendMessage(stats);
                    }
                });

        player.sendMessage(MessageUtils.getMessage("vote.footer"));
    }

    private void handleClaimCommand(Player player) {
        plugin.getDatabaseManager().getPlayerData(player.getUniqueId())
                .thenAccept(playerData -> {
                    if (playerData == null || playerData.getOfflineVotes() == 0) {
                        player.sendMessage(MessageUtils.getMessage("offline.no-offline"));
                        return;
                    }

                    // Process offline votes
                    plugin.getVoteManager().processOfflineVotes(player);

                    String message = MessageUtils.getMessage("offline.claimed")
                            .replace("%count%", String.valueOf(playerData.getOfflineVotes()));
                    player.sendMessage(message);
                });
    }

    private void handleStatsCommand(Player player) {
        plugin.getDatabaseManager().getPlayerData(player.getUniqueId())
                .thenAccept(playerData -> {
                    if (playerData == null) {
                        player.sendMessage(MessageUtils.getMessage("stats.no-data"));
                        return;
                    }

                    player.sendMessage(MessageUtils.getMessage("stats.header"));
                    player.sendMessage(MessageUtils.getMessage("stats.player-stats")
                            .replace("%player%", player.getName()));
                    player.sendMessage(MessageUtils.getMessage("stats.total-votes")
                            .replace("%total%", String.valueOf(playerData.getTotalVotes())));
                    player.sendMessage(MessageUtils.getMessage("stats.current-streak")
                            .replace("%streak%", String.valueOf(playerData.getCurrentStreak())));
                    player.sendMessage(MessageUtils.getMessage("stats.best-streak")
                            .replace("%best_streak%", String.valueOf(playerData.getBestStreak())));

                    if (playerData.getLastVoteTime() != null) {
                        player.sendMessage(MessageUtils.getMessage("stats.last-vote")
                                .replace("%last_vote%", playerData.getLastVoteTime().toString()));
                    }

                    long hoursUntilNext = playerData.getHoursUntilNextVote();
                    if (hoursUntilNext > 0) {
                        player.sendMessage(MessageUtils.getMessage("stats.next-vote")
                                .replace("%next_vote%", hoursUntilNext + " hours"));
                    } else {
                        player.sendMessage(MessageUtils.getMessage("stats.next-vote")
                                .replace("%next_vote%", "Now!"));
                    }
                });
    }
}
