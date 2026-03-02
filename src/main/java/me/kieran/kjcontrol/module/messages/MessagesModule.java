package me.kieran.kjcontrol.module.messages;

import me.kieran.kjcontrol.core.KJControl;
import me.kieran.kjcontrol.module.AbstractModule;
import me.kieran.kjcontrol.module.KJModule;
import me.kieran.kjcontrol.util.PluginMessagesUtil;
import me.kieran.kjcontrol.util.ResolveUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Module responsible for managing custom server messages (Join/Quit, etc.)
 */
public class MessagesModule extends AbstractModule {

    private PlayerListener playerListener;

    private final LinkedHashMap<String, String> joinMessages = new LinkedHashMap<>();
    private final LinkedHashMap<String, String> quitMessages = new LinkedHashMap<>();

    public MessagesModule(KJControl plugin) {
        super(plugin, "Messages", "features.enable-messages", "modules/messages.yml", 2);
    }

    @Override
    protected void onEnable() {
        playerListener = new PlayerListener(this);
        plugin.getServer().getPluginManager().registerEvents(playerListener, plugin);
    }

    @Override
    protected void onDisable() {
        joinMessages.clear();
        quitMessages.clear();

        if (playerListener != null) {
            HandlerList.unregisterAll(playerListener);
            playerListener = null;
        }
    }

    @Override
    protected boolean onConfigLoad(FileConfiguration config) {

        joinMessages.clear();
        quitMessages.clear();

        ConfigurationSection joinSection = config.getConfigurationSection("join-messages");
        if (joinSection != null) {
            for (String key : joinSection.getKeys(false)) {
                joinMessages.put(key, joinSection.getString(key));
            }
        }

        ConfigurationSection quitSection = config.getConfigurationSection("quit-messages");
        if (quitSection != null) {
            for (String key : quitSection.getKeys(false)) {
                quitMessages.put(key, quitSection.getString(key));
            }
        }

        return true;
    }

    /*
        ----------------------------------------------------------------------
        Component Builders
        ----------------------------------------------------------------------
     */

    /**
     * Builds the join message component for a player.
     *
     * @param player The player who joined.
     * @return The formatted Component, or null if disabled/empty.
     */
    public Component getJoinMessage(Player player) {
        if (joinMessages.isEmpty()) return null;

        for (Map.Entry<String, String> entry : joinMessages.entrySet()) {
            String key = entry.getKey();

            if (key.equalsIgnoreCase("default") || player.hasPermission("kjcontrol.join." + key)) {
                return ResolveUtil.parse(player, entry.getValue());
            }
        }

        return null;
    }

    /**
     * Builds the quit message component for a player.
     *
     * @param player The player who quit.
     * @return The formatted Component, or null if disabled/empty.
     */
    public Component getQuitMessage(Player player) {
        if (quitMessages.isEmpty()) return null;

        for (Map.Entry<String, String> entry : quitMessages.entrySet()) {
            String key = entry.getKey();

            if (key.equalsIgnoreCase("default") || player.hasPermission("kjcontrol.quit." + key)) {
                return ResolveUtil.parse(player, entry.getValue());
            }
        }

        return null;
    }

}