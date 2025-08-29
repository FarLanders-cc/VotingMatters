package cc.farlanders.votingmatters.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import cc.farlanders.votingmatters.utils.MessageUtils;

public class VoteRewardCommand implements CommandExecutor {

    // Simple in-memory list to hold rewards; replace with persistent store as
    // needed.
    private static final List<String> rewards = new ArrayList<>();

    public VoteRewardCommand() {
        /*
         * Intentionally empty: no initialization required at construction time because
         * rewards are stored in a static in-memory list and there are no dependencies
         * to inject; this constructor is kept to preserve a public no-arg API and to
         * allow future initialization without changing callers.
         */
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("votingmatters.admin")) {
            sender.sendMessage(MessageUtils.getMessage("errors.no-permission"));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("Usage: /votereward <add|remove|list> [args...]");
            return false;
        }

        String subcommand = args[0].toLowerCase();

        switch (subcommand) {
            case "add" -> {
                return handleAdd(sender, args);
            }
            case "remove" -> {
                return handleRemove(sender, args);
            }
            case "list" -> {
                return handleList(sender);
            }
            default -> {
                sender.sendMessage("Unknown subcommand. Usage: /votereward <add|remove|list> [args...]");
                return false;
            }
        }
    }

    private boolean handleAdd(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("Usage: /votereward add <reward>");
            return false;
        }
        // Add reward to the in-memory list
        String rewardToAdd = args[1];
        if (rewards.contains(rewardToAdd)) {
            sender.sendMessage("Reward already exists: " + rewardToAdd);
        } else {
            rewards.add(rewardToAdd);
            sender.sendMessage("Added reward: " + rewardToAdd);
        }
        return true;
    }

    private boolean handleRemove(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("Usage: /votereward remove <reward>");
            return false;
        }
        // Remove reward from the in-memory list
        String rewardToRemove = args[1];
        if (rewards.remove(rewardToRemove)) {
            sender.sendMessage("Removed reward: " + rewardToRemove);
        } else {
            sender.sendMessage("Reward not found: " + rewardToRemove);
        }
        return true;
    }

    private boolean handleList(CommandSender sender) {
        // List rewards from the in-memory list
        if (rewards.isEmpty()) {
            sender.sendMessage("No rewards configured.");
        } else {
            sender.sendMessage("Rewards: " + rewards.toString());
        }
        return true;
    }
}
