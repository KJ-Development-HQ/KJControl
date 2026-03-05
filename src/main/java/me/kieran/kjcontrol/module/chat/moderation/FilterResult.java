package me.kieran.kjcontrol.module.chat.moderation;


import net.kyori.adventure.text.Component;

/**
 * An immutable record representing the outcome of a chat moderation filter.
 *
 * @param isCancelled     True if the message violates a rule and should be blocked.
 * @param cancelReason    The specific error message to send to the player (null if not cancelled)
 * @param modifiedMessage The altered message if the filter censors instead of blocks.
 * @param moduleName      The name of the module that caught the infraction (null if logging disabled).
 */
public record FilterResult(boolean isCancelled, Component cancelReason, String modifiedMessage, String moduleName) {

    /**
     * Helper factor for a message that successfully passes the filter.
     *
     * @return A clean FilterResult.
     */
    public static FilterResult pass() {
        return new FilterResult(false, null, null, null);
    }

    /**
     * Helper factory for a message that violates a rule and must be blocked.
     *
     * @param reason     The Adventure Component containing the warning message.
     * @param moduleName The name of the module, or null is logging is disabled.
     * @return A cancelled FilterResult.
     */
    public static FilterResult fail(Component reason, String moduleName) {
        return new FilterResult(true, reason, null, moduleName);
    }

    /**
     * Helper factory for a message that violates a rule but is modified instead of blocked.
     *
     * @param newMessage The message after being modified.
     * @param moduleName The name of the module, or null is logging is disabled.
     * @return A modified FilterResult.
     */
    public static FilterResult modify(String newMessage, String moduleName) {
        return new FilterResult(false, null, newMessage, moduleName);
    }

}
