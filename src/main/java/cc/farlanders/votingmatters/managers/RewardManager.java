package cc.farlanders.votingmatters.managers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import cc.farlanders.votingmatters.VotingMatters;
import cc.farlanders.votingmatters.models.PlayerVoteData;
import cc.farlanders.votingmatters.models.Reward;
import cc.farlanders.votingmatters.utils.MessageUtils;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.milkbowl.vault.economy.Economy;

public class RewardManager {

    private static final String ENCHANTMENTS_KEY = "enchantments";

    private final VotingMatters plugin;
    private List<Reward> defaultRewards;
    private Map<Integer, List<Reward>> streakRewards;
    private Map<Integer, List<Reward>> milestoneRewards;
    private Map<String, List<Reward>> siteRewards;
    private List<Reward> vipRewards;
    private List<Reward> weekendRewards;

    public RewardManager(VotingMatters plugin) {
        this.plugin = plugin;
        loadRewards();
    }

    public final void loadRewards() {
        ConfigurationSection rewardsConfig = plugin.getConfigManager().getRewards();

        // Load default rewards
        defaultRewards = loadRewardList(rewardsConfig.getConfigurationSection("default-rewards"));

        // Load streak rewards
        streakRewards = new HashMap<>();
        ConfigurationSection streakSection = rewardsConfig.getConfigurationSection("streak-rewards");
        if (streakSection != null) {
            for (String key : streakSection.getKeys(false)) {
                int streak = Integer.parseInt(key);
                List<Reward> rewards = loadRewardList(streakSection.getConfigurationSection(key));
                streakRewards.put(streak, rewards);
            }
        }

        // Load milestone rewards
        milestoneRewards = new HashMap<>();
        ConfigurationSection milestoneSection = rewardsConfig.getConfigurationSection("milestone-rewards");
        if (milestoneSection != null) {
            for (String key : milestoneSection.getKeys(false)) {
                int milestone = Integer.parseInt(key);
                List<Reward> rewards = loadRewardList(milestoneSection.getConfigurationSection(key));
                milestoneRewards.put(milestone, rewards);
            }
        }

        // Load site-specific rewards
        siteRewards = new HashMap<>();
        ConfigurationSection siteSection = rewardsConfig.getConfigurationSection("site-rewards");
        if (siteSection != null) {
            for (String siteName : siteSection.getKeys(false)) {
                List<Reward> rewards = loadRewardList(siteSection.getConfigurationSection(siteName));
                siteRewards.put(siteName, rewards);
            }
        }

        // Load VIP rewards
        vipRewards = loadRewardList(rewardsConfig.getConfigurationSection("vip-rewards"));

        // Load weekend rewards
        weekendRewards = loadRewardList(rewardsConfig.getConfigurationSection("weekend-bonus.extra-rewards"));
    }

    private List<Reward> loadRewardList(ConfigurationSection section) {
        List<Reward> rewards = new ArrayList<>();
        if (section == null)
            return rewards;

        for (String key : section.getKeys(false)) {
            ConfigurationSection rewardSection = section.getConfigurationSection(key);
            String type = rewardSection != null ? rewardSection.getString("type") : null;

            if (rewardSection != null && type != null) {
                int chance = rewardSection.getInt("chance", 100);
                RewardData rewardData = createRewardData(type, rewardSection);

                if (rewardData != null) {
                    rewards.add(new Reward(rewardData.type, rewardData.value, chance, rewardData.properties));
                }
            } else if (rewardSection != null && plugin.getLogger().isLoggable(java.util.logging.Level.WARNING)) {
                plugin.getLogger().warning(String.format("Reward type is null for key: %s", key));
            }
        }

        return rewards;
    }

