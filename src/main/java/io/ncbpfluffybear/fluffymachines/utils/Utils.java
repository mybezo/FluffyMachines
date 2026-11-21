package io.ncbpfluffybear.fluffymachines.utils;

import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.implementation.handlers.SimpleBlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.libraries.dough.common.ChatColors;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import io.ncbpfluffybear.fluffymachines.FluffyMachines;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public final class Utils {

    private static final NamespacedKey fluffykey = new NamespacedKey(FluffyMachines.getInstance(), "fluffykey");
    private static final NamespacedKey nonClickable = new NamespacedKey(FluffyMachines.getInstance(), "nonclickable");

    private final static TreeMap<Integer, String> map = new TreeMap<>();

    static {

        map.put(1000, "M");
        map.put(900, "CM");
        map.put(500, "D");
        map.put(400, "CD");
        map.put(100, "C");
        map.put(90, "XC");
        map.put(50, "L");
        map.put(40, "XL");
        map.put(10, "X");
        map.put(9, "IX");
        map.put(5, "V");
        map.put(4, "IV");
        map.put(1, "I");

    }

    private Utils() {
    }

    public static String color(String str) {
        if (str == null) {
            return null;
        }

        return ChatColor.translateAlternateColorCodes('&', str);
    }

    public static void send(CommandSender p, String message) {
        p.sendMessage(color("&7[&6FluffyMachines&7] &r" + message));
    }

    public static String multiBlockWarning() {
        return "&cThis is a Multiblock machine!";
    }

    // TODO: Deprecate custom model data method of detecting non interactables
    public static ItemStack buildNonInteractable(Material material, @Nullable String name, @Nullable String... lore) {
        ItemStack nonClickableItem = new ItemStack(material);
        ItemMeta NCMeta = nonClickableItem.getItemMeta();
        if (name != null) {
            NCMeta.setDisplayName(ChatColors.color(name));
        } else {
            NCMeta.setDisplayName(" ");
        }

        if (lore.length > 0) {
            List<String> lines = new ArrayList<>();

            for (String line : lore) {
                lines.add(ChatColor.translateAlternateColorCodes('&', line));
            }
            NCMeta.setLore(lines);
        }

        NCMeta.getPersistentDataContainer().set(nonClickable, PersistentDataType.BYTE, (byte) 1);
        nonClickableItem.setItemMeta(NCMeta);
        return nonClickableItem;
    }

    // TODO: Deprecate custom model data method of detecting non interactables
    public static boolean checkNonInteractable(ItemStack item) {
        if (item == null || item.getItemMeta() == null) {
            return false;
        }

        return item.getItemMeta().getPersistentDataContainer().getOrDefault(nonClickable, PersistentDataType.BYTE, (byte) 0) == 1;
    }

    public static void createBorder(ChestMenu menu, ItemStack backgroundItem, int[] slots) {
        for (int slot : slots) {
            menu.addItem(slot, backgroundItem, ChestMenuUtils.getEmptyClickHandler());
        }
    }

    public static BlockBreakHandler getDefaultBreakHandler(int[] inputs, int[] outputs) {
        return new SimpleBlockBreakHandler() {

            @Override
            public void onBlockBreak(@Nonnull Block b) {
                BlockMenu inv = StorageCacheUtils.getMenu(b.getLocation());

                if (inv != null) {
                    inv.dropItems(b.getLocation(), inputs);
                    inv.dropItems(b.getLocation(), outputs);
                }
            }

        };
    }

    public static void giveOrDropItem(Player p, ItemStack toGive) {
        for (ItemStack leftover : p.getInventory().addItem(toGive).values()) {
            p.getWorld().dropItemNaturally(p.getLocation(), leftover);
        }
    }

    /**
     * Shows a particle to a player, adding the data newer Minecraft versions require for some particles
     * (for example DRAGON_BREATH needs a Float on newer Paper versions). A particle that can not be shown is skipped,
     * it is only a visual effect and must never break what triggered it.
     */
    public static void spawnParticle(Player p, Particle particle, Location location, int count) {
        try {
            Class<?> dataType = particle.getDataType();
            if (dataType == Float.class) {
                p.spawnParticle(particle, location, count, 0, 0, 0, 1, 1.0F);
            } else if (dataType == Void.class) {
                p.spawnParticle(particle, location, count);
            }
        } catch (IllegalArgumentException ignored) {
            // unsupported particle data on this server version
        }
    }

    /**
     * The name of an item as players see it: its display name, or the English name of its material.
     * (GuizhanLib's helper gives Chinese names for vanilla items.)
     */
    public static String getViewableName(ItemStack item) {
        if (item == null) {
            return "";
        }
        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasDisplayName()) {
                return meta.getDisplayName();
            }
        }
        return humanize(item.getType().name());
    }

    /**
     * The English name of an enchantment, e.g. "Fire Aspect".
     */
    public static String getEnchantmentName(Enchantment enchantment) {
        return humanize(enchantment.getKey().getKey());
    }

    /**
     * Turns a key like "IRON_INGOT" or "fire_aspect" into "Iron Ingot" / "Fire Aspect".
     */
    public static String humanize(String key) {
        StringBuilder builder = new StringBuilder(key.length());
        for (String word : key.toLowerCase(java.util.Locale.ROOT).split("_")) {
            if (word.isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return builder.toString();
    }

    public static String toRoman(int number) {
        int l = map.floorKey(number);
        if (number == l) {
            return map.get(number);
        }
        return map.get(l) + toRoman(number - l);
    }

    public static ItemStack keyItem(ItemStack item) {
        ItemStack clone = item.clone();
        ItemMeta meta = clone.getItemMeta();
        meta.getPersistentDataContainer().set(fluffykey, PersistentDataType.INTEGER, 1);
        clone.setItemMeta(meta);
        return clone;
    }

    public static ItemStack unKeyItem(ItemStack item) {
        ItemStack clone = item.clone();
        ItemMeta meta = clone.getItemMeta();
        meta.getPersistentDataContainer().remove(fluffykey);
        clone.setItemMeta(meta);
        return clone;
    }

    public static boolean canOpen(@Nonnull Block b, @Nonnull Player p) {
        return (p.hasPermission("slimefun.inventory.bypass")
            || Slimefun.getProtectionManager().hasPermission(
            p, b.getLocation(), Interaction.INTERACT_BLOCK));
    }

    // Don't use Slimefun's runsync
    public static BukkitTask runSync(Runnable r) {
        return FluffyMachines.getInstance() != null && FluffyMachines.getInstance().isEnabled() ?
            Bukkit.getScheduler().runTask(FluffyMachines.getInstance(), r) : null;
    }

    public static BukkitTask runSync(Runnable r, long delay) {
        return FluffyMachines.getInstance() != null && FluffyMachines.getInstance().isEnabled() ?
            Bukkit.getScheduler().runTaskLater(FluffyMachines.getInstance(), r, delay) : null;
    }
}

