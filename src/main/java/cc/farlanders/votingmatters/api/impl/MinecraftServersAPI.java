package cc.farlanders.votingmatters.api.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.json.JSONException;

import cc.farlanders.votingmatters.VotingMatters;
import cc.farlanders.votingmatters.models.VoteRecord;

/**
 * API implementation for MinecraftServers.org
 */
public class MinecraftServersAPI extends BaseVoteAPI {

    public MinecraftServersAPI(VotingMatters plugin) {
        super(plugin, "minecraftservers");
    }

    @Override
    protected List<VoteRecord> performVoteCheck() {
        List<VoteRecord> voteRecords = new ArrayList<>();
        String apiKey = plugin.getConfigManager().getConfig().getString("minecraftservers.apiKey", "");
        String serverId = plugin.getConfigManager().getConfig().getString("minecraftservers.serverId", "");
        String apiUrl = "https://minecraftservers.org/api/?object=servers&element=voters&key=" + apiKey + "&id="
                + serverId;

        if (plugin.getConfigManager().getConfig().getBoolean("debug.enabled", false)) {
            plugin.getLogger().log(Level.INFO, "Checking votes for MinecraftServers.org using API: {0}", apiUrl);
        }

        try {
            java.net.URL url = new java.net.URL(apiUrl);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            int status = conn.getResponseCode();
            if (status == 200) {
                StringBuilder content = new StringBuilder();
                try (java.io.BufferedReader in = new java.io.BufferedReader(
                        new java.io.InputStreamReader(conn.getInputStream()))) {
                    String line;
                    while ((line = in.readLine()) != null) {
                        content.append(line);
                    }
                }

                // Parse JSON response
                org.json.JSONObject json = new org.json.JSONObject(content.toString());
                org.json.JSONArray voters = json.getJSONArray("voters");
                for (int i = 0; i < voters.length(); i++) {
                    org.json.JSONObject voter = voters.getJSONObject(i);
                    String username = voter.getString("username");
                    long timestamp = voter.getLong("timestamp");
                    // Adjust the parameters below to match your VoteRecord constructor
                    // Example assumes: VoteRecord(int id, UUID uuid, String username, Timestamp
                    // timestamp, String source, boolean isValid)
                    java.util.UUID uuid = java.util.UUID.nameUUIDFromBytes(username.getBytes());
                    java.sql.Timestamp ts = new java.sql.Timestamp(timestamp * 1000L);
                    voteRecords.add(new VoteRecord(0, uuid, username, ts, "minecraftservers", true));
                }
            } else {
                plugin.getLogger().log(Level.WARNING, "Failed to fetch votes from MinecraftServers.org: HTTP {0}",
                        status);
            }
            conn.disconnect();
        } catch (IOException | JSONException e) {
            plugin.getLogger().log(Level.WARNING, "Error fetching votes from MinecraftServers.org: {0}",
                    e.getMessage());
        }

        return voteRecords;
    }

    @Override
    public boolean hasPlayerVoted(String username) {
        String apiKey = plugin.getConfigManager().getConfig().getString("minecraftservers.apiKey", "");
        String serverId = plugin.getConfigManager().getConfig().getString("minecraftservers.serverId", "");
        String apiUrl = "https://minecraftservers.org/api/?object=servers&element=voters&key=" + apiKey + "&id="
                + serverId;

        try {
            java.net.URL url = new java.net.URL(apiUrl);
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            int status = conn.getResponseCode();
            if (status == 200) {
                StringBuilder content;
                try (java.io.BufferedReader in = new java.io.BufferedReader(
                        new java.io.InputStreamReader(conn.getInputStream()))) {
                    content = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        content.append(line);
                    }
                }

                org.json.JSONObject json = new org.json.JSONObject(content.toString());
                org.json.JSONArray voters = json.getJSONArray("voters");
                for (int i = 0; i < voters.length(); i++) {
                    org.json.JSONObject voter = voters.getJSONObject(i);
                    String voterUsername = voter.getString("username");
                    if (voterUsername.equalsIgnoreCase(username)) {
                        conn.disconnect();
                        return true;
                    }
                }
            }
            conn.disconnect();
        } catch (IOException | org.json.JSONException e) {
            plugin.getLogger().log(Level.WARNING, "Error checking if player voted on MinecraftServers.org: {0}",
                    e.getMessage());
        }
        return false;
    }
}
