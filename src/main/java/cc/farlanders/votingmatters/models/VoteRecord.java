package cc.farlanders.votingmatters.models;

import java.sql.Timestamp;
import java.util.UUID;

public class VoteRecord {

    private final int id;
    private final UUID playerUuid;
    private final String siteName;
    private final Timestamp voteTime;
    private final String ipAddress;
    private boolean rewarded;

    public VoteRecord(int id, UUID playerUuid, String siteName, Timestamp voteTime,
            String ipAddress, boolean rewarded) {
        this.id = id;
        this.playerUuid = playerUuid;
        this.siteName = siteName;
        this.voteTime = voteTime;
        this.ipAddress = ipAddress;
        this.rewarded = rewarded;
    }

    // Create new vote record (for insertion)
    public VoteRecord(UUID playerUuid, String siteName, String ipAddress) {
        this(0, playerUuid, siteName, new Timestamp(System.currentTimeMillis()), ipAddress, false);
    }

    // Getters
    public int getId() {
        return id;
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public String getSiteName() {
        return siteName;
    }

    public Timestamp getVoteTime() {
        return voteTime;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public boolean isRewarded() {
        return rewarded;
    }

    // Setters
    public void setRewarded(boolean rewarded) {
        this.rewarded = rewarded;
    }

    @Override
    public String toString() {
        return String.format("VoteRecord{id=%d, player=%s, site='%s', time=%s, rewarded=%b}",
                id, playerUuid, siteName, voteTime, rewarded);
    }
}
