package me.kieran.kjcontrol.menu;

import me.kieran.kjcontrol.core.ConfigManager;
import me.kieran.kjcontrol.module.KJModule;
import me.kieran.kjcontrol.util.ActionUtil;
import me.kieran.kjcontrol.util.PluginMessagesUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Intercepts inventory click events to handle interactions within custom plugin menus.
 * Prevents unauthorised item movement and routes clicks to their dynamically mapped actions.
 */
public class InventoryListener implements Listener {

    // 1. Hold a local reference to our injected dependencies
    private final ConfigManager configManager;

    /**
     * Constructs the listener with required dependencies.
     *
     * @param configManager The manager handling formatting and state.
     */
    public InventoryListener(ConfigManager configManager) {
        this.configManager = configManager;
    }

    /**
     * Evaluates inventory interactions and locks down custom GUIs to prevent item theft.
     *
     * @param event The synchronous inventory click event.
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onInventoryClick(InventoryClickEvent event) {

        // 1. Identify the top inventory (the GUI the player is looking at)
        Inventory topInventory = event.getView().getTopInventory();
        InventoryHolder holder = topInventory.getHolder(false);

        boolean isMainMenu = holder instanceof KJControlMenu;
        boolean isConfigMenu = holder instanceof ConfigMenu;

        // If the open menu isn't one of ours, ignore the event entirely
        if (!isMainMenu && !isConfigMenu) return;

        // 2. Lock down the entire view immediately to prevent shift-clicking exploits
        event.setCancelled(true);

        // 3. Ignore clicks outside the GUI boundaries or inside the player's bottom inventory
        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null || !clickedInventory.equals(topInventory)) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType().isAir()) return;

        HumanEntity clicker = event.getWhoClicked();

        // 4. Route the click to the appropriate handler
        if (isMainMenu) {
            handleMainMenuClick(clicker, clicked);
        } else {
            handleConfigMenuClick(clicked, clicker);
        }
    }

    /**
     * Processes interactions specifically within the primary KJControl menu.
     */
    private void handleMainMenuClick(HumanEntity clicker, ItemStack clicked) {
        // Utilising modern Java arrow-switch syntax for clean, break-free routing
        switch (clicked.getType()) {
            case EMERALD_BLOCK -> {
                if (!clicker.hasPermission("kjcontrol.reload")) {
                    clicker.sendMessage(PluginMessagesUtil.noPermissionMessage(
                            "kjcontrol.reload"
                    ));
                    return; //Return early without closing the menu
                }
                ActionUtil.reload(clicker);
                clicker.closeInventory();
            }
            case DIAMOND_BLOCK -> {
                if (!clicker.hasPermission("kjcontrol.preview")) {
                    clicker.sendMessage(PluginMessagesUtil.noPermissionMessage(
                            "kjcontrol.preview"
                    ));
                    return;
                }
                ActionUtil.preview(clicker);
                clicker.closeInventory();
            }
            case IRON_BLOCK -> {
                if (!clicker.hasPermission("kjcontrol.editconfig")) {
                    clicker.sendMessage(PluginMessagesUtil.noPermissionMessage(
                            "kjcontrol.editconfig"
                    ));
                    return;
                }
                ActionUtil.editConfig(clicker);
            }
            default -> {
                // Ignore clicks on filler glass or empty space
            }
        }
    }

    /**
     * Processes interactions dynamically by reading the clicked item's display name.
     */
    private void handleConfigMenuClick(ItemStack clicked, HumanEntity clicker) {
        ItemMeta meta = clicked.getItemMeta();
        if (meta == null || !meta.hasCustomName()) return;

        Component displayName = meta.displayName();
        if (displayName == null) return;

        String plainName = PlainTextComponentSerializer.plainText().serialize(displayName);

        KJModule module = configManager.getModule(plainName);

        if (module != null) {
            configManager.setModuleState(plainName, !module.isEnabled(), clicker);
        }
    }

}
