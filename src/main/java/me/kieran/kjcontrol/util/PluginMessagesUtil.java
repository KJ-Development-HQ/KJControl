package me.kieran.kjcontrol.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

/**
 * Centralised utility for building standard plugin messages.
 * Automatically prepends the customisable plugin prefix.
 */
public final class PluginMessagesUtil {

    // Cached prefix component
    private static Component prefix = Component.empty();

    // Private constructor to prevent instantiation of this static utility class
    private PluginMessagesUtil() {}

    /**
     * Updates the cached prefix. Called by ConfigManager during startup/reload.
     *
     * @param rawPrefix The raw MiniMessage string from config.yml.
     */
    public static void setPrefix(String rawPrefix) {
        if (rawPrefix == null || rawPrefix.isEmpty()) {
            prefix = Component.empty();
        } else {
            prefix = MiniMessage.miniMessage().deserialize(rawPrefix);
        }
    }

    /**
     * Prepends the plugin prefix to a raw MiniMessage string.
     *
     * @param rawMessage The raw message string.
     * @return The formatted Component.
     */
    public static Component format(String rawMessage) {
        return prefix.append(MiniMessage.miniMessage().deserialize(rawMessage));
    }

    /**
     * Prepends the plugin prefix to an existing Component.
     *
     * @param message The Adventure Component.
     * @return The formatted Component.
     */
    public static Component format(Component message) {
        return prefix.append(message);
    }

    /*
        ----------------------------------------------------------------------
        Standardized Messages
        ----------------------------------------------------------------------
     */

    /**
     * Constructs a standardised error message component from an exception.
     * Extracts the exception type and message for clean, readable console logging.
     *
     * @param e The caught exception to format.
     * @return A {@link Component} containing the formatted error details.
     */
    public static Component defaultErrorMessage(Exception e) {
        String cause = e.getClass().getSimpleName();
        String message = (e.getMessage() != null) ? e.getMessage() : "No additional details provided";

        // Utilising modern Java string formatting for clean template construction
        return Component.text("Cause: %s | Message: %s".formatted(cause, message));
    }

    /**
     * Constructs a formatted "no permission" warning message.
     *
     * @param permissionNode The specific permission node the user lacks.
     * @return A formatted MiniMessage {@link Component} ready to be sent to the user.
     */
    public static Component noPermissionMessage(String permissionNode) {
        // Utilising string formatting to safely inject the permission node into the markup
        String errorMessage = "<red>You do not have permission: <dark_gray>%s</dark_gray></red>"
                .formatted(permissionNode);
        return format(errorMessage);
    }

}
