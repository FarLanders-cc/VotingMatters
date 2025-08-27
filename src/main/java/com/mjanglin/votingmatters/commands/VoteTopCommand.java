package com.mjanglin.votingmatters.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.mjanglin.votingmatters.VotingMatters;
import com.mjanglin.votingmatters.models.PlayerVoteData;
import com.mjanglin.votingmatters.utils.MessageUtils;

public class VoteTopCommand implements CommandExecutor {

    private final VotingMatters plugin;

    public VoteTopCommand(VotingMatters plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        int tempPage = 1;
        if (args.length > 0) {
            try {
                tempPage = Integer.parseInt(args[0]);
                tempPage = Math.max(1, tempPage);
            } catch (NumberFormatException e) {
                tempPage = 1;
            }
        }
        final int page = tempPage; // Make final for lambda

        int limit = plugin.getConfigManager().getConfig().getInt("leaderboard.top-players", 10);

        plugin.getDatabaseManager().getTopVoters(limit)
                .thenAccept(topVoters -> showLeaderboard(sender, topVoters, page));

        return true;
    }

    private void showLeaderboard(CommandSender sender, List<PlayerVoteData> topVoters, int page) {
        if (topVoters.isEmpty()) {
            sender.sendMessage(MessageUtils.getMessage("leaderboard.no-data"));
            return;
        }

        sender.sendMessage(MessageUtils.getMessage("leaderboard.header"));

        int start = (page - 1) * 10;
        int end = Math.min(start + 10, topVoters.size());

        for (int i = start; i < end; i++) {
            PlayerVoteData voter = topVoters.get(i);
            String entry = MessageUtils.getMessage("leaderboard.entry")
                    .replace("%position%", String.valueOf(i + 1))
                    .replace("%player%", voter.getUsername())
                    .replace("%votes%", String.valueOf(voter.getTotalVotes()));
            sender.sendMessage(entry);
        }

        int maxPages = (int) Math.ceil((double) topVoters.size() / 10);
        String footer = MessageUtils.getMessage("leaderboard.footer")
                .replace("%page%", String.valueOf(page))
                .replace("%max_pages%", String.valueOf(maxPages))
                .replace("%next_page%", String.valueOf(Math.min(page + 1, maxPages)));
        sender.sendMessage(footer);
    }
}
