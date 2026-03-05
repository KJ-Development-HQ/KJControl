package me.kieran.kjcontrol.module.message;

import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
    Intercepts player connection lifecycle events.
    * Responsible for broadcasting custom formatted join and quit announcements,
    provided the respective configuration toggles are active.
 */
public class PlayerListener implements Listener {

    private final MessagesModule module;

    public PlayerListener(MessagesModule module) {
        this.module = module;
    }

    /**
        Triggered when a player successfully joins the server.

        @param event The synchronous player join event.
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Component message = module.getJoinMessage(event.getPlayer());
        if (message != null) {
            event.joinMessage(message);
        }
    }

    /**
        Triggered when a player disconnects from the server.

        @param event The synchronous player quit event.
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Component message = module.getQuitMessage(event.getPlayer());
        if (message != null) {
            event.quitMessage(message);
        }
    }

}
