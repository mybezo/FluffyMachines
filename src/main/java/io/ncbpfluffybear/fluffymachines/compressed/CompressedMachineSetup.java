package io.ncbpfluffybear.fluffymachines.compressed;

import io.ncbpfluffybear.fluffymachines.utils.FluffyLore;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.items.groups.SubItemGroup;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.libraries.dough.common.ChatColors;
import io.github.thebusybiscuit.slimefun4.libraries.dough.config.Config;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import io.ncbpfluffybear.fluffymachines.FluffyItemSetup;
import io.ncbpfluffybear.fluffymachines.FluffyMachines;
import io.ncbpfluffybear.fluffymachines.machines.AutoAncientAltar;
import io.ncbpfluffybear.fluffymachines.machines.AutoCraftingTable;
import io.ncbpfluffybear.fluffymachines.machines.AutoEnhancedCraftingTable;
import io.ncbpfluffybear.fluffymachines.machines.AutoMagicWorkbench;
import io.ncbpfluffybear.fluffymachines.machines.AutoTableSaw;
import io.ncbpfluffybear.fluffymachines.machines.SmartFactory;
import io.ncbpfluffybear.fluffymachines.objects.AutoCrafter;
import io.ncbpfluffybear.fluffymachines.objects.FluffyNestedGroup;
import io.ncbpfluffybear.fluffymachines.utils.FluffyItems;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Registers the compressed machines (several machines combined into one), configured by {@code compressed.yml}.
 * <p>
 * Every machine gets one item per multiplier (x8, x16, x32, x64). The first one is made from 8 normal machines
 * in the Compressor, every next one from 2 of the previous multiplier. Only the highest enabled multiplier
 * is listed in the guide, the lower ones are kept in a hidden group (they can still be crafted and are
 * reachable from the recipes and the guide search).
 */
public final class CompressedMachineSetup {

    public static final int[] MULTIPLIERS = {8, 16, 32, 64};

    private static final String CONFIG_FILE = "compressed.yml";

    // slot of the "Compressed Items" button in the main menu of the addon
    private static final int COMPRESSED_ITEMS_SLOT = 40;

    // head texture of the "Compressed Items" button (Mybezo's skin)
    private static final String COMPRESSED_ITEMS_TEXTURE = "82600941fab88efc41bb132d873d3b5c5f35f7720ebd9375476c1a18955bb03e";

    private CompressedMachineSetup() {
    }

    /**
     * Creates the compressed version of a machine for one multiplier.
     */
    @FunctionalInterface
    private interface Factory {
        SlimefunItem create(ItemGroup group, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe, int multiplier);
    }

    /**
     * The category of compressed items, decides the guide group and the config section.
     */
    private enum Category {
        MACHINES("machines");

        private final String configSection;

        Category(String configSection) {
            this.configSection = configSection;
        }
    }

    private static final class Entry {
        private final SlimefunItemStack base;
        private final Category category;
        private final int capacity;
        private final int energy;
        private final Factory factory;

        private Entry(SlimefunItemStack base, Category category, int capacity, int energy, Factory factory) {
            this.base = base;
            this.category = category;
            this.capacity = capacity;
            this.energy = energy;
            this.factory = factory;
        }
    }

    private static List<Entry> entries() {
        List<Entry> entries = new ArrayList<>();

        entries.add(new Entry(FluffyItems.AUTO_CRAFTING_TABLE, Category.MACHINES,
            AutoCraftingTable.CAPACITY, AutoCraftingTable.ENERGY_CONSUMPTION, AutoCraftingTable::new));
        entries.add(new Entry(FluffyItems.AUTO_ENHANCED_CRAFTING_TABLE, Category.MACHINES,
            AutoCrafter.CAPACITY, AutoCrafter.ENERGY_CONSUMPTION, AutoEnhancedCraftingTable::new));
        entries.add(new Entry(FluffyItems.AUTO_MAGIC_WORKBENCH, Category.MACHINES,
            AutoCrafter.CAPACITY, AutoCrafter.ENERGY_CONSUMPTION, AutoMagicWorkbench::new));
        entries.add(new Entry(FluffyItems.AUTO_ANCIENT_ALTAR, Category.MACHINES,
            AutoAncientAltar.CAPACITY, AutoAncientAltar.ENERGY_CONSUMPTION, AutoAncientAltar::new));
        entries.add(new Entry(FluffyItems.AUTO_TABLE_SAW, Category.MACHINES,
            AutoTableSaw.CAPACITY, AutoTableSaw.ENERGY_CONSUMPTION, AutoTableSaw::new));
        entries.add(new Entry(FluffyItems.SMART_FACTORY, Category.MACHINES,
            SmartFactory.getEnergyCapacity(), SmartFactory.getEnergyConsumption(), SmartFactory::new));

        return entries;
    }

