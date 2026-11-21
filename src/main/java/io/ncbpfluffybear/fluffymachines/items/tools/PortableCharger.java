package io.ncbpfluffybear.fluffymachines.items.tools;


import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.Rechargeable;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemUseHandler;
import io.github.thebusybiscuit.slimefun4.implementation.items.SimpleSlimefunItem;
import io.github.thebusybiscuit.slimefun4.libraries.dough.common.ChatColors;
import io.ncbpfluffybear.fluffymachines.FluffyMachines;
import io.ncbpfluffybear.fluffymachines.utils.Utils;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * The {@link PortableCharger} is an item that opens
 * a portable charging GUI that charges any
 * {@link Rechargeable} item.
 * <p>
 * The charger and the charged item are always read from their slots and written back after every
 * change, so charging works whether the server hands out live items or copies. The item in the
 * charging slot is given back as soon as the GUI closes (also when the player leaves or the server stops).
 *
 * @author NCBPFluffyBear
 */
public class PortableCharger extends SimpleSlimefunItem<ItemUseHandler> implements Rechargeable {

    private static final int[] BORDER = {5, 6, 7, 14, 16, 23, 24, 25};
    private static final int POWER_SLOT = 11;
    private static final int CHARGE_SLOT = 15;
    private static final int INV_SIZE = 27;
    private static final int OFF_HAND_SLOT = 40; // index of the off hand in a PlayerInventory

    // one listener handles the GUIs of all the charger tiers
    private static boolean listenerRegistered = false;

    private final float CHARGE_CAPACITY;
    private final float CHARGE_SPEED;

    public PortableCharger(ItemGroup category, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe,
                           int chargeCapacity, int chargeSpeed) {
        super(category, item, recipeType, recipe);

        this.CHARGE_CAPACITY = chargeCapacity;
        this.CHARGE_SPEED = chargeSpeed;

        if (!listenerRegistered) {
            Bukkit.getPluginManager().registerEvents(new GuiListener(), FluffyMachines.getInstance());
            listenerRegistered = true;
        }
    }

    @Nonnull
    @Override
    public ItemUseHandler getItemHandler() {
        return e -> {
            e.cancel();

            Player p = e.getPlayer();
            int chargerSlot = e.getHand() == EquipmentSlot.OFF_HAND
                ? OFF_HAND_SLOT : p.getInventory().getHeldItemSlot();

            ChargerSession session = new ChargerSession(p, chargerSlot);
            Inventory inventory = session.getInventory();

            ItemStack backgroundItem = Utils.buildNonInteractable(Material.GRAY_STAINED_GLASS_PANE, null);
            ItemStack borderItem = Utils.buildNonInteractable(Material.YELLOW_STAINED_GLASS_PANE, null);

            for (int i = 0; i < INV_SIZE; i++) {
                inventory.setItem(i, backgroundItem);
            }
            for (int slot : BORDER) {
                inventory.setItem(slot, borderItem);
            }
            inventory.setItem(CHARGE_SLOT, null);
            inventory.setItem(POWER_SLOT, Utils.buildNonInteractable(Material.GLOWSTONE, "&4Power"));
            updatePowerSlot(inventory, getItemCharge(e.getItem()));

            p.openInventory(inventory);
            session.task = Bukkit.getScheduler().runTaskTimer(FluffyMachines.getInstance(), session::tick, 20, 20);
        };
    }

