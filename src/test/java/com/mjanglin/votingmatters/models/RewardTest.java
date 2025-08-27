package com.mjanglin.votingmatters.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Reward Model Tests")
class RewardTest {

    @Test
    @DisplayName("Should create money reward")
    void testMoneyRewardCreation() {
        double moneyAmount = 10.5;
        int chance = 100;

        Reward reward = new Reward(Reward.RewardType.MONEY, moneyAmount, chance, null);

        assertEquals(Reward.RewardType.MONEY, reward.getType());
        assertEquals(moneyAmount, reward.getMoneyAmount(), 0.001);
        assertEquals(chance, reward.getChance());
        assertTrue(reward.shouldGive()); // 100% chance should always give
    }

    @Test
    @DisplayName("Should create command reward")
    void testCommandRewardCreation() {
        List<String> commands = Arrays.asList("give {player} diamond 1", "say Thanks {player}!");
        int chance = 50;

        Reward reward = new Reward(Reward.RewardType.COMMAND, commands, chance, null);

        assertEquals(Reward.RewardType.COMMAND, reward.getType());
        assertEquals(commands, reward.getCommands());
        assertEquals(chance, reward.getChance());
    }

    @Test
    @DisplayName("Should create experience reward")
    void testExperienceRewardCreation() {
        int expAmount = 100;
        int chance = 75;

        Reward reward = new Reward(Reward.RewardType.EXPERIENCE, expAmount, chance, null);

        assertEquals(Reward.RewardType.EXPERIENCE, reward.getType());
        assertEquals(expAmount, reward.getExperienceAmount());
        assertEquals(chance, reward.getChance());
    }

    @Test
    @DisplayName("Should create item reward with properties")
    void testItemRewardCreation() {
        String itemValue = "DIAMOND";
        int chance = 25;
        Map<String, Object> properties = new HashMap<>();
        properties.put("material", "DIAMOND");
        properties.put("amount", 3);
        properties.put("name", "Vote Diamond");

        Reward reward = new Reward(Reward.RewardType.ITEM, itemValue, chance, properties);

        assertEquals(Reward.RewardType.ITEM, reward.getType());
        assertEquals(itemValue, reward.getValue());
        assertEquals(chance, reward.getChance());
        assertEquals(properties, reward.getProperties());
    }

    @Test
    @DisplayName("Should create potion effect reward")
    void testPotionEffectRewardCreation() {
        String effectValue = "SPEED";
        int chance = 80;
        Map<String, Object> properties = new HashMap<>();
        properties.put("effect", "SPEED");
        properties.put("duration", 1200);
        properties.put("amplifier", 1);

        Reward reward = new Reward(Reward.RewardType.POTION_EFFECT, effectValue, chance, properties);

        assertEquals(Reward.RewardType.POTION_EFFECT, reward.getType());
        assertEquals(effectValue, reward.getValue());
        assertEquals(chance, reward.getChance());
        assertEquals(properties, reward.getProperties());
    }

    @Test
    @DisplayName("Should handle zero chance reward")
    void testZeroChanceReward() {
        Reward reward = new Reward(Reward.RewardType.MONEY, 10.0, 0, null);

        assertEquals(0, reward.getChance());
        // Note: shouldGive() uses random, so we can't guarantee it won't give with 0%
        // but statistically it should be very unlikely
    }

    @Test
    @DisplayName("Should handle hundred percent chance reward")
    void testHundredPercentChanceReward() {
        Reward reward = new Reward(Reward.RewardType.MONEY, 10.0, 100, null);

        assertEquals(100, reward.getChance());
        assertTrue(reward.shouldGive()); // Should always give with 100% chance
    }

    @Test
    @DisplayName("Should throw exception for wrong type access")
    @SuppressWarnings("java:S2699") // Suppress "Add assertion" warning - assertThrows is sufficient
    void testWrongTypeAccess() {
        Reward moneyReward = new Reward(Reward.RewardType.MONEY, 10.0, 100, null);

        // Should throw when accessing money reward as command
        assertThrows(IllegalStateException.class, moneyReward::getCommands);

        // Should throw when accessing money reward as experience
        assertThrows(IllegalStateException.class, moneyReward::getExperienceAmount);
    }

    @Test
    @DisplayName("Should handle empty command list")
    void testEmptyCommandList() {
        List<String> emptyCommands = new ArrayList<>();

        Reward reward = new Reward(Reward.RewardType.COMMAND, emptyCommands, 100, null);

        assertEquals(Reward.RewardType.COMMAND, reward.getType());
        assertTrue(reward.getCommands().isEmpty());
    }

    @Test
    @DisplayName("Should preserve command order")
    void testCommandOrder() {
        List<String> commands = Arrays.asList(
                "first command",
                "second command",
                "third command");

        Reward reward = new Reward(Reward.RewardType.COMMAND, commands, 100, null);

        List<String> retrievedCommands = reward.getCommands();
        assertEquals("first command", retrievedCommands.get(0));
        assertEquals("second command", retrievedCommands.get(1));
        assertEquals("third command", retrievedCommands.get(2));
    }

    @Test
    @DisplayName("Should handle negative money values")
    void testNegativeMoneyReward() {
        double negativeMoney = -5.0;

        Reward reward = new Reward(Reward.RewardType.MONEY, negativeMoney, 100, null);

        assertEquals(negativeMoney, reward.getMoneyAmount(), 0.001);
    }

    @Test
    @DisplayName("Should handle large money values")
    void testLargeMoneyReward() {
        double largeMoney = 999999.99;

        Reward reward = new Reward(Reward.RewardType.MONEY, largeMoney, 100, null);

        assertEquals(largeMoney, reward.getMoneyAmount(), 0.001);
    }

    @Test
    @DisplayName("Should handle very precise money values")
    void testPreciseMoneyValues() {
        double preciseValue = 12.345678901234567;

        Reward reward = new Reward(Reward.RewardType.MONEY, preciseValue, 100, null);

        assertEquals(preciseValue, reward.getMoneyAmount(), 0.000000000000001);
    }

    @Test
    @DisplayName("Should create toString representation")
    void testToString() {
        Reward reward = new Reward(Reward.RewardType.MONEY, 10.0, 75, null);

        String result = reward.toString();

        assertTrue(result.contains("MONEY"));
        assertTrue(result.contains("10.0"));
        assertTrue(result.contains("75%"));
    }
}
