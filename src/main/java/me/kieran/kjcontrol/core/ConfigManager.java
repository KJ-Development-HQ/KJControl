package me.kieran.kjcontrol.core;

import me.kieran.kjcontrol.module.KJModule;
import me.kieran.kjcontrol.util.PluginMessagesUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Central orchestration class for the plugin's lifecycle and modules.
 * Manages the main config.yml and delegates feature logic to registered KJModules.
 */
public class ConfigManager {

    private final KJControl plugin;

    // The Registry: A Map linking the module's name to the module instance.
    // We use LinkedHashMap to maintain the exact order modules are registered in.
    private final Map<String, KJModule> modules = new LinkedHashMap<>();

    /**
     * Constructs the ConfigManager.
     *
     * @param plugin The main plugin instanced injected at startup.
     */
    public ConfigManager(KJControl plugin) {
        this.plugin = plugin;
    }

    /**
     * Registers a module into the system.
     *
     * @param module The {@link KJModule} to register
     */
    public void registerModule(KJModule module) {
        modules.put(module.getName().toLowerCase(), module);
    }

    /**
     * Retrieves a module by its name.
     *
     * @param name The name of the module to retrieve.
     * @return The {@link KJModule} with the specified name.
     */
    public KJModule getModule(String name) {
        return modules.get(name.toLowerCase());
    }

    /**
     * Retrieves all registered modules.
     *
     * @return A collection of all modules.
     */
    public Collection<KJModule> getModules() {
        return modules.values();
    }

    /**
        Loads the main config.yml and triggers a reload for all registered modules.
        Evaluates feature toggles to prevent loading inactive modules in memory.

        @return whether all configs and modules loaded successfully.
     */
    public boolean loadConfigs() {
        try {
            Config.load(plugin);

            String rawPrefix = plugin.getConfig().getString(
                    "prefix", "<gradient:#6ad5ff:#4285eb>KJControl</gradient> <dark_grey>»<dark_grey> "
            );
            PluginMessagesUtil.setPrefix(rawPrefix);

            boolean allSuccessful = true;
            for (KJModule module : modules.values()) {

                // Check what config.yml says this module SHOULD be
                boolean shouldBeEnabled = plugin.getConfig().getBoolean(module.getConfigPath());
                boolean currentlyEnabled = module.isEnabled();

                if (shouldBeEnabled && !currentlyEnabled) {
                    // It's supposed to be on, but it's off. Enable it!
                    if (!module.enable()) {
                        allSuccessful = false;
                        plugin.getConfig().set(module.getConfigPath(), false);
                        plugin.getComponentLogger().warn("Module {} failed to enable and was disabled.", module.getName());
                    }
                } else if (shouldBeEnabled) {
                    // It's supposed to be on, and it's already on. Just reload its files.
                    if (!module.reload()) {
                        allSuccessful = false;
                        module.disable();
                        plugin.getConfig().set(module.getConfigPath(), false);
                        plugin.getComponentLogger().warn("Module {} failed to reload and was disabled.", module.getName());
                    }
                } else if (currentlyEnabled) {
                    module.disable();
                }

            }

            if (!allSuccessful) plugin.saveConfig(); // Save any self-healing changes
            return allSuccessful;

        } catch (Exception e) {
            plugin.getComponentLogger().error("Failed to load plugin configs.");
            plugin.getComponentLogger().error(PluginMessagesUtil.defaultErrorMessage(e));
            return false;
        }
    }

    /**
        Command-triggered wrapper for reloading configurations.
        Provides localised feedback and audit logging.

        @param sender The entity that requested the reload.
     */
    public void loadConfigs(CommandSender sender) {
        // We use the boolean return from loadConfigs() to determine success
        boolean success = loadConfigs();

        if (success) {
            Component msg = MiniMessage.miniMessage().deserialize(
                    "<green>Config reloaded successfully!</green>"
            );
            sender.sendMessage(PluginMessagesUtil.format(msg));

            if (sender instanceof Player player) {
                plugin.getComponentLogger().info("{} reloaded the config", player.name());
            } else {
                plugin.getComponentLogger().info("Config reloaded successfully by Console.");
            }
        } else {
            Component msg = MiniMessage.miniMessage().deserialize(
                    "<red>Failed to reload config! See console for details.</red>"
            );
            sender.sendMessage(PluginMessagesUtil.format(msg));
        }
    }

    /*
        ----------------------------------------------------------------------
        Module State Management
        ----------------------------------------------------------------------
     */

    /**
        Internal helper to save boolean state changes to disk and alert the sender.

        @param sender       The entity changing the setting.
        @param configPath   The YAML path being updated.
        @param moduleName  The display name of the feature.
        @param state        The new boolean state.
     */
    private void persistToggle(CommandSender sender, String configPath, String moduleName, boolean state) {
        plugin.getConfig().set(configPath, state);
        plugin.saveConfig();

        if (sender == null) return;

        if (sender instanceof Player player) {
            player.closeInventory();
        }

        String status = state ? "Enabled" : "Disabled";
        String colour = state ? "green" : "red";

        // Dynamically insert the correct colour and status
        Component feedback = MiniMessage.miniMessage().deserialize(
                "<color:%s>%s %s!</color>".formatted(colour, moduleName, status)
        );

        sender.sendMessage(PluginMessagesUtil.format(feedback));
        if (!(sender instanceof Player)) {
            plugin.getComponentLogger().info(feedback);
        }
    }

    public void setModuleState(String moduleName, boolean state, CommandSender sender) {
        KJModule module = getModule(moduleName);

        if (module == null) {
            if (sender != null) sender.sendMessage(
                    PluginMessagesUtil.format(Component.text("Module not found: " + moduleName))
            );
            return;
        }

        if (state) {
            // Try to enable it. If the module returns false (e.g. broken YAML), abort!
            if (!module.enable()) {
                if (sender != null) {
                    Component errorMsg = MiniMessage.miniMessage().deserialize(
                            "<red>Cannot enable %s: The configuration is invalid or broken. Check console.</red>"
                                    .formatted(module.getName())
                    );
                    sender.sendMessage(PluginMessagesUtil.format(errorMsg));
                    if (sender instanceof Player player) player.closeInventory();
                }
                return;
            }
        } else {
            // If turning off, simply tell the module to disable itself
            module.disable();
        }

        // If we succeeded, persist the new state to config.yml
        persistToggle(sender, module.getConfigPath(), module.getName(), state);
    }
}
