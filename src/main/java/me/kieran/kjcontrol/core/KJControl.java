package me.kieran.kjcontrol.core;

import me.kieran.kjcontrol.module.chat.ChatListener;
import me.kieran.kjcontrol.menu.InventoryListener;
import me.kieran.kjcontrol.module.KJModule;
import me.kieran.kjcontrol.module.chat.format.ChatFormatModule;
import me.kieran.kjcontrol.module.chat.ChatPipeline;
import me.kieran.kjcontrol.module.chat.moderation.BlacklistModule;
import me.kieran.kjcontrol.module.chat.moderation.CapsModule;
import me.kieran.kjcontrol.module.chat.moderation.LinkModule;
import me.kieran.kjcontrol.module.chat.moderation.SpamModule;
import me.kieran.kjcontrol.module.messages.MessagesModule;
import me.kieran.kjcontrol.util.ActionUtil;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

/**
 * Main entry point for the KJControl plugin.
 * Handles lifecycle events, dependency injection wire-up, and resource initialisation.
*/
public class KJControl extends JavaPlugin {

    private ConfigManager configManager;

    /**
     * Logic executed when the plugin is enabled by the server.
     * Initialises configurations, logs module status, and registers event listeners.
     */
    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);
        ChatPipeline pipeline = new ChatPipeline();

        // Always register modules before loading configs
        configManager.registerModule(new ChatFormatModule(this, pipeline));
        configManager.registerModule(new MessagesModule(this));
        configManager.registerModule(new CapsModule(this, pipeline));
        configManager.registerModule(new SpamModule(this, pipeline));
        configManager.registerModule(new LinkModule(this, pipeline));
        configManager.registerModule(new BlacklistModule(this, pipeline));
        configManager.loadConfigs();

        // Inject the plugin instance into our command bridge
        ActionUtil.init(this);

        // Log initialisation status of modules using Paper's ComponentLogger
        var logger = getComponentLogger();
        long enabledCount = configManager.getModules().stream().filter(KJModule::isEnabled).count();
        logger.info("Successfully loaded {}/{} modules:", enabledCount, configManager.getModules().size());

        for (KJModule module : configManager.getModules()) {
            String status = module.isEnabled() ? "<green>Enabled</green>" : "<red>Disabled</red>";
            logger.info(MiniMessage.miniMessage().deserialize(" - %s: %s".formatted(module.getName(), status)));
        }

        // Register event handlers to the server's PluginManager.
        var pluginManager = getServer().getPluginManager();
        List.of(
                new ChatListener(pipeline),
                new InventoryListener(configManager)
        ).forEach(listener -> pluginManager.registerEvents(listener, this));
    }

    /**
     * Exposes the ConfigManager for runtime access (e.g., from commands).
     *
     * @return The stored manager instance.
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }

}