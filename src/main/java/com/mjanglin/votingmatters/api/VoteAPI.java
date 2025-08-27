package com.mjanglin.votingmatters.api;

import java.util.List;

import com.mjanglin.votingmatters.models.VoteRecord;

/**
 * Interface for voting site API implementations
 */
public interface VoteAPI {

    /**
     * Get the name of this voting site
     * 
     * @return The site name
     */
    String getSiteName();

    /**
     * Check for new votes from this site
     * 
     * @return List of new vote records
     */
    List<VoteRecord> checkForNewVotes();

    /**
     * Check if a specific player has voted on this site recently
     * 
     * @param username The player's username
     * @return true if the player has voted recently
     */
    boolean hasPlayerVoted(String username);

    /**
     * Get the voting URL for this site
     * 
     * @return The voting URL
     */
    String getVotingURL();

    /**
     * Check if this API is enabled and properly configured
     * 
     * @return true if the API is enabled
     */
    boolean isEnabled();

    /**
     * Get the last time this API was checked
     * 
     * @return Timestamp of last check, or -1 if never checked
     */
    long getLastCheck();

    /**
     * Set the last check time
     * 
     * @param timestamp The timestamp to set
     */
    void setLastCheck(long timestamp);
}
