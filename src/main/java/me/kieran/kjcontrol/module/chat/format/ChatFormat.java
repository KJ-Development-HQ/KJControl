package me.kieran.kjcontrol.module.chat.format;

import me.kieran.kjcontrol.util.ResolveUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * An immutable data record representing the raw, unparsed chat format configuration.
 * Stores configuration strings exactly as they exist in chat-format.yml.
 * Delegates rendering to {@link #resolve(Player, Component)} method.
 *
 * @param prefix     The raw prefix string.
 * @param name       The raw player name string (usually contains <username>).
 * @param hoverName  The raw string shown when hovering over the player's name.
 * @param clickName  The raw command string executed when the player's name is clicked.
 * @param suffix     The raw suffix string appearing before the chat message.
 */
public record ChatFormat(
        String prefix,
        String name,
        String hoverName,
        String clickName,
        String suffix
) {

    /**
     * Validates the integrity of the format structure.
     * Empty strings are permitted, but missing (null) keys indicate a broken configuration.
     *
     * @return true if any required field is null, false otherwise.
     */
    public boolean isInvalid() {
        return prefix == null
                || name == null
                || hoverName == null
                || clickName == null
                || suffix == null;
    }

    /**
     * Transforms the raw configuration strings into fully interactive Adventure Components.
     * Applies PlaceholderAPI, MiniMessage deserialization, and interactive click/hover events.
     *
     * @param player  The player sending the chat message.
     * @param message The original chat message component.
     * @return A {@link ResolvedChatFormat} ready to be broadcasted to the server.
     */
    public ResolvedChatFormat resolve(Player player, Component message) {

        // 1. Convert the player's message to plain text and append it to the suffix.
        // This ensures the suffix's MiniMessage colour tags naturally bleed into the message text.
        String plainMessage = PlainTextComponentSerializer.plainText().serialize(message);
        String suffixMessage = suffix + plainMessage;

        // 2. Batch-apply PlaceholderAPI injections to all format segments
        List<String> resolvedStrings = ResolveUtil.applyPlaceholders(
                player, prefix, name, hoverName, clickName, suffixMessage
        );

        /*
            3. Deserialize all strings into Adventure Components using the global MiniMessage
            singleton and our optimised, player-specific tag resolvers.
         */
        Component[] components = new Component[resolvedStrings.size()];
        var resolvers = ResolveUtil.getContextualResolvers(player);
        var mm = MiniMessage.miniMessage();

        for (int i = 0; i < resolvedStrings.size(); i++) {
            components[i] = mm.deserialize(resolvedStrings.get(i), resolvers);
        }

        // 4. Construct the interactive name component
        Component finalName = components[1];

        if (!hoverName.isEmpty()) {
            finalName = finalName.hoverEvent(HoverEvent.showText(components[2]));
        }

        if (!clickName.isEmpty()) {
            // ClickEvent commands must be plain text strings, not components
            String commandString = PlainTextComponentSerializer.plainText().serialize(components[3]);
            finalName = finalName.clickEvent(ClickEvent.runCommand(commandString));
        }

        // 5. Package the finalised components into the resolved record
        return new ResolvedChatFormat(
                components[0], // Prefix
                finalName,     // Interactive Name
                components[4]  // Suffix + Message
        );
    }
}
