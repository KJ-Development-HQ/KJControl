package me.kieran.kjcontrol.util;

import java.util.List;

/**
 * Utility class for managing help menu data.
 * Contains the static definitions of all available plugin command and their requirements.
 */
public class HelpUtil {

    /**
     * An immutable, pre-cached list of all available command help entries.
     * Stored statically to prevent unnecessary object allocation on every command execution.
     */
    private static final List<HelpEntry> HELP_ENTRIES = List.of(
            new HelpEntry(
                    "/kjcontrol",
                    "Opens the main plugin menu. Alias: /kjc",
                    "kjcontrol.admin"
            ),
            new HelpEntry(
                    "/kjc preview",
                    "Shows a localised preview of the active chat format",
                    "kjcontrol.preview"
            ),
            new HelpEntry(
                    "/kjc config",
                    "Opens the graphical configuration editor",
                    "kjcontrol.config"
            ),
            new HelpEntry(
                    "/kjc config chat-format-enabled <true/false>",
                    "Toggles the active state of the chat format module",
                    "kjcontrol.config"
            ),
            new HelpEntry(
                    "/kjc config messages-enabled <true/false>",
                    "Toggles the active state of the messages module",
                    "kjcontrol.config"
            ),
            new HelpEntry(
                    "/kjc reload",
                    "Reloads configuration files and caches from disk",
                    "kjcontrol.reload"
            ),
            new HelpEntry(
                    "/kjc help",
                    "Displays this interactive help menu",
                    null
            )
    );

    // Private constructor to prevent instantiation of this static utility class
    private HelpUtil() {}

    /**
     * Retrieves the comprehensive list of command help entries.
     *
     * @return An immutable {@link List} of {@link HelpEntry} objects detailing command usage.
     */
    public static List<HelpEntry> getHelpEntries() {
        return HELP_ENTRIES;
    }

}
