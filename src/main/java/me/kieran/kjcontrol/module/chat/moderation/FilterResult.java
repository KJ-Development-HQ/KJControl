package me.kieran.kjcontrol.module.chat.moderation;


import net.kyori.adventure.text.Component;

/**
 * An immutable record representing the outcome of a chat moderation filter.
 *
 * @param isCancelled  True if the message violates a rule and should be blocked.
 * @param cancelReason The specific error message to send to the player (null if not cancelled)
 */
public record FilterResult(boolean isCancelled, Component cancelReason, String modifiedMessage) {

    /**
     * Helper factor for a message that successfully passes the filter.
     *
     * @return A clean FilterResult.
     */
    public static FilterResult pass() {
        return new FilterResult(false, null, null);
    }

    /**
     * Helper factory for a message that violates a rule and must be blocked.
     *
     * @param reason The Adventure Component containing the warning message.
     * @return A cancelled FilterResult.
     */
    public static FilterResult fail(Component reason) {
        return new FilterResult(true, reason, null);
    }

    /**
     * Helper factory for a message that violates a rule but is modified instead of blocked.
     *
     * @param newMessage The message after being modified.
     * @return A modified FilterResult.
     */
    public static FilterResult modify(String newMessage) {
        return new FilterResult(false, null, newMessage);
    }

}
