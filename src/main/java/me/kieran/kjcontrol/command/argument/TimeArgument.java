package me.kieran.kjcontrol.command.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NullMarked;

import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/// Custom Brigadier argument that parses time formats (e.g., 1d, 5m, 20s) into milliseconds.
@NullMarked
public class TimeArgument implements CustomArgumentType<Long, String> {

    private static final Pattern TIME_PATTERN = Pattern.compile("^(\\d+)([smhd])$");

    private static final DynamicCommandExceptionType ERROR_INVALID_FORMAT = new DynamicCommandExceptionType(value ->
            MessageComponentSerializer.message().serialize(
                    Component.text("Invalid time format: " + value + ". Use s, m, h, or d (e.g., 5m, 1d).")
            )
    );

    @Override
    public Long parse(StringReader reader) throws CommandSyntaxException {
        String input = reader.readUnquotedString();
        Matcher matcher = TIME_PATTERN.matcher(input);

        if (!matcher.matches()) {
            throw ERROR_INVALID_FORMAT.create(input);
        }

        long amount = Long.parseLong(matcher.group(1));
        String unit = matcher.group(2);

        return switch (unit) {
            case "s" -> amount * 1000L;
            case "m" -> amount * 60 * 1000L;
            case "h" -> amount * 60 * 60 * 1000L;
            case "d" -> amount * 24 * 60 * 60 * 1000L;
            default -> throw ERROR_INVALID_FORMAT.create(input);
        };
    }

    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.word();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        // Suggest common time formats
        for (String suggestion : new String[]{"1m", "5m", "1h", "1d", "7d"}) {
            if (suggestion.startsWith(builder.getRemainingLowerCase())) {
                builder.suggest(suggestion);
            }
        }
        return builder.buildFuture();
    }

}