    private RewardData createRewardData(String type, ConfigurationSection rewardSection) {
        switch (type.toLowerCase()) {
            case "money" -> {
                return new RewardData(Reward.RewardType.MONEY, rewardSection.getDouble("amount"), new HashMap<>());
            }
            case "item" -> {
                return createItemRewardData(rewardSection);
            }
            case "command" -> {
                return new RewardData(Reward.RewardType.COMMAND, rewardSection.getStringList("commands"),
                        new HashMap<>());
            }
            case "experience" -> {
                return new RewardData(Reward.RewardType.EXPERIENCE, rewardSection.getInt("amount"), new HashMap<>());
            }
            case "potion", "potion_effect" -> {
                return createPotionRewardData(rewardSection);
            }
            default -> {
                if (plugin.getLogger().isLoggable(java.util.logging.Level.WARNING)) {
                    plugin.getLogger().warning(String.format("Unknown reward type: %s", type));
                }
                return null;
            }
        }
    }

    private RewardData createItemRewardData(ConfigurationSection rewardSection) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("material", rewardSection.getString("material"));
        properties.put("amount", rewardSection.getInt("amount", 1));

        if (rewardSection.contains("name")) {
            String name = rewardSection.getString("name");
            if (name != null) {
                properties.put("name",
                        LegacyComponentSerializer.legacyAmpersand().serialize(
                                LegacyComponentSerializer.legacyAmpersand().deserialize(name)));
            }
        }

        if (rewardSection.contains("lore")) {
            List<String> lore = rewardSection.getStringList("lore");
            lore.replaceAll(line -> LegacyComponentSerializer.legacyAmpersand().serialize(
                    LegacyComponentSerializer.legacyAmpersand().deserialize(line)));
            properties.put("lore", lore);
            if (rewardSection.contains(ENCHANTMENTS_KEY)) {
                properties.put(ENCHANTMENTS_KEY, rewardSection.getStringList(ENCHANTMENTS_KEY));
            }
            properties.put(ENCHANTMENTS_KEY, rewardSection.getStringList(ENCHANTMENTS_KEY));
        }

