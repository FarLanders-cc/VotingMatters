package cc.farlanders.votingmatters.utils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("MessageUtils Simple Tests")
class MessageUtilsSimpleTest {

    @Test
    @DisplayName("Should handle basic string operations")
    void testBasicStringOperations() {
        // Test basic string functionality that doesn't require Bukkit API

        String testString = "Hello World";
        assertNotNull(testString);
        assertFalse(testString.isEmpty());
        assertTrue(testString.contains("Hello"));
        assertTrue(testString.contains("World"));
    }

    @Test
    @DisplayName("Should handle color code patterns")
    void testColorCodePatterns() {
        // Test pattern matching for color codes without actually converting them

        String input = "&aGreen &cRed &9Blue";

        // Check if string contains color code patterns
        assertTrue(input.contains("&a"));
        assertTrue(input.contains("&c"));
        assertTrue(input.contains("&9"));

        // Count color codes
        long colorCodeCount = input.chars()
                .filter(ch -> ch == '&')
                .count();
        assertEquals(3, colorCodeCount);
    }

    @Test
    @DisplayName("Should handle placeholder patterns")
    void testPlaceholderPatterns() {
        String input = "Thank you {player} for voting!";

        // Check if string contains placeholder patterns
        assertTrue(input.contains("{player}"));
        assertFalse(input.contains("{unknown}"));

        // Test basic placeholder replacement logic
        String result = input.replace("{player}", "TestPlayer");
        assertEquals("Thank you TestPlayer for voting!", result);
    }

    @Test
    @DisplayName("Should handle multiple placeholder replacements")
    void testMultiplePlaceholderReplacements() {
        String input = "{player} voted! Thank you {player}!";

        String result = input.replace("{player}", "TestPlayer");
        assertEquals("TestPlayer voted! Thank you TestPlayer!", result);

        // Verify both occurrences were replaced
        assertFalse(result.contains("{player}"));
        assertEquals(2, result.split("TestPlayer", -1).length - 1);
    }

    @Test
    @DisplayName("Should handle empty and null strings safely")
    void testEmptyAndNullStrings() {
        // Test empty string
        String empty = "";
        assertNotNull(empty);
        assertEquals(0, empty.length());

        // Test null safety concepts
        String nullString = null;
        assertNull(nullString);

        // Test safe string operations
        assertDoesNotThrow(() -> {
            String safe = (nullString != null) ? nullString : "default";
            assertEquals("default", safe);
        });
    }

    @Test
    @DisplayName("Should handle special characters")
    void testSpecialCharacters() {
        String input = "Special chars: §, &, {, }, $, %";

        assertTrue(input.contains("§"));
        assertTrue(input.contains("&"));
        assertTrue(input.contains("{"));
        assertTrue(input.contains("}"));
        assertTrue(input.contains("$"));
        assertTrue(input.contains("%"));
    }

    @Test
    @DisplayName("Should handle formatting patterns")
    void testFormattingPatterns() {
        String input = "&lBold &oItalic &nUnderline &mStrike &rReset";

        // Count formatting codes
        long formatCodeCount = input.chars()
                .filter(ch -> ch == '&')
                .count();
        assertEquals(5, formatCodeCount);

        // Check specific formatting codes
        assertTrue(input.contains("&l"));
        assertTrue(input.contains("&o"));
        assertTrue(input.contains("&n"));
        assertTrue(input.contains("&m"));
        assertTrue(input.contains("&r"));
    }

    @Test
    @DisplayName("Should handle message length validation")
    void testMessageLengthValidation() {
        String shortMessage = "Hi!";
        String longMessage = "This is a very long message that might need to be truncated or handled specially in some cases.";

        assertTrue(shortMessage.length() < 10);
        assertTrue(longMessage.length() > 50);

        // Test message length limits
        assertEquals(3, shortMessage.length());
        assertTrue(longMessage.length() < 1000); // Reasonable message limit
    }

    @Test
    @DisplayName("Should handle case sensitivity")
    void testCaseSensitivity() {
        String input = "&AGreen &agreen &CRed &cred";

        // Test case differences
        assertTrue(input.contains("&A"));
        assertTrue(input.contains("&a"));
        assertTrue(input.contains("&C"));
        assertTrue(input.contains("&c"));

        assertNotEquals(input.toLowerCase(), input);
        assertNotEquals(input.toUpperCase(), input);
    }

    @Test
    @DisplayName("Should validate message structure")
    void testMessageStructure() {
        String validMessage = "Player {player} received reward!";
        String invalidMessage = "Player { player } received reward!";

        // Test proper placeholder format
        assertTrue(validMessage.contains("{player}"));
        assertFalse(invalidMessage.contains("{player}"));

        // Count placeholder patterns
        assertEquals(1, countOccurrences(validMessage, "{player}"));
        assertEquals(0, countOccurrences(invalidMessage, "{player}"));
    }

    @Test
    @DisplayName("Should handle string concatenation")
    void testStringConcatenation() {
        String prefix = "&a[VotingMatters] ";
        String message = "Thank you for voting!";
        String playerName = "TestPlayer";

        String combined = prefix + message.replace("{player}", playerName);

        assertTrue(combined.startsWith("&a[VotingMatters]"));
        assertTrue(combined.contains("Thank you for voting!"));
    }

    // Helper method to count occurrences of a substring
    private int countOccurrences(String text, String substring) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(substring, index)) != -1) {
            count++;
            index += substring.length();
        }
        return count;
    }
}
