package me.kieran.kjcontrol.module.chat;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.kieran.kjcontrol.core.KJControl;
import me.kieran.kjcontrol.module.chat.format.ResolvedChatFormat;
import me.kieran.kjcontrol.module.chat.moderation.FilterResult;
import me.kieran.kjcontrol.util.PluginMessagesUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
    Intercepts asynchronous chat events to route them through the ChatPipeline.
    Handles both the moderation filtering and the custom format application.
 */
public class ChatListener implements Listener {

    private final KJControl plugin;
    private final ChatPipeline pipeline;

    /**
     * Constructs the listener with required dependencies.
     *
     * @param plugin   The main plugin instance for accessing repositories.
     * @param pipeline The active {@link ChatPipeline}.
     */
    public ChatListener(KJControl plugin, ChatPipeline pipeline) {
        this.plugin = plugin;
        this.pipeline = pipeline;
    }

    /**
        Fired asynchronously whenever a player broadcasts a chat message.
        Overrides the default Paper chat renderer with a custom Adventure Component renderer.

        @param event The asynchronous chat event containing the message payload.
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        String rawMessage = PlainTextComponentSerializer.plainText().serialize(event.message());

        // Fire the raw message to the database asynchronously before any mutations occur.
        if (plugin.getLogRepository() != null && !player.hasPermission("kjcontrol.bypass.logging")) {
            plugin.getLogRepository().logChat(player.getUniqueId(), player.getName(), rawMessage)
                    .exceptionally(throwable -> {
                        plugin.getComponentLogger().error("Async database thread rejected chat log task!", throwable);
                        return null;
                    });
        }

        // If NO chat modules are enabled, yield immediately to vanilla Bukkit
        if (!pipeline.isActive()) return;

        FilterResult result = pipeline.runFilters(player, rawMessage);

        if (result.moduleName() != null &&
                plugin.getLogRepository() != null &&
                !player.hasPermission("kjcontrol.bypass.logging")
        ) {
            plugin.getLogRepository()
                    .logInfraction(player.getUniqueId(), player.getName(), result.moduleName(), rawMessage)
                    .exceptionally(throwable -> {
                        plugin.getComponentLogger().error("Async database thread rejected infraction log task!", throwable);
                        return null;
                    });
        }

        // If a filter blocked the message, cancel the event and warn the player.
        if (result.isCancelled()) {
            event.setCancelled(true);
            if (result.cancelReason() != null) {
                player.sendMessage(PluginMessagesUtil.format(result.cancelReason()));
            }
            return;
        }

        if (result.modifiedMessage() != null) {
            event.message(Component.text(result.modifiedMessage()));
        }

        // If it passed the filters (or none exist) and a formatter is attached, apply the visual format
        if (pipeline.getActiveFormatter() != null) {
            event.renderer((source, sourceDisplayName, message, audience) -> {
                ResolvedChatFormat format = pipeline.getActiveFormatter().getFormat().resolve(source, message);
                return format.prefix().append(format.name()).append(format.suffixMessage());
            });
        }
    }

}
