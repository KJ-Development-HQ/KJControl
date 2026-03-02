package me.kieran.kjcontrol.core;

import me.clip.placeholderapi.PlaceholderAPI;
import me.kieran.kjcontrol.module.KJModule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive integration tests for ConfigManager state transitions,
 * module registry handling, and self-healing mechanics.
 */
class ConfigManagerTest {

    private ServerMock server;
    private KJControl plugin;
    private ConfigManager configManager;

    private MockedStatic<PlaceholderAPI> papiMock;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();

        papiMock = Mockito.mockStatic(PlaceholderAPI.class);
        papiMock.when(() -> PlaceholderAPI.setPlaceholders(Mockito.any(), Mockito.anyString()))
                .thenAnswer(invocation -> invocation.getArgument(1));

        plugin = MockBukkit.load(KJControl.class);
        configManager = plugin.getConfigManager();
    }

    @AfterEach
    void tearDown() {
        if (papiMock != null) {
            papiMock.close();
        }
        MockBukkit.unmock();
    }

    /**
     * Helper method to safely pop and verify the next Adventure Component
     * sent to a MockBukkit player.
     *
     * @param player            The player receiving the message.
     * @param expectedPlainText The text expected to be received by the player.
     */
    private void assertPlayerReceived(PlayerMock player, String expectedPlainText) {
        Component receivedComponent = player.nextComponentMessage();
        assertNotNull(receivedComponent, "Expected a message to be sent to the player, but the queue was empty.");

        String actualPlainText = PlainTextComponentSerializer.plainText().serialize(receivedComponent);
        assertEquals(expectedPlainText, actualPlainText, "The received message did not match the expected text.");
    }

    /*
        ----------------------------------------------------------------------
        System-Wide Reload Tests
        ----------------------------------------------------------------------
     */

    @Test
    @DisplayName("loadConfigs() should return true when standard files are healthy")
    void testLoadConfigsSuccess() {
        boolean success = configManager.loadConfigs();
        assertTrue(success, "ConfigManager failed to load healthy default configurations.");
    }

    @Test
    @DisplayName("loadConfigs(CommandSender) should send success feedback to a player")
    void testLoadConfigsWithFeedback() {
        PlayerMock player = server.addPlayer();
        configManager.loadConfigs(player);
        assertPlayerReceived(player, "KJControl » Config reloaded successfully!");
    }

    /*
        ----------------------------------------------------------------------
        Module State & Self-Healing Tests
        ----------------------------------------------------------------------
     */

    @Test
    @DisplayName("setModuleState(true) should successfully enable a module when its files are valid")
    void testEnableModuleValid() {
        PlayerMock player = server.addPlayer();
        KJModule chatModule = configManager.getModule("Chat Format");

        configManager.setModuleState("Chat Format", false, null);
        assertFalse(chatModule.isEnabled(), "Module should be disabled initially.");

        configManager.setModuleState("Chat Format", true, player);

        assertTrue(chatModule.isEnabled(), "Chat format module failed to enable.");
        assertPlayerReceived(player, "KJControl » Chat Format Enabled!");
    }

    @Test
    @DisplayName("loadConfigs() should disable a module and update config.yml if it fails to load")
    void testSelfHealingOnLoadConfigs() throws IOException {
        KJModule chatModule = configManager.getModule("Chat Format");

        configManager.setModuleState("Chat Format", true, null);
        assertTrue(chatModule.isEnabled(), "Setup failed: Module did not enable.");

        File configFile = new File(plugin.getDataFolder(), "modules/chat-format.yml");
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(configFile);
        yaml.set("format.prefix", null);
        yaml.save(configFile);

        boolean success = configManager.loadConfigs();

        assertFalse(success, "loadConfigs() should return false when a module fails to load.");
        assertFalse(chatModule.isEnabled(), "Module should be disabled in memory if it failed to load.");
        assertFalse(plugin.getConfig().getBoolean(chatModule.getConfigPath()),
                "ConfigManager should have toggled the feature off in config.yml.");
    }

}