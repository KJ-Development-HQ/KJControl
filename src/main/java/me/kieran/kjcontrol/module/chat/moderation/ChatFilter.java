package me.kieran.kjcontrol.module.chat.moderation;

import org.bukkit.entity.Player;

/**
 * The standard contract for all chat moderation features within the pipeline.
 * Evaluates chat messages before they are processed by the formatter.
 */
public interface ChatFilter {

    /**
     * Evaluates a chat message against this specific moderation rule.
     *
     * @param player  The player attempting to send the message.
     * @param message The raw, unformatted message string directly from the event.
     * @return A {@link FilterResult} dictating if the message should pass or be blocked.
     */
    FilterResult check(Player player, String message);

    /**
     * Defines the permission node required to bypass this specific filter.
     * By default, returns null (meaning no one can bypass it).
     *
     * @return The permission string, or null.
     */
    default String getBypassPermission() {
        return null;
    }

}
