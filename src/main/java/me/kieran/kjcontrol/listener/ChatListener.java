package me.kieran.kjcontrol.listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.kieran.kjcontrol.config.ConfigManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/*
    Listens for player chat events and applies the custom
    KJControl chat format when enabled.

    This listener does not build chat components itself.
    Instead, it delegates all formatting logic to ChatFormatConfig,
    keeping the listener small and easy to reason about.
 */
public class ChatListener implements Listener {

    /*
        Fired asynchronously whenever a player sends a chat message.

        AsyncChatEvent allows custom rendering of chat messages
        using Adventure Components.
     */
    @EventHandler
    public void onChat(AsyncChatEvent event) {

        /*
        Exit early if:
        - The chat format feature is disabled in config.yml
        - The chat format failed to load or is invalid

        This ensures vanilla chat behaviour is preserved
        when formatting is unavailable.
     */
        if (ConfigManager.isChatFormatDisabled() || !ConfigManager.isChatFormatLoaded()) return;

        /*
            Override the chat renderer for this message.

            The renderer is called once per viewer and is responsible
            for producing the final chat Component.

            Parameters:
            - source  -> the player who sent the message
            - _       -> the source display name (unused)
            - message -> the original chat message Component
            - _       -> the viewer receiving the message (unused)

            All formatting logic is delegated to ConfigManager.
         */
        event.renderer((source, sourceDisplayName, message, audience) -> ConfigManager.getChatFormat(source, message));
    }

}
