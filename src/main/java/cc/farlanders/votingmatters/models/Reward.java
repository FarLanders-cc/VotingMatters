package cc.farlanders.votingmatters.models;

import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
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

    private void configureItemMeta(ItemMeta meta) {
        setDisplayName(meta);
        setLore(meta);
        addEnchantments(meta);
    }

    private void setDisplayName(ItemMeta meta) {
        if (properties.containsKey("name")) {
            Object nameObj = properties.get("name");
            if (nameObj instanceof String string) {
                safeSetDisplayName(meta, string);
            } else if (nameObj != null) {
                safeSetDisplayName(meta, String.valueOf(nameObj));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void setLore(ItemMeta meta) {
        if (properties.containsKey("lore")) {
            List<String> lore = (List<String>) properties.get("lore");
            safeSetLore(meta, lore);
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
            Enchantment enchant = lookupEnchantment(parts[0]);
            int level = Integer.parseInt(parts[1]);
            if (enchant != null) {
                meta.addEnchant(enchant, level, true);
            }
        } catch (NumberFormatException ignored) {
            // Invalid enchantment format
        }
    }

    // --- Compatibility helpers ---

    /**
     * Try to look up an enchantment by NamespacedKey when available, fall back to
     * getByName.
     */
    private Enchantment lookupEnchantment(String name) {
        if (name == null)
            return null;
        String key = name.toLowerCase();
        try {
            // Try Enchantment.getByKey(NamespacedKey.minecraft(key)) when available
            Enchantment byKey = lookupEnchantmentByKey(key);
            if (byKey != null) {
                return byKey;
            }

            // Fallback to legacy name lookup (use reflection to avoid direct deprecated
            // call)
            try {
                java.lang.reflect.Method getByName = Enchantment.class.getMethod("getByName", String.class);
                Object res = getByName.invoke(null, name.toUpperCase());
                if (res instanceof Enchantment enchantment)
                    return enchantment;
            } catch (ReflectiveOperationException ignored) {
                // ignore
            }
            return null;
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    /**
     * Attempt to lookup an Enchantment via NamespacedKey.minecraft(key) and
     * Enchantment.getByKey,
     * catching the same exceptions as the original nested try block and letting
     * reflective
     * invocation exceptions bubble up to the caller.
     */
    private Enchantment lookupEnchantmentByKey(String key) throws ReflectiveOperationException {
        try {
            java.lang.reflect.Method namespacedMinecraft = NamespacedKey.class.getMethod("minecraft", String.class);
            Object nsKey = namespacedMinecraft.invoke(null, key);
            java.lang.reflect.Method getByKey = Enchantment.class.getMethod("getByKey", NamespacedKey.class);
            Object result = getByKey.invoke(null, nsKey);
            if (result instanceof Enchantment enchantment)
                return enchantment;
        } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException e) {
            // ignore and fallback
        }
        return null;
    }

    /**
     * Safely set display name using Adventure Component API when present, otherwise
     * fall back to legacy String API.
     */
    private void safeSetDisplayName(ItemMeta meta, String name) {
        if (name == null)
            return;
        try {
            Class<?> componentClass = Class.forName("net.kyori.adventure.text.Component");
            java.lang.reflect.Method textMethod = componentClass.getMethod("text", CharSequence.class);
            Object component = textMethod.invoke(null, name);

            if (trySetComponentDisplayName(meta, componentClass, component)) {
                return;
            }
        } catch (ReflectiveOperationException ignored) {
            // Adventure not available or reflective call failed, fall back below
        }

        // Legacy fallback via reflection to avoid direct deprecated reference
        try {
            java.lang.reflect.Method setDisplayNameLegacy = meta.getClass().getMethod("setDisplayName", String.class);
            setDisplayNameLegacy.invoke(meta, name);
        } catch (ReflectiveOperationException ignored) {
            // nothing we can do
        }
    }

    /**
     * Attempt to set the display name using a Component parameter, trying known
     * method names; return true if successful.
     */
    private boolean trySetComponentDisplayName(ItemMeta meta, Class<?> componentClass, Object component) {
        try {
            java.lang.reflect.Method setDisplayName = meta.getClass().getMethod("setDisplayName", componentClass);
            setDisplayName.invoke(meta, component);
            return true;
        } catch (NoSuchMethodException ignored) {
            // try alternate method name: displayName
            try {
                java.lang.reflect.Method displayName = meta.getClass().getMethod("displayName", componentClass);
                displayName.invoke(meta, component);
                return true;
            } catch (ReflectiveOperationException ignored2) {
                // fallback to legacy
            }
        } catch (ReflectiveOperationException ignored) {
            // invocation failed
        }
        return false;
    }

    /**
     * Safely set lore using Adventure Component API when present, otherwise fall
     * back to legacy String API.
     */
    private void safeSetLore(ItemMeta meta, List<String> lore) {
        if (lore == null)
            return;
        try {
            Class<?> componentClass = Class.forName("net.kyori.adventure.text.Component");
            java.lang.reflect.Method textMethod = componentClass.getMethod("text", CharSequence.class);
            java.util.List<Object> components = new java.util.ArrayList<>();
            for (String line : lore) {
                components.add(textMethod.invoke(null, line));
            }

            try {
                java.lang.reflect.Method setLore = meta.getClass().getMethod("setLore", java.util.List.class);
                // check parameter type: if it expects Component, pass component list
                java.lang.reflect.Type[] params = setLore.getGenericParameterTypes();
                if (params.length == 1) {
                    setLore.invoke(meta, components);
                    return;
                }
            } catch (NoSuchMethodException ignored) {
                // fallback
            }
        } catch (ReflectiveOperationException ignored) {
            // Adventure not available, fall back
        }

        // Legacy fallback via reflection to avoid direct deprecated reference
        try {
            java.lang.reflect.Method setLoreLegacy = meta.getClass().getMethod("setLore", java.util.List.class);
            setLoreLegacy.invoke(meta, lore);
        } catch (ReflectiveOperationException ignored) {
            // nothing we can do
        }
    }

    /**
     * Lookup a PotionEffectType by name, attempting
     * getByKey(NamespacedKey.minecraft(name)) first,
     * then falling back to reflective getByName.
     */
    private PotionEffectType lookupPotionEffect(String name) {
        if (name == null)
            return null;
        String key = name.toLowerCase();

        // Try lookup by key (may use NamespacedKey.minecraft)
        PotionEffectType byKey = lookupPotionEffectByKey(key);
        if (byKey != null) {
            return byKey;
        }

        // Fallback to reflective getByName to avoid direct deprecated call
        try {
            java.lang.reflect.Method getByName = PotionEffectType.class.getMethod("getByName", String.class);
            Object res = getByName.invoke(null, name);
            if (res instanceof PotionEffectType potioneffecttype)
                return potioneffecttype;
        } catch (ReflectiveOperationException ignored) {
            // ignore
        }

        return null;
    }

    /**
     * Helper: attempt to resolve a PotionEffectType via
     * NamespacedKey.minecraft(key) and PotionEffectType.getByKey,
     * returning null if not available or reflection fails.
     */
    private PotionEffectType lookupPotionEffectByKey(String key) {
        if (key == null)
            return null;
        try {
            java.lang.reflect.Method namespacedMinecraft = NamespacedKey.class.getMethod("minecraft", String.class);
            Object nsKey = namespacedMinecraft.invoke(null, key);
            java.lang.reflect.Method getByKey = PotionEffectType.class.getMethod("getByKey", NamespacedKey.class);
            Object result = getByKey.invoke(null, nsKey);
            if (result instanceof PotionEffectType potioneffecttype)
                return potioneffecttype;
        } catch (ReflectiveOperationException ignored) {
            // ignore and fallback
        }
        return null;
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

        PotionEffectType effectType = lookupPotionEffect(effectName);
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
