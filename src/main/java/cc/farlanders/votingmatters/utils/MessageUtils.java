package cc.farlanders.votingmatters.utils;

import org.bukkit.ChatColor;

import cc.farlanders.votingmatters.VotingMatters;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class MessageUtils {

    private static VotingMatters plugin;

    // Private constructor to prevent instantiation
    private MessageUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static void initialize(VotingMatters pluginInstance) {
        plugin = pluginInstance;
    }

    public static String getMessage(String key) {
        if (plugin == null) {
            return "Plugin not initialized";
        }

        String message = plugin.getConfigManager().getMessages().getString(key);
        if (message == null) {
            return "Message not found: " + key;
        }

        // Add prefix if not already present
        if (!message.startsWith(getPrefix()) && !key.equals("prefix")) {
            message = getPrefix() + message;
        }

        return translateColors(message);
    }

    public static String getPrefix() {
        if (plugin == null) {
            return "[VotingMatters] ";
        }

        String prefix = plugin.getConfigManager().getMessages().getString("prefix", "&8[&6VotingMatters&8] ");
        return translateColors(prefix);
    }

    public static String translateColors(String message) {
        if (message == null)
            return "";

        // Support for both legacy and modern color codes
        message = ChatColor.translateAlternateColorCodes('&', message);

        return message;
    }

    public static Component getComponent(String key) {
        String message = getMessage(key);
        return LegacyComponentSerializer.legacyAmpersand().deserialize(message);
    }

    public static String formatTime(long milliseconds) {
        if (milliseconds <= 0)
            return "now";

        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return days + " day" + (days != 1 ? "s" : "");
        } else if (hours > 0) {
            return hours + " hour" + (hours != 1 ? "s" : "");
        } else if (minutes > 0) {
            return minutes + " minute" + (minutes != 1 ? "s" : "");
        } else {
            return seconds + " second" + (seconds != 1 ? "s" : "");
        }
    }

    public static String formatNumber(long number) {
        if (number >= 1000000) {
            return String.format("%.1fM", number / 1000000.0);
        } else if (number >= 1000) {
            return String.format("%.1fK", number / 1000.0);
        } else {
            return String.valueOf(number);
        }
    }

    public static String replacePlaceholders(String message, String playerName, int votes, int streak) {
        return message
                .replace("%player%", playerName)
                .replace("%votes%", String.valueOf(votes))
                .replace("%total_votes%", String.valueOf(votes))
                .replace("%streak%", String.valueOf(streak))
                .replace("%current_streak%", String.valueOf(streak));
    }
}
