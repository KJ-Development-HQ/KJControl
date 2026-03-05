package me.kieran.kjcontrol.module.chat.moderation;

import me.kieran.kjcontrol.core.KJControl;
import me.kieran.kjcontrol.module.AbstractModule;
import me.kieran.kjcontrol.module.chat.ChatPipeline;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Moderation filter that prevents players from sending chat messages too quickly.
 * Manages its own memory clean-up and pipeline integration.
 */
public class SpamModule extends AbstractModule implements ChatFilter {

    private final ChatPipeline pipeline;

    // Cached configuration values
    private long cooldownMs;
    private String rawCancelMsg;
    private boolean logInfractions;

    // Thread-safe cache to track the exact millisecond a player last sent a message
    private final Map<UUID, Long> lastMessagesTimes = new ConcurrentHashMap<>();

    // A dedicated listener just to prevent memory leaks when players log off
    private Listener cleanupListener;

    public SpamModule(KJControl plugin, ChatPipeline pipeline) {
        super(plugin, "Spam Filter", "features.moderation.spam-filter", "modules/spam-filter.yml", 2);
        this.pipeline = pipeline;
    }

    @Override
    protected void onEnable() {
        pipeline.registerFilter(this);
        cleanupListener = new Listener() {
            @EventHandler
            public void onQuit(PlayerQuitEvent event) {
                lastMessagesTimes.remove(event.getPlayer().getUniqueId());
            }
        };
        plugin.getServer().getPluginManager().registerEvents(cleanupListener, plugin);
    }

    @Override
    protected void onDisable() {
        pipeline.unregisterFilter(this);

        if (cleanupListener != null) {
            HandlerList.unregisterAll(cleanupListener);
            cleanupListener = null;
        }

        lastMessagesTimes.clear();
    }

    @Override
    protected boolean onConfigLoad(FileConfiguration config) {
        double seconds = config.getDouble("message-cooldown-seconds", 2.0);
        cooldownMs = (long) (seconds * 1000);

        rawCancelMsg = config.getString("cancel-message", "<red>Please wait <remaining>s before typing again!</red>");
        logInfractions = config.getBoolean("database-logging.log-infractions", false);

        return true;
    }

    /*
        ----------------------------------------------------------------------
        ChatFilter Implementation (Business Logic)
        ----------------------------------------------------------------------
     */

    @Override
    public FilterResult check(Player player, String message) {
        UUID uuid = player.getUniqueId();
        long currentTime = System.currentTimeMillis();

        String logName = logInfractions ? getName() : null;

        if (lastMessagesTimes.containsKey(uuid)) {
            long lastTime = lastMessagesTimes.get(uuid);
            long timeSinceLastMessage = currentTime - lastTime;

            if (timeSinceLastMessage < cooldownMs) {
                double remainingSeconds = (cooldownMs - timeSinceLastMessage) / 1000.0;
                String formattedTime = String.format("%.1f", remainingSeconds);
                Component dynamicCancelMsg = MiniMessage.miniMessage().deserialize(
                        rawCancelMsg,
                        Placeholder.unparsed("remaining", formattedTime)
                );
                return FilterResult.fail(dynamicCancelMsg, logName);
            }
        }

        lastMessagesTimes.put(uuid, currentTime);
        return FilterResult.pass();
    }

    @Override
    public String getBypassPermission() {
        return "kjcontrol.bypass.spam";
    }

}
