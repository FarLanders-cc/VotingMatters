package cc.farlanders.votingmatters.api.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import cc.farlanders.votingmatters.VotingMatters;
import cc.farlanders.votingmatters.api.VoteAPI;
import cc.farlanders.votingmatters.models.VoteRecord;

/**
 * Base implementation for voting site APIs
 */
public abstract class BaseVoteAPI implements VoteAPI {

    protected final VotingMatters plugin;
    protected final String siteName;
    protected long lastCheck = -1;

    protected BaseVoteAPI(VotingMatters plugin, String siteName) {
        this.plugin = plugin;
        this.siteName = siteName;
    }

    @Override
    public String getSiteName() {
        return siteName;
    }

    @Override
    public long getLastCheck() {
        return lastCheck;
    }

    @Override
    public void setLastCheck(long timestamp) {
        this.lastCheck = timestamp;
    }

    @Override
    public boolean isEnabled() {
        return plugin.getConfigManager().getConfig().getBoolean("vote-sites." + siteName + ".enabled", false);
    }

    @Override
    public String getVotingURL() {
        return plugin.getConfigManager().getConfig().getString("vote-sites." + siteName + ".url", "");
    }

    protected String getApiKey() {
        return plugin.getConfigManager().getConfig().getString("vote-sites." + siteName + ".api-key", "");
    }

    protected String getServerId() {
        return plugin.getConfigManager().getConfig().getString("vote-sites." + siteName + ".server-id", "");
    }

    @Override
    public List<VoteRecord> checkForNewVotes() {
        if (!isEnabled()) {
            return new ArrayList<>();
        }

        try {
            setLastCheck(System.currentTimeMillis());
            return performVoteCheck();
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to check votes for {0}: {1}",
                    new Object[] { siteName, e.getMessage() });
            return new ArrayList<>();
        }
    }

    /**
     * Perform the actual vote check for this site
     * 
     * @return List of new vote records
     */
    protected abstract List<VoteRecord> performVoteCheck();

    @Override
    public boolean hasPlayerVoted(String username) {
        // Default implementation - override if the API supports direct checking
        return false;
    }
}
