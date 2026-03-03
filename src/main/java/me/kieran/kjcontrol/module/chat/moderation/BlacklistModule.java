package me.kieran.kjcontrol.module.chat.moderation;

import me.kieran.kjcontrol.core.KJControl;
import me.kieran.kjcontrol.module.AbstractModule;
import me.kieran.kjcontrol.module.chat.ChatPipeline;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Moderation filter that prevents players from using blacklisted words.
 * Pre-compiles a master Regex pattern on load for ultra-fast chat evaluations.
 */
public class BlacklistModule extends AbstractModule implements ChatFilter {

    private final ChatPipeline pipeline;

    private String action;
    private String censorChar;
    private Component cancelMessage;
    private Pattern blacklistPattern;

    public BlacklistModule(KJControl plugin, ChatPipeline pipeline) {
        super(plugin, "Blacklist Filter", "features.moderation.blacklist", "modules/blacklist.yml", 1);
        this.pipeline = pipeline;
    }

    @Override
    protected void onEnable() {
        pipeline.registerFilter(this);
    }

    @Override
    protected void onDisable() {
        pipeline.unregisterFilter(this);
    }

    @Override
    protected boolean onConfigLoad(FileConfiguration config) {
        action = config.getString("action", "BLOCK").toUpperCase();
        censorChar = config.getString("censor-character", "*");

        String rawCancelMsg = config.getString("cancel-message", "<red>That language is not allowed.</red>");
        cancelMessage = MiniMessage.miniMessage().deserialize(rawCancelMsg);

        boolean strictMatching = config.getBoolean("strict-matching", false);
        List<String> words = config.getStringList("blocked-words");

        if (words.isEmpty()) {
            blacklistPattern = null;
            return true;
        }

        String joinedWords = words.stream()
                .map(Pattern::quote)
                .collect(Collectors.joining("|"));

        String regex = strictMatching
                ? "(?i)(" + joinedWords + ")"
                : "(?i)\\b(" + joinedWords + ")\\b";

        blacklistPattern = Pattern.compile(regex);

        return true;
    }

    /*
        ----------------------------------------------------------------------
        ChatFilter Implementation (Business Logic)
        ----------------------------------------------------------------------
     */

    @Override
    public FilterResult check(Player player, String message) {
        if (blacklistPattern == null) {
            return FilterResult.pass();
        }

        Matcher matcher = blacklistPattern.matcher(message);

        if (action.equals("BLOCK")) {
            if (matcher.find()) {
                return FilterResult.fail(cancelMessage);
            }
            return FilterResult.pass();
        }

        if (action.equals("CENSOR")) {
            boolean wasModified = false;
            StringBuilder sb = new StringBuilder();

            while (matcher.find()) {
                wasModified = true;

                String replacement = censorChar.repeat(matcher.group().length());
                matcher.appendReplacement(sb, replacement);
            }

            if (wasModified) {
                matcher.appendTail(sb);
                return FilterResult.modify(sb.toString());
            }
        }

        return FilterResult.pass();
    }

    @Override
    public String getBypassPermission() {
        return "kjcontrol.bypass.blacklist";
    }

}
