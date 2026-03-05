package me.kieran.kjcontrol.module.chat.moderation;

import me.kieran.kjcontrol.core.KJControl;
import me.kieran.kjcontrol.module.AbstractModule;
import me.kieran.kjcontrol.module.chat.ChatPipeline;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

/// Moderation filter that prevents players from sending messages with excessive capital letters.
public class CapsModule extends AbstractModule implements ChatFilter {

    private final ChatPipeline pipeline;

    // Cached configuration values
    private int minimumLength;
    private int maxCapsPercentage;
    private Component cancelMessage;
    private String action;
    private boolean logInfractions;

    public CapsModule(KJControl plugin, ChatPipeline pipeline) {
        super(plugin, "Caps Filter", "features.moderation.caps-filter", "modules/caps-filter.yml", 2);
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
        minimumLength = config.getInt("minimum-length", 4);
        maxCapsPercentage = config.getInt("max-caps-percentage", 50);
        action = config.getString("action", "BLOCK").toUpperCase();

        String rawCancelMsg = config.getString("cancel-message", "<red>Too many caps!</red>");
        cancelMessage = MiniMessage.miniMessage().deserialize(rawCancelMsg);

        logInfractions = config.getBoolean("database-logging.log-infractions", false);

        return true;
    }

    /*
        ----------------------------------------------------------------------
        ChatFilter Implementation (Business Logic)
        ----------------------------------------------------------------------
     */

    @Override
    public FilterResult check(Player player, String message) {
        // If the message is too short to care about, let it pass
        if (message.length() < minimumLength) return FilterResult.pass();

        // Count the capital letters
        int capsCount = 0;
        for (char c : message.toCharArray()) {
            if (Character.isUpperCase(c)) capsCount++;
        }

        // Calculate the percentage of uppercase characters compared to the total message length
        double percentage = ((double) capsCount / message.length()) * 100;

        // Block or pass based on the configured tolerance
        if (percentage > maxCapsPercentage) {
            String logName = logInfractions ? getName() : null;
            if (action.equals("LOWERCASE")) {
                return FilterResult.modify(message.toLowerCase(), logName);
            } else {
                return FilterResult.fail(cancelMessage, logName);
            }
        }

        return FilterResult.pass();
    }

    @Override
    public String getBypassPermission() {
        return "kjcontrol.bypass.caps";
    }

}