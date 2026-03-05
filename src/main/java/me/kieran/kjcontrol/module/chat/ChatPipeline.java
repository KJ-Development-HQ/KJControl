package me.kieran.kjcontrol.module.chat;

import me.kieran.kjcontrol.module.chat.format.ChatFormatModule;
import me.kieran.kjcontrol.module.chat.moderation.ChatFilter;
import me.kieran.kjcontrol.module.chat.moderation.FilterResult;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The central hub for all chat processing.
 * Moderation filters and formatters register their specific logic here when enabled.
 */
public class ChatPipeline {

    private ChatFormatModule activeFormatter = null;

    // Thread-safe list to prevent ConcurrentModificationExceptions between
    // the main server thread (reloads) and the async chat thread (events).
    private final List<ChatFilter> activeFilters = new CopyOnWriteArrayList<>();

    /*
        ----------------------------------------------------------------------
        Moderation Filter Management
        ----------------------------------------------------------------------
     */

    /**
     * Plugs a new moderation filter into the chat pipeline.
     * Called by a moderation module during its enable() phase.
     *
     * @param filter The ChatFilter implementation to add.
     */
    public void registerFilter(ChatFilter filter) {
        activeFilters.add(filter);
    }

    /**
     * Unplugs a moderation filter from the chat pipeline.
     * Called by a moderation module during its disable() phase.
     *
     * @param filter The ChatFilter implementation to remove.
     */
    public void unregisterFilter(ChatFilter filter) {
        activeFilters.remove(filter);
    }

    /**
     * Passes a chat message through the Chain of Responsibility.
     * Evaluates filters sequentially and halts immediately if a violation is found.
     *
     * @param player  The player sending the message.
     * @param initialMessage The initially sent message.
     * @return A {@link FilterResult} representing a pass or the first encountered failure.
     */
    public FilterResult runFilters(Player player, String initialMessage) {
        String currentMessage = initialMessage;
        boolean wasModified = false;
        String triggeringModule = null;

        for (ChatFilter filter : activeFilters) {

            String bypassPerm = filter.getBypassPermission();
            if (bypassPerm != null && player.hasPermission(bypassPerm)) {
                continue;
            }

            FilterResult result = filter.check(player, currentMessage);

            // If any filter flags the message, stop the chain immediately and return the error
            if (result.isCancelled()) {
                return result;
            }

            if (result.modifiedMessage() != null) {
                currentMessage = result.modifiedMessage();
                wasModified = true;
                if (triggeringModule == null) {
                    triggeringModule = result.moduleName();
                }
            }
        }

        // Return the modified string if changes occurred, otherwise standard pass
        return wasModified ? FilterResult.modify(currentMessage, triggeringModule) : FilterResult.pass();
    }

    /*
        ----------------------------------------------------------------------
        Format Management & Status Checks
        ----------------------------------------------------------------------
     */

    /**
     * Called by ChatFormatModule.enable().
     *
     * @param formatter The module that handles chat formatting.
     */
    public void setFormatter(ChatFormatModule formatter) {
        activeFormatter = formatter;
    }

    /**
     * Called by ChatFormatModule.disable().
     */
    public void removeFormatter() {
        activeFormatter = null;
    }

    public ChatFormatModule getActiveFormatter() {
        return activeFormatter;
    }

    /**
     * Ultra-fast check to see if the pipeline actually needs to process an event.
     *
     * @return true if there is a formatter OR at least one active filter.
     */
    public boolean isActive() {
        return activeFormatter != null || !activeFilters.isEmpty();
    }

}
