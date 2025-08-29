package cc.farlanders.votingmatters.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import cc.farlanders.votingmatters.VotingMatters;
import cc.farlanders.votingmatters.utils.MessageUtils;

public class VoteReloadCommand implements CommandExecutor {

    private final VotingMatters plugin;

    public VoteReloadCommand(VotingMatters plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("votingmatters.admin")) {
            sender.sendMessage(MessageUtils.getMessage("errors.no-permission"));
            return false;
        }

        try {
            plugin.reload();
            sender.sendMessage(MessageUtils.getMessage("admin.reload-success"));
            return true;
        } catch (Exception e) {
            String message = MessageUtils.getMessage("admin.reload-error")
                    .replace("%error%", e.getMessage());
            sender.sendMessage(message);
            return false;
        }
    }
}
