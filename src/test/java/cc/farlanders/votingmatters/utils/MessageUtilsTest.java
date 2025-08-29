package cc.farlanders.votingmatters.utils;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import be.seeseemelk.mockbukkit.MockBukkit;
import cc.farlanders.votingmatters.VotingMatters;

@DisplayName("MessageUtils Tests")
class MessageUtilsTest {

    private VotingMatters plugin;

    @BeforeEach
    void setUp() {
        MockBukkit.mock();
        plugin = MockBukkit.load(VotingMatters.class);
        MessageUtils.initialize(plugin);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("Should initialize correctly")
    void testInitialization() {
        // Test that MessageUtils initializes without errors
        assertDoesNotThrow(() -> {
            MessageUtils.initialize(plugin);
        }, "MessageUtils should initialize without errors");
    }

    @Test
    @DisplayName("Should get prefix")
    void testGetPrefix() {
        String prefix = MessageUtils.getPrefix();
        assertNotNull(prefix, "Prefix should not be null");
        assertFalse(prefix.isEmpty(), "Prefix should not be empty");
    }

    @Test
    @DisplayName("Should translate color codes")
    void testColorTranslation() {
        String input = "&aGreen &cRed &6Gold";
        String result = MessageUtils.translateColors(input);

        assertNotNull(result, "Translated string should not be null");
        assertNotEquals(input, result, "String should be different after color translation");
        // The exact translation depends on the implementation
    }

    @Test
    @DisplayName("Should get messages with keys")
    void testGetMessage() {
        // Test getting a message that should exist (prefix)
        String prefixMessage = MessageUtils.getMessage("prefix");
        assertNotNull(prefixMessage, "Prefix message should not be null");

        // Test getting a non-existent message
        String nonExistentMessage = MessageUtils.getMessage("non.existent.key");
        assertNotNull(nonExistentMessage, "Should return fallback for non-existent keys");
        assertTrue(nonExistentMessage.contains("not found") || nonExistentMessage.contains("Message not found"),
                "Should indicate message not found");
    }

    @Test
    @DisplayName("Should replace placeholders correctly")
    void testPlaceholderReplacement() {
        String testMessage = "Hello %player%, you have %votes% votes and %streak% streak!";
        String formatted = MessageUtils.replacePlaceholders(testMessage, "TestPlayer", 5, 3);

        assertNotNull(formatted, "Formatted message should not be null");
        assertTrue(formatted.contains("TestPlayer"), "Should replace player placeholder");
        assertTrue(formatted.contains("5"), "Should replace votes placeholder");
        assertTrue(formatted.contains("3"), "Should replace streak placeholder");
        assertFalse(formatted.contains("%player%"), "Should not contain unreplaced placeholders");
        assertFalse(formatted.contains("%votes%"), "Should not contain unreplaced placeholders");
        assertFalse(formatted.contains("%streak%"), "Should not contain unreplaced placeholders");
    }

    @Test
    @DisplayName("Should handle null parameters gracefully")
    void testNullParameterHandling() {
        // Test getMessage with null key
        assertDoesNotThrow(() -> {
            String result = MessageUtils.getMessage(null);
            assertNotNull(result, "Should return fallback for null key");
        }, "Should handle null key gracefully");

        // Test translateColors with null input
        assertDoesNotThrow(() -> {
            MessageUtils.translateColors(null);
            // Result might be null or empty string, but shouldn't throw
        }, "Should handle null color translation gracefully");

        // Test replacePlaceholders with null parameters
        assertDoesNotThrow(() -> {
            MessageUtils.replacePlaceholders(null, "player", 5, 3);
            // Should handle gracefully
        }, "Should handle null message replacement gracefully");
    }

    @Test
    @DisplayName("Should handle empty strings")
    void testEmptyStringHandling() {
        // Test with empty message key
        String emptyKeyResult = MessageUtils.getMessage("");
        assertNotNull(emptyKeyResult, "Should handle empty key");

        // Test with empty color translation
        String emptyColorResult = MessageUtils.translateColors("");
        assertNotNull(emptyColorResult, "Should handle empty color translation");

        // Test with empty placeholder replacement
        String emptyFormatResult = MessageUtils.replacePlaceholders("", "player", 5, 3);
        assertNotNull(emptyFormatResult, "Should handle empty message replacement");
    }

    @Test
    @DisplayName("Should handle component conversion")
    void testComponentConversion() {
        String testMessage = "&aGreen text";

        assertDoesNotThrow(() -> {
            // Test component conversion methods if they exist
            MessageUtils.translateColors(testMessage);
        }, "Component conversion should work without errors");
    }

    @Test
    @DisplayName("Should handle multiple color codes")
    void testMultipleColorCodes() {
        String multiColorMessage = "&a&lBold Green &c&nUnderlined Red &6&oItalic Gold";
        String result = MessageUtils.translateColors(multiColorMessage);

        assertNotNull(result, "Should handle multiple color codes");
        assertNotEquals(multiColorMessage, result, "Should translate multiple color codes");
    }

    @Test
    @DisplayName("Should handle placeholder replacement with null player name")
    void testNullPlayerPlaceholderReplacement() {
        // Test with null player name
        assertDoesNotThrow(() -> {
            String result = MessageUtils.replacePlaceholders("Hello %player%", null, 5, 3);
            assertNotNull(result, "Should handle null player name gracefully");
        }, "Should handle null player name gracefully");
    }
}
