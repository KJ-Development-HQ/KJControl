package me.kieran.kjcontrol.module.chat.format;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the ChatFormat record and its resolution logic.
 * Verifies invalidation rules, PlaceholderAPI integration, and Adventure event generation.
 */
class ChatFormatTest {

    private ServerMock server;
    private PlayerMock player;
    private MockedStatic<PlaceholderAPI> papiMock;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        player = server.addPlayer("Kieran");

        papiMock = Mockito.mockStatic(PlaceholderAPI.class);
        papiMock.when(() -> PlaceholderAPI.setPlaceholders(Mockito.any(Player.class), Mockito.anyString()))
                .thenAnswer(invocation -> {
                    String input = invocation.getArgument(1);
                    return input.replace("%player_ping%", "12ms");
                });
    }

    @AfterEach
    void tearDown() {
        if (papiMock != null) papiMock.close();
        MockBukkit.unmock();
    }

    private String toPlain(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    @Test
    @DisplayName("isInvalid() should correctly flag missing configuration keys")
    void testIsInvalid() {
        ChatFormat validFormat = new ChatFormat("[Admin] ", "<username>", "Hover text", "/msg", " > ");
        assertFalse(validFormat.isInvalid(), "Format should be valid when all keys are present.");

        ChatFormat invalidFormat = new ChatFormat("[Admin] ", null, "Hover text", "/msg", " > ");
        assertTrue(invalidFormat.isInvalid(), "Format should be invalid when a required key is null.");
    }

    @Test
    @DisplayName("resolve() should construct fully interactive components with all features enabled")
    void testFullResolution() {
        ChatFormat format = new ChatFormat(
                "<gray>[Admin]</gray> ",
                "<green><username></green>",
                "Ping: %player_ping%",
                "/msg <username> ",
                " <red>></red> <white>"
        );

        Component originalMessage = Component.text("Hello Server!");
        ResolvedChatFormat resolved = format.resolve(player, originalMessage);

        assertEquals("[Admin] ", toPlain(resolved.prefix()));
        assertEquals("Kieran", toPlain(resolved.name()));
        assertEquals(" > Hello Server!", toPlain(resolved.suffixMessage()));

        HoverEvent<?> hover = resolved.name().hoverEvent();
        assertNotNull(hover, "Hover event was not generated.");
        assertEquals(HoverEvent.Action.SHOW_TEXT, hover.action());
        assertEquals("Ping: 12ms", toPlain((Component) hover.value()), "Hover event did not parse PAPI placeholders.");

        ClickEvent click = resolved.name().clickEvent();
        assertNotNull(click, "Click event was not generated.");
        assertEquals(ClickEvent.Action.RUN_COMMAND, click.action());

        ClickEvent.Payload.Text payloadText = assertInstanceOf(
                ClickEvent.Payload.Text.class,
                click.payload(),
                "Click event payload must be of type Text."
        );

        assertEquals("/msg Kieran ", payloadText.value(), "Click event did not resolve MiniMessage username tag.");
    }

    @Test
    @DisplayName("resolve() should safely omit interactive events when strings are empty")
    void testEmptyInteractiveEvents() {
        ChatFormat format = new ChatFormat("[Player] ", "<username>", "", "", " > ");

        ResolvedChatFormat resolved = format.resolve(player, Component.text("Hello"));

        assertNull(resolved.name().hoverEvent(), "Hover event should be null when hoverName is empty.");
        assertNull(resolved.name().clickEvent(), "Click event should be null when clickName is empty.");
    }

}