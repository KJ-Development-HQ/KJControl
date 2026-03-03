package me.kieran.kjcontrol.module;

import org.bukkit.inventory.ItemStack;

/**
 * The core content for all toggleable features within the KJControl plugin.
 * Modules manage their own lifecycles, file I/O, and state to ensure safe
 * enabling, disabling, and reloading without affecting the broader plugin.
 */
public interface KJModule {

    /**
     * @return The path in config.yml that dictates if this module should be on or off.
     */
    String getConfigPath();

    /**
     * Gets the internal identifier of this module.
     * Used for logging, debugging, and registry lookups (e.g., "ChatFormat", "SpamFilter").
     *
     * @return The module's name.
     */
    String getName();

    /**
     * Gets the display item representing this module in the configuration GUI.
     *
     * @return An ItemStack representing the module's state.
     */
    ItemStack getDisplayItem();

    /**
     * Starts the module. Loads files, registers listeners, and builds caches.
     *
     * @return true if successful, false if the files were invalid.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean enable();

    /**
     * Stops the module. Unregisters listeners and clears memory.
     */
    void disable();

    /**
     * Reloads the module's files while it is already running.
     *
     * @return true if successful, false if the new files are invalid.
     */
    boolean reload();

    /**
     * Checks the current runtime state of the module.
     *
     * @return true if the module is active and loaded, false otherwise.
     */
    boolean isEnabled();



}
