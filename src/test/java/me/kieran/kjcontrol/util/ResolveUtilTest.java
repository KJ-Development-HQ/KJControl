package me.kieran.kjcontrol.util;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ResolveUtil.
 * Verifies custom MiniMessage tag resolution and PlaceholderAPI integration.
 */
class ResolveUtilTest {

    private PlayerMock player;
    private MockedStatic<PlaceholderAPI> papiMock;
    private MockedStatic<Bukkit> bukkitMock;

    @BeforeEach
    void setUp() {
        ServerMock server = MockBukkit.mock();

        // Create a player and explicitly set their display name for testing
        player = server.addPlayer("Kieran");
        player.displayName(Component.text("Admin_Kieran"));

        PluginManager pmSpy = Mockito.spy(server.getPluginManager());
        Mockito.doReturn(true).when(pmSpy).isPluginEnabled("PlaceholderAPI");

        bukkitMock = Mockito.mockStatic(Bukkit.class, Mockito.CALLS_REAL_METHODS);
        bukkitMock.when(Bukkit::getPluginManager).thenReturn(pmSpy);

        // Mock PlaceholderAPI to actually replace a specific dummy variable
        papiMock = Mockito.mockStatic(PlaceholderAPI.class);
        papiMock.when(() -> PlaceholderAPI.setPlaceholders(Mockito.any(Player.class), Mockito.anyString()))
                .thenAnswer(invocation -> {
                    String input = invocation.getArgument(1);
                    // Simulate PAPI replacing %player_ping% with "42"
                    return input.replace("%player_ping%", "42");
                });
    }

    @AfterEach
    void tearDown() {
        if (papiMock != null) { papiMock.close(); }
        if (bukkitMock != null) bukkitMock.close();
        MockBukkit.unmock();
    }

    /**
     * Helper method to quickly serialise a {@link Component} to plain text for assertions.
     *
     * @param component The Component to serialise into plain text.
     * @return The serialised Component as a String.
     */
    private String toPlain(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    @Test
    @DisplayName("getContextualResolvers() should successfully map custom <username> and <displayname> tags")
    void testContextualResolvers() {
        TagResolver customTags = ResolveUtil.getContextualResolvers(player);
        String raw = "Hello <username>, also known as <displayname>!";
        Component parsed = MiniMessage.miniMessage().deserialize(raw, customTags);
        assertEquals("Hello Kieran, also known as Admin_Kieran!", toPlain(parsed));
    }

    @Test
    @DisplayName("parse() should seamlessly combine PAPI placeholders and MiniMessage tags")
    void testParse() {
        String raw = "<red>Ping: %player_ping% ms</red> | User: <username>";
        Component parsed = ResolveUtil.parse(player, raw);
        assertEquals("Ping: 42 ms | User: Kieran", toPlain(parsed));
    }

    @Test
    @DisplayName("applyPlaceholders() should process a varargs array of strings correctly")
    void testApplyPlaceholdersVarargs() {
        List<String> results = ResolveUtil.applyPlaceholders(
                player,
                "First ping: %player_ping%",
                "Second string has no PAPI",
                "Third ping: %player_ping%"
        );

        assertEquals(3, results.size());
        assertEquals("First ping: 42", results.getFirst());
        assertEquals("Second string has no PAPI", results.get(1));
        assertEquals("Third ping: 42", results.getLast());
    }

}