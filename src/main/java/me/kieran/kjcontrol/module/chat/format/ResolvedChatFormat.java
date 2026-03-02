package me.kieran.kjcontrol.module.chat.format;

import net.kyori.adventure.text.Component;

/**
 * An immutable data record representing a fully-resolved, player-specific chat format.
 * <p>
 *     This record enforces a strict separation of concerns by isolating the rendered,
 *     interactive Adventure {@link Component}s from the raw configuration data found
 *     in {@link ChatFormat}.
 * </p>
 * By the time this object is instantiated:
 * <ul>
 *     <li>All PlaceholderAPI injections have been processed.</li>
 *     <li>All MiniMessage tags have been deserialized.</li>
 *     <li>Interactive hover and click events have been conditionally attached.</li>
 *     <li>The original chat payload has been seamlessly merged into the suffix.</li>
 * </ul>
 * This ensures the chat listener interacts only with a safe, finalised object
 * ready for immediate broadcast to the server.
 *
 * @param prefix         The fully rendered prefix component.
 * @param name           The fully rendered player name component, including any interactive events.
 * @param suffixMessage  The fully rendered suffix component with the player's plain text message appended.
 */
public record ResolvedChatFormat(
        Component prefix,
        Component name,
        Component suffixMessage
) {}
