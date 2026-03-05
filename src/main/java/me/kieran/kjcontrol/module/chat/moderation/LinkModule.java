package me.kieran.kjcontrol.module.chat.moderation;

import me.kieran.kjcontrol.core.KJControl;
import me.kieran.kjcontrol.module.AbstractModule;
import me.kieran.kjcontrol.module.chat.ChatPipeline;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Moderation filter that prevents players from sending unauthorised links of IP addresses.
 * Extracts base domains and compares them against a configurable whitelist.
 */
public class LinkModule extends AbstractModule implements ChatFilter {

    /*
        The Regex Pattern:
        - (?i) : Case insensitive
        - \\b : Word boundary (prevents matching in the middle of a string arbitrarily)
        - (?:https?://)? : Optionally matches http:// or https://
        - (?:www\\.)? : Optionally matches www.
        - ([a-zA-Z0-9-]+\\.[a-zA-Z]{2,}) : GROUP 1 - Captures the base domain (e.g., discord.gg)
        - \\b : Word boundary
     */
    private static final Pattern URL_PATTERN = Pattern.compile("(?i)\\b(?:https?://)?(?:www\\.)?([a-zA-Z0-9-]+\\.[a-zA-Z]{2,})\\b");

    private final ChatPipeline pipeline;
    private Component cancelMessage;
    private final Set<String> allowedDomains = new HashSet<>();
    private boolean logInfractions;

    public LinkModule(KJControl plugin, ChatPipeline pipeline) {
        super(plugin, "Link Filter", "features.moderation.link-filter", "modules/link-filter.yml", 2);
        this.pipeline = pipeline;
    }

    @Override
    protected void onEnable() {
        pipeline.registerFilter(this);
    }

    @Override
    protected void onDisable() {
        pipeline.unregisterFilter(this);
        allowedDomains.clear();
    }

    @Override
    protected boolean onConfigLoad(FileConfiguration config) {
        String rawCancelMsg = config.getString("cancel-message", "<red>Sending links is not allowed.</red>");
        cancelMessage = MiniMessage.miniMessage().deserialize(rawCancelMsg);

        logInfractions = config.getBoolean("database-logging.log-infractions", false);

        allowedDomains.clear();
        List<String> domains = config.getStringList("allowed-domains");
        for (String domain : domains) {
            allowedDomains.add(domain.toLowerCase());
        }
        return true;
    }

    /*
        ----------------------------------------------------------------------
        ChatFilter Implementation (Business Logic)
        ----------------------------------------------------------------------
     */

    @Override
    public FilterResult check(Player player, String message) {
        Matcher matcher = URL_PATTERN.matcher(message);
        String logName = logInfractions ? getName() : null;

        while (matcher.find()) {
            String domain = matcher.group(1).toLowerCase();
            if (!allowedDomains.contains(domain)) {
                return FilterResult.fail(cancelMessage, logName);
            }
        }

        return FilterResult.pass();
    }

    @Override
    public String getBypassPermission() {
        return "kjcontrol.bypass.links";
    }

}
