package cc.farlanders.votingmatters.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

import cc.farlanders.votingmatters.VotingMatters;
import cc.farlanders.votingmatters.models.PlayerVoteData;
import cc.farlanders.votingmatters.models.VoteRecord;

public class DatabaseManager {

    private final VotingMatters plugin;
    private Connection connection;
    private final String dbType;

    public DatabaseManager(VotingMatters plugin) {
        this.plugin = plugin;
        this.dbType = plugin.getConfigManager().getConfig().getString("database.type", "sqlite");
        initialize();
    }

    private void initialize() {
        try {
            if (dbType.equalsIgnoreCase("sqlite")) {
                setupSQLite();
            } else if (dbType.equalsIgnoreCase("mysql")) {
                setupMySQL();
            }
            createTables();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize database: {0}", e.getMessage());
        }
    }

    private void setupSQLite() throws SQLException {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        String url = "jdbc:sqlite:" + dataFolder.getAbsolutePath() + File.separator + "votes.db";
        connection = DriverManager.getConnection(url);
    }

    private void setupMySQL() throws SQLException {
        String host = plugin.getConfigManager().getConfig().getString("database.host");
        int port = plugin.getConfigManager().getConfig().getInt("database.port");
        String database = plugin.getConfigManager().getConfig().getString("database.database");
        String username = plugin.getConfigManager().getConfig().getString("database.username");
        String password = plugin.getConfigManager().getConfig().getString("database.password");

        String url = String.format("jdbc:mysql://%s:%d/%s?useSSL=false&serverTimezone=UTC",
                host, port, database);
        connection = DriverManager.getConnection(url, username, password);
    }

