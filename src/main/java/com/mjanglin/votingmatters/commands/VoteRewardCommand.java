package com.mjanglin.votingmatters.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.mjanglin.votingmatters.utils.MessageUtils;

public class VoteRewardCommand implements CommandExecutor {

    public VoteRewardCommand() {
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("votingmatters.admin")) {
            sender.sendMessage(MessageUtils.getMessage("errors.no-permission"));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("Usage: /votereward <add|remove|list> [args...]");
            return true;
        }

        // TODO: Implement reward management commands
        sender.sendMessage("Reward management commands coming soon!");
        return true;
    }
}