    public static void setup(@Nonnull FluffyMachines plugin) {
        List<Entry> entries = entries();
        Config cfg = loadConfig(plugin, entries);

        if (!cfg.getBoolean("enabled")) {
            return;
        }

        // a multiplier needs all the lower multipliers, its recipe is made from the previous one
        List<Integer> enabledMultipliers = new ArrayList<>();
        for (int multiplier : MULTIPLIERS) {
            if (!cfg.getBoolean("multipliers.x" + multiplier)) {
                break;
            }
            enabledMultipliers.add(multiplier);
        }
        if (enabledMultipliers.isEmpty()) {
            return;
        }

        Groups groups = new Groups(plugin);
        int count = 0;

        for (Entry entry : entries) {
            String id = entry.base.getItemId();
            if (!cfg.getBoolean(entry.category.configSection + "." + id)) {
                continue;
            }

            SlimefunItem baseItem = SlimefunItem.getById(id);
            if (baseItem == null || baseItem.isDisabled()) {
                continue;
            }

            SlimefunItemStack previous = entry.base;
            int previousMultiplier = 1;
            int highest = enabledMultipliers.get(enabledMultipliers.size() - 1);

            for (int multiplier : enabledMultipliers) {
                SlimefunItemStack itemStack = createItemStack(entry, multiplier);
                ItemStack[] recipe = new ItemStack[9];
                recipe[0] = new SlimefunItemStack(previous, multiplier / previousMultiplier);

                // only the highest multiplier is listed in the guide
                ItemGroup group = multiplier == highest ? groups.get(entry.category) : groups.hidden;

                entry.factory.create(group, itemStack, RecipeType.COMPRESSOR, recipe, multiplier).register(plugin);
                count++;

                previous = itemStack;
                previousMultiplier = multiplier;
            }
        }

        if (count > 0) {
            FluffyItemSetup.getMainGroup().setFixedGroup(COMPRESSED_ITEMS_SLOT, groups.compressedItems);
            plugin.getLogger().log(Level.INFO, "Registered {0} compressed machines", count);
        }
    }

    /**
     * Loads {@code compressed.yml}, adding the missing options without changing the existing values.
     */
    private static Config loadConfig(FluffyMachines plugin, List<Entry> entries) {
        File file = new File(plugin.getDataFolder(), CONFIG_FILE);
        if (!file.exists()) {
            plugin.saveResource(CONFIG_FILE, false);
        }

        Config cfg = new Config(file);
        cfg.setDefaultValue("enabled", true);
        for (int multiplier : MULTIPLIERS) {
            cfg.setDefaultValue("multipliers.x" + multiplier, true);
        }
        for (Entry entry : entries) {
            cfg.setDefaultValue(entry.category.configSection + "." + entry.base.getItemId(), true);
        }
        cfg.save();
        return cfg;
    }

    private static SlimefunItemStack createItemStack(Entry entry, int multiplier) {
        ItemStack item = new ItemStack(entry.base);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColors.color(CompressedUtils.name(meta.getDisplayName(), multiplier)));

        // the energy lines are rebuilt with the multiplied values, the rest of the lore is kept
        String bufferLine = ChatColors.color(FluffyLore.powerBuffer(entry.capacity));
        String energyLine = ChatColors.color(FluffyLore.powerPerSecond(entry.energy));
        List<String> lore = new ArrayList<>();
        if (meta.hasLore()) {
            for (String line : meta.getLore()) {
                if (line.equals(bufferLine)) {
                    lore.add(ChatColors.color(FluffyLore.powerBuffer(CompressedUtils.scale(entry.capacity, multiplier))));
                } else if (line.equals(energyLine)) {
                    lore.add(ChatColors.color(FluffyLore.powerPerSecond(CompressedUtils.scale(entry.energy, multiplier))));
                } else {
                    lore.add(line);
                }
            }
        }
        lore.add("");
        lore.add(ChatColors.color("&7" + CompressedUtils.combinesLore(multiplier)));
        meta.setLore(lore);
        item.setItemMeta(meta);

        return new SlimefunItemStack(entry.base.getItemId() + "_X" + multiplier, item);
    }

    /**
     * The guide groups of the compressed items, only created when at least one compressed item is enabled.
     */
    private static final class Groups {
        private final FluffyNestedGroup compressedItems;
        private final ItemGroup machines;
        private final ItemGroup hidden;

        private Groups(FluffyMachines plugin) {
            compressedItems = new FluffyNestedGroup(
                new NamespacedKey(plugin, "compressed_items"),
                new CustomItemStack(SlimefunUtils.getCustomHead(COMPRESSED_ITEMS_TEXTURE), "&aCompressed Items",
                    "", "&7Several machines combined into one"),
                false
            );

            // same icon as the original category
            machines = new SubItemGroup(
                new NamespacedKey(plugin, "compressed_machines"), compressedItems,
                new CustomItemStack(Material.SMOKER, "&9Compressed Machines")
            );

            hidden = new HiddenItemGroup(
                new NamespacedKey(plugin, "compressed_hidden"),
                new CustomItemStack(Material.SMOKER, "&9Compressed Machines")
            );
        }

        private ItemGroup get(Category category) {
            return switch (category) {
                case MACHINES -> machines;
            };
        }
    }
}
