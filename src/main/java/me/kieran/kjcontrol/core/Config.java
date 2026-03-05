package me.kieran.kjcontrol.core;

import me.kieran.kjcontrol.util.PluginMessagesUtil;

/**
 * Manages the base config.yml file.
 * Handles file initialisation and version validation.
 * Note: Individual feature states are now managed dynamically by their respective modules.
 */
public final class Config {

    /**
     * The expected schema version of config.yml.
     * Used to detect outdated configurations that may lack new feature keys.
     */
    private static final int LATEST_CONFIG_VERSION = 3;

    // Private constructor to prevent instantiation of this static utility class
    private Config() {}

    /**
     * Initialises or reloads the main configuration from disk.
     * Validates the configuration schema version.
     *
     * @param plugin The main plugin instance handling the reload.
     */
    public static void load(KJControl plugin) {

        try {
            // Ensure the default config.yml exists on disk before attempting to read.
            plugin.saveDefaultConfig();

            // Load the latest state from disk into memory.
            plugin.reloadConfig();

            // Validate configuration schema version (-1 fallback if key is missing entirely)
            int currentVersion = plugin.getConfig().getInt("config-version", -1);
            if (currentVersion != LATEST_CONFIG_VERSION) {
                plugin.getComponentLogger().warn(
                        "config.yml version mismatch! Expected {}, found {}. Some features may behave unexpectedly.",
                        LATEST_CONFIG_VERSION,
                        currentVersion == -1 ? "Unknown" : currentVersion
                );
            }

        } catch (Exception e) {
            plugin.getComponentLogger().error("config.yml failed to load.");
            plugin.getComponentLogger().error(PluginMessagesUtil.defaultErrorMessage(e));
        }

    }

}
