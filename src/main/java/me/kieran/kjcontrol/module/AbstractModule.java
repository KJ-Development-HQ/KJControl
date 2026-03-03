package me.kieran.kjcontrol.module;

import me.kieran.kjcontrol.core.KJControl;
import me.kieran.kjcontrol.util.PluginMessagesUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;

/**
 * A base implementation of KJModule that handles boilerplate state management,
 * configuration I/O, and file generation.
 */
public abstract class AbstractModule implements KJModule {

    protected final KJControl plugin;
    private final String name;
    private final String configPath;
    private final String fileName;
    private final int latestVersion;

    private boolean enabled = false;

    /**
     * @param plugin     The main plugin instance.
     * @param name       The display name of the module.
     * @param configPath The path in config.yml used to toggle this module.
     * @param fileName   The name of the module's dedicated config file (e.g., "modules/caps-filter.yml")
     */
    public AbstractModule(KJControl plugin, String name, String configPath, String fileName, int latestVersion) {
        this.plugin = plugin;
        this.name = name;
        this.configPath = configPath;
        this.fileName = fileName;
        this.latestVersion = latestVersion;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getConfigPath() {
        return configPath;
    }

    @Override
    public ItemStack getDisplayItem() {
        Material material = enabled ? Material.LIME_CONCRETE : Material.RED_CONCRETE;
        ItemStack item = ItemStack.of(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            Component nameComponent = Component.text(getName())
                    .color(enabled ? NamedTextColor.GREEN : NamedTextColor.RED)
                    .decoration(TextDecoration.ITALIC, false);

            meta.displayName(nameComponent);
            item.setItemMeta(meta);
        }

        return item;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean enable() {
        if (loadConfig()) {
            enabled = true;
            onEnable();
            return true;
        }
        return false;
    }

    @Override
    public void disable() {
        enabled = false;
        onDisable();
    }

    @Override
    public boolean reload() {
        return loadConfig();
    }

    /**
     * Handles the boilerplate of creating directories, saving default resources,
     * and loading the YAML configuration into memory.
     *
     * @return Whether loading was successful.
     */
    private boolean loadConfig() {
        try {
            File file = new File(plugin.getDataFolder(), fileName);

            if (!file.exists()) {
                File parent = file.getParentFile();

                // Safely check if the parent folder exists, and if we fail to create it, abort.
                if (parent != null && !parent.exists() && !parent.mkdirs()) {
                    plugin.getComponentLogger().error("Failed to create directory for {}", fileName);
                    return false;
                }

                plugin.saveResource(fileName, false);
            }

            FileConfiguration config = YamlConfiguration.loadConfiguration(file);

            int currentVersion = config.getInt("version", -1);
            if (currentVersion != latestVersion) {
                plugin.getComponentLogger().warn(
                        "{} version mismatch! Expected {}, found {}",
                        fileName,
                        latestVersion,
                        currentVersion == -1 ? "Unknown" : currentVersion
                );
            }

            return onConfigLoad(config);
        } catch (Exception e) {
            plugin.getComponentLogger().error("Failed to load {}", fileName);
            plugin.getComponentLogger().error(PluginMessagesUtil.defaultErrorMessage(e));
            return false;
        }
    }

    /*
        ----------------------------------------------------------------------
        Lifecycle Hooks for Child Classes
        ----------------------------------------------------------------------
     */

    /// Fired after the module's config is loaded and its state is set to true.
    protected abstract void onEnable();

    /// Fired when the module is toggled off. Used to unregister listeners/filters.
    protected abstract void onDisable();

    /**
     * Fired when the config file is parsed.
     *
     * @param config The actively loaded FileConfiguration.
     * @return true if the configuration was parsed successfully.
     */
    protected abstract boolean onConfigLoad(FileConfiguration config);
}
