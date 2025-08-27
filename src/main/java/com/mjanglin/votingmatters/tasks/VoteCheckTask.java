package com.mjanglin.votingmatters.tasks;

import org.bukkit.scheduler.BukkitRunnable;

import com.mjanglin.votingmatters.VotingMatters;

public class VoteCheckTask extends BukkitRunnable {

    private final VotingMatters plugin;

    public VoteCheckTask(VotingMatters plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (!plugin.getConfigManager().getConfig().getBoolean("vote-check.enabled", true)) {
            return;
        }

        try {
            plugin.getVoteManager().checkForVotes().join();
        } catch (Exception e) {
            plugin.getLogger().warning(String.format("Error during vote check: %s", e.getMessage()));
        }
    }
}
