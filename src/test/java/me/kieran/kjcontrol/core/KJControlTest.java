package me.kieran.kjcontrol.core;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class KJControlTest {

    private ServerMock server;
    private KJControl plugin;

    @BeforeEach
    void setUp() {
        // Initialise the mocked server environment before each test
        server = MockBukkit.mock();
        // Load the plugin, which automatically calls onLoad() and onEnable()
        plugin = MockBukkit.load(KJControl.class);
    }

    @AfterEach
    void tearDown() {
        // Unload the mocked environment to prevent state bleeding between tests.
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("Plugin should enable successfully in the server environment")
    void testPluginEnablesSuccessfully() {
        assertTrue(plugin.isEnabled(), "The plugin failed to reach an enabled state.");
    }

    @Test
    @DisplayName("ConfigManager should be instantiated and injected on startup")
    void testConfigManagerIsInjected() {
        // Verifies our new Dependency Injection architecture works correctly
        ConfigManager configManager = plugin.getConfigManager();
        assertNotNull(configManager, "ConfigManager should be instantiated during onEnable().");
    }

    @Test
    @DisplayName("Default config.yml should generate in the plugin data folder")
    void testDefaultConfigGeneration() {
        File dataFolder = plugin.getDataFolder();
        File configFile = new File(dataFolder, "config.yml");
        assertTrue(configFile.exists(), "The default config.yml was not copied to the data folder.");
    }

    @Test
    @DisplayName("ConfigManager should successfully register all core modules on startup")
    void testModulesAreRegistered() {
        ConfigManager configManager = plugin.getConfigManager();
        assertFalse(configManager.getModules().isEmpty(), "No modules were registered on startup.");
        assertNotNull(configManager.getModule("Chat Format"), "Chat Format module was not registered.");
        assertNotNull(configManager.getModule("Messages"), "Messages module was not registered.");
        assertEquals(6, configManager.getModules().size(), "Expected exactly 4 modules to be registered.");
    }

}