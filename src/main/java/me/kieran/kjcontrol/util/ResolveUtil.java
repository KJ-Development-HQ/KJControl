package me.kieran.kjcontrol.util;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

/**
 * Utility class for text resolution and placeholder parsing.
 * Bridges PlaceholderAPI string manipulation with Adventure's MiniMessage component system.
 */
public final class ResolveUtil {

    // Private constructor to prevent instantiation of this static utility class
    private ResolveUtil() {}

    /**
     * Generates a contextual TagResolver for a specific player.
     * Maps custom placeholder tags (like <username>) to their respective Adventure Components.
     *
     * @param player The player providing the context for the placeholders.
     * @return A {@link TagResolver} containing the injected player data.
     */
    public static TagResolver getContextualResolvers(Player player) {
        // We only build the dynamic resolvers here, not the entire MiniMessage instance
        return TagResolver.resolver(
                Placeholder.component("username", player.name()),
                Placeholder.component("displayname", player.displayName())
        );
    }

    /**
     * Fully parses a raw string into an Adventure Component.
     * Applies PlaceholderAPI first, then deserializes via MiniMessage with player-specific tags.
     *
     * @param player      The player to parse placeholders against.
     * @param rawMessage  The unformatted configuration string.
     * @return The fully resolved and formatted {@link Component}.
     */
    public static Component parse(Player player, String rawMessage) {
        // 1. Apply legacy PAPI placeholders (%player_name%, etc.)
        String papiResolved = PlaceholderAPI.setPlaceholders(player, rawMessage);

        // 2. Deserialize using the global MiniMessage singleton, passing our dynamic tags directly
        return MiniMessage.miniMessage().deserialize(papiResolved, getContextualResolvers(player));
    }

    /**
     * Bulk applies PlaceholderAPI placeholders to an array of strings.
     *
     * @param player The player context for the placeholders.
     * @param inputs A varargs array of raw strings.
     * @return An immutable {@link List} of all strings with all PAPI placeholders resolved.
     */
    public static List<String> applyPlaceholders(Player player, String... inputs) {
        return Arrays.stream(inputs)
                .map(s -> PlaceholderAPI.setPlaceholders(player, s))
                .toList();
    }

}