    private static void updatePowerSlot(Inventory inventory, float charge) {
        ItemStack item = inventory.getItem(POWER_SLOT);
        if (item == null) {
            return;
        }
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColors.color("&6&lPower Remaining"));
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.translateAlternateColorCodes('&', "&e" + charge + "J"));
        meta.setLore(lore);
        item.setItemMeta(meta);
        inventory.setItem(POWER_SLOT, item);
    }

    private static boolean isCharger(ItemStack item) {
        return SlimefunItem.getByItem(item) instanceof PortableCharger;
    }

    @Override
    public float getMaxItemCharge(ItemStack itemStack) {
        return CHARGE_CAPACITY;
    }

    @Override
    public boolean isDisenchantable() {
        return false;
    }

    /**
     * One open charging GUI. It is the holder of its inventory, so the GUI is recognised without its title.
     */
    private static final class ChargerSession implements InventoryHolder {
        private final Player player;
        private final int chargerSlot;
        private final Inventory inventory;
        private BukkitTask task;
        private String lastMessage;

        private ChargerSession(Player player, int chargerSlot) {
            this.player = player;
            this.chargerSlot = chargerSlot;
            this.inventory = Bukkit.createInventory(this, INV_SIZE, ChatColor.GOLD + "Portable Charger");
        }

        @Nonnull
        @Override
        public Inventory getInventory() {
            return inventory;
        }

        private void sendOnce(String message) {
            if (!message.equals(lastMessage)) {
                Utils.send(player, message);
                lastMessage = message;
            }
        }

        /**
         * Runs every second while the GUI is open.
         */
        private void tick() {
            if (!player.isOnline() || player.getOpenInventory().getTopInventory().getHolder() != this) {
                // closed without an event (should not happen), give the item back and stop
                close();
                return;
            }

            ItemStack chargerItem = player.getInventory().getItem(chargerSlot);
            if (!(SlimefunItem.getByItem(chargerItem) instanceof PortableCharger charger)) {
                Utils.send(player, "&cWhere did your portable charger go?");
                player.closeInventory();
                return;
            }

            ItemStack deviceItem = inventory.getItem(CHARGE_SLOT);
            SlimefunItem sfDevice = SlimefunItem.getByItem(deviceItem);

            if (sfDevice instanceof PortableCharger) {
                Utils.send(player, "&cYou can not charge a portable charger");
                player.closeInventory();
                return;
            }

            if (sfDevice instanceof Rechargeable device) {
                float neededCharge = device.getMaxItemCharge(deviceItem) - device.getItemCharge(deviceItem);
                float availableCharge = charger.getItemCharge(chargerItem);

                if (player.getGameMode() == GameMode.CREATIVE && neededCharge > 0) {
                    device.setItemCharge(deviceItem, device.getMaxItemCharge(deviceItem));
                    inventory.setItem(CHARGE_SLOT, deviceItem);

                } else if (neededCharge > 0 && availableCharge > 0) {
                    lastMessage = null;
                    float charge = Math.min(charger.CHARGE_SPEED, Math.min(neededCharge, availableCharge));

                    charger.removeItemCharge(chargerItem, charge);
                    device.addItemCharge(deviceItem, charge);

                    // write both items back, in case the server gave us copies
                    player.getInventory().setItem(chargerSlot, chargerItem);
                    inventory.setItem(CHARGE_SLOT, deviceItem);

                } else if (neededCharge <= 0) {
                    sendOnce("&cThis item is already full!");

                } else {
                    sendOnce("&cYour charger does not have enough power!");
                }
            }

            updatePowerSlot(inventory, charger.getItemCharge(chargerItem));
        }

        /**
         * Stops charging and gives the item in the charging slot back. Safe to call more than once.
         */
        private void close() {
            if (task != null) {
                task.cancel();
                task = null;
            }

            ItemStack item = inventory.getItem(CHARGE_SLOT);
            if (item != null && item.getType() != Material.AIR) {
                inventory.setItem(CHARGE_SLOT, null);
                Utils.giveOrDropItem(player, item);
            }
        }
    }

    /**
     * Protects the charging GUI and gives the item back when it closes.
     */
    private static final class GuiListener implements Listener {

        @EventHandler(priority = EventPriority.LOWEST)
        public void onClick(InventoryClickEvent e) {
            if (!(e.getView().getTopInventory().getHolder() instanceof ChargerSession)) {
                return;
            }

            // only the charging slot of the GUI can be used
            if (e.getClickedInventory() == e.getView().getTopInventory() && e.getSlot() != CHARGE_SLOT) {
                e.setCancelled(true);
                return;
            }

            // the charger must stay where it is while the GUI is open
            ItemStack swapped = null;
            if (e.getHotbarButton() >= 0) {
                swapped = e.getWhoClicked().getInventory().getItem(e.getHotbarButton());
            } else if (e.getClick() == ClickType.SWAP_OFFHAND) {
                swapped = e.getWhoClicked().getInventory().getItemInOffHand();
            }

            if (isCharger(e.getCurrentItem()) || isCharger(e.getCursor()) || isCharger(swapped)
                || e.getClick() == ClickType.DOUBLE_CLICK) {
                e.setCancelled(true);
            }
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onDrag(InventoryDragEvent e) {
            if (!(e.getView().getTopInventory().getHolder() instanceof ChargerSession)) {
                return;
            }

            if (isCharger(e.getOldCursor())) {
                e.setCancelled(true);
                return;
            }

            for (int slot : e.getRawSlots()) {
                if (slot < INV_SIZE && slot != CHARGE_SLOT) {
                    e.setCancelled(true);
                    return;
                }
            }
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onClose(InventoryCloseEvent e) {
            // also fires when the player leaves or the server stops, before the player data is saved
            if (e.getInventory().getHolder() instanceof ChargerSession session) {
                session.close();
            }
        }
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public enum Type {

        SMALL(128, 8),
        MEDIUM(512, 32),
        BIG(1024, 64),
        LARGE(8192, 512),
        CARBONADO(65526, 4096);

        public final int chargeCapacity;
        public final int chargeSpeed;

    }
}
