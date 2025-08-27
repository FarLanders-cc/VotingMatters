package com.mjanglin.votingmatters.models;

import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Reward {

    public enum RewardType {
        MONEY,
        ITEM,
        COMMAND,
        POTION_EFFECT,
        EXPERIENCE
    }

    private final RewardType type;
    private final Object value;
    private final int chance;
    private final Map<String, Object> properties;

    public Reward(RewardType type, Object value, int chance, Map<String, Object> properties) {
        this.type = type;
        this.value = value;
        this.chance = chance;
        this.properties = properties;
    }

    public RewardType getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }

    public int getChance() {
        return chance;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public boolean shouldGive() {
        return Math.random() * 100 < chance;
    }

    // Helper methods for specific reward types
    public double getMoneyAmount() {
        if (type != RewardType.MONEY) {
            throw new IllegalStateException("Reward is not of type MONEY");
        }
        return ((Number) value).doubleValue();
    }

    @SuppressWarnings("unchecked")
    public List<String> getCommands() {
        if (type != RewardType.COMMAND) {
            throw new IllegalStateException("Reward is not of type COMMAND");
        }
        return (List<String>) value;
    }

    public int getExperienceAmount() {
        if (type != RewardType.EXPERIENCE) {
            throw new IllegalStateException("Reward is not of type EXPERIENCE");
        }
        return ((Number) value).intValue();
    }

    @SuppressWarnings("unchecked")
    public ItemStack getItemStack() {
        if (type != RewardType.ITEM) {
            throw new IllegalStateException("Reward is not of type ITEM");
        }

        ItemStack item = createBaseItem();
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            configureItemMeta(meta);
            item.setItemMeta(meta);
        }

        return item;
    }

    private ItemStack createBaseItem() {
        String materialName = (String) properties.get("material");
        Material material = Material.valueOf(materialName.toUpperCase());
        int amount = properties.containsKey("amount") ? ((Number) properties.get("amount")).intValue() : 1;
        return new ItemStack(material, amount);
    }

    @SuppressWarnings("unchecked")
    private void configureItemMeta(ItemMeta meta) {
        setDisplayName(meta);
        setLore(meta);
        addEnchantments(meta);
    }

    private void setDisplayName(ItemMeta meta) {
        if (properties.containsKey("name")) {
            meta.setDisplayName((String) properties.get("name"));
        }
    }

    @SuppressWarnings("unchecked")
    private void setLore(ItemMeta meta) {
        if (properties.containsKey("lore")) {
            meta.setLore((List<String>) properties.get("lore"));
        }
    }

    @SuppressWarnings("unchecked")
    private void addEnchantments(ItemMeta meta) {
        if (!properties.containsKey("enchantments")) {
            return;
        }

        List<String> enchantments = (List<String>) properties.get("enchantments");
        for (String enchantLine : enchantments) {
            addSingleEnchantment(meta, enchantLine);
        }
    }

    private void addSingleEnchantment(ItemMeta meta, String enchantLine) {
        String[] parts = enchantLine.split(":");
        if (parts.length != 2) {
            return;
        }

        try {
            Enchantment enchant = Enchantment.getByName(parts[0]);
            int level = Integer.parseInt(parts[1]);
            if (enchant != null) {
                meta.addEnchant(enchant, level, true);
            }
        } catch (NumberFormatException ignored) {
            // Invalid enchantment format
        }
    }

    public PotionEffect getPotionEffect() {
        if (type != RewardType.POTION_EFFECT) {
            throw new IllegalStateException("Reward is not of type POTION_EFFECT");
        }

        String effectName = (String) properties.get("effect");
        int duration = properties.containsKey("duration") ? ((Number) properties.get("duration")).intValue() : 600; // 30
                                                                                                                    // seconds
                                                                                                                    // default
        int amplifier = properties.containsKey("amplifier") ? ((Number) properties.get("amplifier")).intValue() : 0;

        PotionEffectType effectType = PotionEffectType.getByName(effectName);
        if (effectType == null) {
            throw new IllegalArgumentException("Invalid potion effect: " + effectName);
        }

        return new PotionEffect(effectType, duration, amplifier);
    }

    @Override
    public String toString() {
        return String.format("Reward{type=%s, value=%s, chance=%d%%}", type, value, chance);
    }
}