    private void createTables() throws SQLException {
        // Player vote data table
        String createPlayerData = """
                    CREATE TABLE IF NOT EXISTS player_vote_data (
                        uuid VARCHAR(36) PRIMARY KEY,
                        username VARCHAR(16) NOT NULL,
                        total_votes INTEGER DEFAULT 0,
                        current_streak INTEGER DEFAULT 0,
                        best_streak INTEGER DEFAULT 0,
                        last_vote_time TIMESTAMP,
                        offline_votes INTEGER DEFAULT 0,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    )
                """;

        // Vote records table
        String createVoteRecords = """
                    CREATE TABLE IF NOT EXISTS vote_records (
                        id INTEGER PRIMARY KEY %s,
                        player_uuid VARCHAR(36) NOT NULL,
                        site_name VARCHAR(50) NOT NULL,
                        vote_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        ip_address VARCHAR(45),
                        rewarded BOOLEAN DEFAULT FALSE,
                        FOREIGN KEY (player_uuid) REFERENCES player_vote_data(uuid)
                    )
                """.formatted(dbType.equalsIgnoreCase("sqlite") ? "AUTOINCREMENT" : "AUTO_INCREMENT");

        // Vote site status table
        String createSiteStatus = """
                    CREATE TABLE IF NOT EXISTS vote_site_status (
                        site_name VARCHAR(50) PRIMARY KEY,
                        last_check TIMESTAMP,
                        status VARCHAR(20) DEFAULT 'UNKNOWN',
                        error_message TEXT
                    )
                """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createPlayerData);
            stmt.execute(createVoteRecords);
            stmt.execute(createSiteStatus);
        }
    }

    public CompletableFuture<PlayerVoteData> getPlayerData(UUID playerUuid) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT uuid, username, total_votes, current_streak, best_streak, last_vote_time, offline_votes FROM player_vote_data WHERE uuid = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, playerUuid.toString());
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    return new PlayerVoteData(
                            UUID.fromString(rs.getString("uuid")),
                            rs.getString("username"),
                            rs.getInt("total_votes"),
                            rs.getInt("current_streak"),
                            rs.getInt("best_streak"),
                            rs.getTimestamp("last_vote_time"),
                            rs.getInt("offline_votes"));
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to get player data: {0}", e.getMessage());
            }
            return null;
        });
    }

    public CompletableFuture<Void> savePlayerData(PlayerVoteData data) {
        String sql = """
                INSERT INTO player_vote_data (uuid, username, total_votes, current_streak, best_streak, last_vote_time, offline_votes, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                ON %s
                SET username = VALUES(username),
                    total_votes = VALUES(total_votes),
                    current_streak = VALUES(current_streak),
                    best_streak = VALUES(best_streak),
                    last_vote_time = VALUES(last_vote_time),
                    offline_votes = VALUES(offline_votes),
                    updated_at = CURRENT_TIMESTAMP
                """
                .formatted(dbType.equalsIgnoreCase("sqlite") ? "CONFLICT(uuid) DO UPDATE" : "DUPLICATE KEY UPDATE");

        return CompletableFuture.runAsync(() -> {
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, data.getUuid().toString());
                stmt.setString(2, data.getUsername());
                stmt.setInt(3, data.getTotalVotes());
                stmt.setInt(4, data.getCurrentStreak());
                stmt.setInt(5, data.getBestStreak());
                stmt.setTimestamp(6, data.getLastVoteTime());
                stmt.setInt(7, data.getOfflineVotes());
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to save player data: {0}", e.getMessage());
            }
        });
    }

    public CompletableFuture<Void> addVoteRecord(VoteRecord record) {
        return CompletableFuture.runAsync(() -> {
            String sql = "INSERT INTO vote_records (player_uuid, site_name, vote_time, ip_address, rewarded) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, record.getPlayerUuid().toString());
                stmt.setString(2, record.getSiteName());
                stmt.setTimestamp(3, record.getVoteTime());
                stmt.setString(4, record.getIpAddress());
                stmt.setBoolean(5, record.isRewarded());
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to add vote record: {0}", e.getMessage());
            }
        });
    }

    public CompletableFuture<List<PlayerVoteData>> getTopVoters(int limit) {
        return CompletableFuture.supplyAsync(() -> {
            List<PlayerVoteData> topVoters = new ArrayList<>();
            String sql = "SELECT uuid, username, total_votes, current_streak, best_streak, last_vote_time, offline_votes FROM player_vote_data ORDER BY total_votes DESC LIMIT ?";

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, limit);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    topVoters.add(new PlayerVoteData(
                            UUID.fromString(rs.getString("uuid")),
                            rs.getString("username"),
                            rs.getInt("total_votes"),
                            rs.getInt("current_streak"),
                            rs.getInt("best_streak"),
                            rs.getTimestamp("last_vote_time"),
                            rs.getInt("offline_votes")));
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to get top voters: {0}", e.getMessage());
            }
            return topVoters;
        });
    }

    public CompletableFuture<Boolean> hasVotedOnSite(UUID playerUuid, String siteName, long cooldownHours) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT vote_time FROM vote_records WHERE player_uuid = ? AND site_name = ? ORDER BY vote_time DESC LIMIT 1";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, playerUuid.toString());
                stmt.setString(2, siteName);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    Timestamp lastVote = rs.getTimestamp("vote_time");
                    long cooldownMillis = cooldownHours * 60 * 60 * 1000;
                    return (System.currentTimeMillis() - lastVote.getTime()) < cooldownMillis;
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to check vote cooldown: {0}", e.getMessage());
            }
            return false;
        });
    }

    public CompletableFuture<List<VoteRecord>> getUnrewardedVotes(UUID playerUuid) {
        return CompletableFuture.supplyAsync(() -> {
            List<VoteRecord> unrewardedVotes = new ArrayList<>();
            String sql = "SELECT id, player_uuid, site_name, vote_time, ip_address, rewarded FROM vote_records WHERE player_uuid = ? AND rewarded = FALSE ORDER BY vote_time ASC";

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, playerUuid.toString());
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    unrewardedVotes.add(new VoteRecord(
                            rs.getInt("id"),
                            UUID.fromString(rs.getString("player_uuid")),
                            rs.getString("site_name"),
                            rs.getTimestamp("vote_time"),
                            rs.getString("ip_address"),
                            rs.getBoolean("rewarded")));
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to get unrewarded votes: {0}", e.getMessage());
            }
            return unrewardedVotes;
        });
    }

    public CompletableFuture<Void> markVoteRewarded(int voteId) {
        return CompletableFuture.runAsync(() -> {
            String sql = "UPDATE vote_records SET rewarded = TRUE WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, voteId);
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to mark vote as rewarded: {0}", e.getMessage());
            }
        });
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to close database connection: {0}", e.getMessage());
        }
    }
}
