package me.kieran.kjcontrol.util;

/**
 * An immutable data record representing a single command entry in the dynamic help menu.
 * <p>
 *     This record serves as the foundational data model for the {@link HelpUtil}, allowing
 *     the help menu to be generated and filtered dynamically based on the executing
 *     user's current permissions. This abstraction completely eliminates the need for
 *     hard-coded, static help text.
 * </p>
 *
 * @param command     The literal command string displayed to the user and executed upon click.
 * @param description A concise explanation of the command's functionality.
 * @param permission  the required permission node to view and execute the command,
 *                    or {@code null} if the command is globally accessible.
 */
public record HelpEntry(
        String command,
        String description,
        String permission
) {}
