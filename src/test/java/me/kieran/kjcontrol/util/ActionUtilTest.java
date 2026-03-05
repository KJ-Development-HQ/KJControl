package me.kieran.kjcontrol.util;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import me.clip.placeholderapi.PlaceholderAPI;
import me.kieran.kjcontrol.core.KJControl;
import me.kieran.kjcontrol.menu.ConfigMenu;
import me.kieran.kjcontrol.module.KJModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.command.ConsoleCommandSenderMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the ActionUtil bridge layer.
 * Verifies command routing, sender restrictions, and GUI initialisations.
 */
class ActionUtilTest {

    private ServerMock server;
    private KJControl plugin;
    private MockedStatic<PlaceholderAPI> papiMock;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();

        // Safely mock PlaceholderAPI to prevent NoClassDefFoundErrors during component parsing
        papiMock = Mockito.mockStatic(PlaceholderAPI.class);
        papiMock.when(() -> PlaceholderAPI.setPlaceholders(Mockito.any(), Mockito.anyString()))
                .thenAnswer(invocation -> invocation.getArgument(1));

        plugin = MockBukkit.load(KJControl.class);
        ActionUtil.init(plugin);
    }

    @AfterEach
    void tearDown() {
        if (papiMock != null) papiMock.close();
        MockBukkit.unmock();
    }

    /**
     * Helper to verify text sent via Adventure Components.
     *
     * @param sender       The entity that is receiving the message.
     * @param expectedText The text being received.
     */
    private void assertReceived(CommandSender sender, String expectedText) {
        Component message;
        if (sender instanceof PlayerMock player) {
            message = player.nextComponentMessage();
        } else if (sender instanceof ConsoleCommandSenderMock console) {
            message = console.nextComponentMessage();
        } else {
            fail("Unsupported sender type for assertion.");
            return;
        }

        assertNotNull(message, "Expected a message but none was received.");
        String actualText = PlainTextComponentSerializer.plainText().serialize(message);
        assertEquals(expectedText, actualText);
    }

    /**
     * Helper to create a fake Brigadier CommandContext for testing.
     *
     * @param sender          The person sending the command.
     * @param booleanArgValue The boolean argument value.
     * @return The command context
     */
    @SuppressWarnings("unchecked")
    private CommandContext<CommandSourceStack> createMockContext(CommandSender sender, boolean booleanArgValue) {
        CommandContext<CommandSourceStack> ctx = Mockito.mock(CommandContext.class);
        CommandSourceStack stack = Mockito.mock(CommandSourceStack.class);

        // When the code asks for the source, give it our fake stack
        Mockito.when(ctx.getSource()).thenReturn(stack);
        // When the stack asks for the sender, give it our MockBukkit sender
        Mockito.when(stack.getSender()).thenReturn(sender);
        // When the boolean argument is fetched, return the provided value
        Mockito.when(ctx.getArgument("state", Boolean.class)).thenReturn(booleanArgValue);

        return ctx;
    }

    @Test
    @DisplayName("preview() should reject Console senders immediately")
    void testPreviewConsoleRejection() {
        ConsoleCommandSenderMock console = server.getConsoleSender();
        ActionUtil.preview(console);
        assertReceived(console, "Only players can preview chat format!");
    }

    @Test
    @DisplayName("preview() should reject execution if the chat format module is disabled")
    void testPreviewFailsIfDisabled() {
        PlayerMock player = server.addPlayer();
        plugin.getConfigManager().setModuleState("Chat Format", false, null);

        ActionUtil.preview(player);
        assertReceived(player, "KJControl » Cannot preview: The chat format module is currently disabled or invalid.");
    }

    @Test
    @DisplayName("editConfig() should reject Console senders immediately")
    void testEditConfigConsoleRejection() {
        ConsoleCommandSenderMock console = server.getConsoleSender();
        ActionUtil.editConfig(console);
        assertReceived(console, "The configuration GUI is only accessible in-game.");
    }

    @Test
    @DisplayName("editConfig() should successfully open the ConfigMenu GUI for a player")
    void testEditConfigOpensInventory() {
        PlayerMock player = server.addPlayer();
        ActionUtil.editConfig(player);

        // Verify the player actually has an inventory open
        InventoryView openView = player.getOpenInventory();
        assertNotNull(openView, "The GUI failed to open for the player.");

        // Verify it is a ConfigMenu
        InventoryHolder holder = openView.getTopInventory().getHolder();
        assertNotNull(holder, "The opened inventory has no holder.");
        assertInstanceOf(ConfigMenu.class, holder, "The opened GUI was not the ConfigMenu.");
    }

}