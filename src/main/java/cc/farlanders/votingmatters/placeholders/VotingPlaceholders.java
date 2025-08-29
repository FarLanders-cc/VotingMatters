package cc.farlanders.votingmatters.placeholders;

import org.bukkit.entity.Player;

import cc.farlanders.votingmatters.VotingMatters;
import cc.farlanders.votingmatters.models.PlayerVoteData;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class VotingPlaceholders extends PlaceholderExpansion {

    private final VotingMatters plugin;

    public VotingPlaceholders(VotingMatters plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "votingmatters";
    }

    @Override
    public String getAuthor() {
        return "clxrity";
    }

    @Override
    public String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, String params) {
        if (player == null)
            return "";

        PlayerVoteData data = plugin.getDatabaseManager().getPlayerData(player.getUniqueId()).join();
        if (data == null)
            return "0";

        return switch (params.toLowerCase()) {
            case "total_votes", "votes" -> String.valueOf(data.getTotalVotes());
            case "current_streak", "streak" -> String.valueOf(data.getCurrentStreak());
            case "best_streak" -> String.valueOf(data.getBestStreak());
            case "offline_votes" -> String.valueOf(data.getOfflineVotes());
            case "last_vote" -> data.getLastVoteTime() != null ? data.getLastVoteTime().toString() : "Never";
            case "hours_until_next" -> String.valueOf(data.getHoursUntilNextVote());
            case "can_vote" -> data.hasVotedToday() ? "false" : "true";
            case "streak_at_risk" -> data.isStreakAtRisk() ? "true" : "false";
            default -> null;
        };
    }
}