        return new RewardData(Reward.RewardType.ITEM, null, properties);
    }

    private RewardData createPotionRewardData(ConfigurationSection rewardSection) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("effect", rewardSection.getString("potion-type", rewardSection.getString("effect")));
        properties.put("duration", rewardSection.getInt("duration", 600));
        properties.put("amplifier", rewardSection.getInt("amplifier", 0));

        return new RewardData(Reward.RewardType.POTION_EFFECT, null, properties);
    }

    private static class RewardData {
        final Reward.RewardType type;
        final Object value;
        final Map<String, Object> properties;

        RewardData(Reward.RewardType type, Object value, Map<String, Object> properties) {
            this.type = type;
            this.value = value;
            this.properties = properties;
        }
    }

    public void giveVoteRewards(Player player, String siteName, PlayerVoteData playerData) {
        List<String> rewardMessages = new ArrayList<>();

        // Give default rewards
        giveRewardList(player, defaultRewards, rewardMessages);

        // Give site-specific rewards
        if (siteRewards.containsKey(siteName)) {
            giveRewardList(player, siteRewards.get(siteName), rewardMessages);
        }

        // Check for streak rewards
        int currentStreak = playerData.getCurrentStreak();
        if (streakRewards.containsKey(currentStreak)) {
            giveRewardList(player, streakRewards.get(currentStreak), rewardMessages);
            String streakMessage = MessageUtils.getMessage("rewards.streak-bonus")
                    .replace("%days%", String.valueOf(currentStreak));
            rewardMessages.add(streakMessage);
        }

        // Check for milestone rewards
        int totalVotes = playerData.getTotalVotes();
        if (milestoneRewards.containsKey(totalVotes)) {
            giveRewardList(player, milestoneRewards.get(totalVotes), rewardMessages);
            String milestoneMessage = MessageUtils.getMessage("rewards.milestone-bonus")
                    .replace("%votes%", String.valueOf(totalVotes));
            rewardMessages.add(milestoneMessage);
        }

        // Give VIP rewards if player has permission
        if (player.hasPermission("votingmatters.vip") && vipRewards != null) {
            giveRewardList(player, vipRewards, rewardMessages);
            rewardMessages.add(MessageUtils.getMessage("rewards.vip-bonus"));
        }

        // Weekend bonus
        if (isWeekend() && weekendRewards != null) {
            giveRewardList(player, weekendRewards, rewardMessages);
            rewardMessages.add(MessageUtils.getMessage("rewards.weekend-bonus"));

            // Apply weekend multiplier to money rewards
            applyWeekendMultiplier(player);
        }

        // Send reward messages
        if (!rewardMessages.isEmpty()) {
            player.sendMessage(MessageUtils.getMessage("rewards.received"));
            for (String message : rewardMessages) {
                player.sendMessage(message);
            }
        }

        // Broadcast vote if enabled
        if (plugin.getConfigManager().getConfig().getBoolean("notifications.broadcast-votes", true)) {
            String broadcastMessage = MessageUtils.getMessage("detection.broadcast")
                    .replace("%player%", player.getName());
            Bukkit.getServer().broadcast(net.kyori.adventure.text.Component.text(broadcastMessage));
        }
    }

    private void giveRewardList(Player player, List<Reward> rewards, List<String> messages) {
        if (rewards == null)
            return;

        for (Reward reward : rewards) {
            if (!reward.shouldGive())
                continue;

            switch (reward.getType()) {
                case MONEY -> giveMoney(player, reward.getMoneyAmount(), messages);
                case ITEM -> giveItem(player, reward.getItemStack(), messages);
                case COMMAND -> executeCommands(player, reward.getCommands(), messages);
                case EXPERIENCE -> giveExperience(player, reward.getExperienceAmount(), messages);
                case POTION_EFFECT -> givePotionEffect(player, reward.getPotionEffect(), messages);
            }
        }
    }

    private void giveMoney(Player player, double amount, List<String> messages) {
        Economy economy = plugin.getEconomy();
        if (economy != null) {
            economy.depositPlayer(player, amount);
            String message = MessageUtils.getMessage("rewards.money")
                    .replace("%amount%", String.format("%.2f", amount));
            messages.add(message);
        }
    }

    private void giveItem(Player player, ItemStack item, List<String> messages) {
        Map<Integer, ItemStack> leftover = player.getInventory().addItem(item);
        if (!leftover.isEmpty()) {
            // Drop items that don't fit
            for (ItemStack drop : leftover.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), drop);
            }
        }

        String message = MessageUtils.getMessage("rewards.item")
                .replace("%amount%", String.valueOf(item.getAmount()))
                .replace("%item%", item.getType().name().toLowerCase().replace("_", " "));
        messages.add(message);
    }

    private void executeCommands(Player player, List<String> commands, List<String> messages) {
        for (String command : commands) {
            String processedCommand = command.replace("%player%", player.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCommand);
        }
        messages.add(MessageUtils.getMessage("rewards.command"));
    }

    private void giveExperience(Player player, int amount, List<String> messages) {
        player.giveExp(amount);
        String message = MessageUtils.getMessage("rewards.experience")
                .replace("%amount%", String.valueOf(amount));
        messages.add(message);
    }

    private void givePotionEffect(Player player, PotionEffect effect, List<String> messages) {
        player.addPotionEffect(effect);
        String message = MessageUtils.getMessage("rewards.potion")
                .replace("%effect%", effect.getType().getKey().getKey().toLowerCase().replace("_", " "));
        messages.add(message);
    }

    private boolean isWeekend() {
        LocalDateTime now = LocalDateTime.now();
        int dayOfWeek = now.getDayOfWeek().getValue();
        return dayOfWeek >= 5; // Friday (5), Saturday (6), Sunday (7)
    }

    private void applyWeekendMultiplier(Player player) {
        double multiplier = plugin.getConfigManager().getRewards().getDouble("weekend-bonus.multiplier", 1.5);
        if (multiplier > 1.0) {
            Economy economy = plugin.getEconomy();
            if (economy != null) {
                // Calculate bonus amount based on default money rewards
                double bonusAmount = 0;
                for (Reward reward : defaultRewards) {
                    if (reward.getType() == Reward.RewardType.MONEY && reward.shouldGive()) {
                        bonusAmount += reward.getMoneyAmount() * (multiplier - 1.0);
                    }
                }

                if (bonusAmount > 0) {
                    economy.depositPlayer(player, bonusAmount);
                }
            }
        }
    }

    public void reload() {
        loadRewards();
    }
}
