package io.ncbpfluffybear.fluffymachines.objects;

import com.xzavier0722.mc.plugin.slimefun4.storage.controller.SlimefunBlockData;
import com.xzavier0722.mc.plugin.slimefun4.storage.util.StorageCacheUtils;
import io.github.thebusybiscuit.slimefun4.api.events.BlockPlacerPlaceEvent;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetComponent;
import io.github.thebusybiscuit.slimefun4.core.attributes.RecipeDisplayItem;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockPlaceHandler;
import io.github.thebusybiscuit.slimefun4.core.multiblocks.MultiBlockMachine;
import io.github.thebusybiscuit.slimefun4.core.networks.energy.EnergyNetComponentType;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import io.ncbpfluffybear.fluffymachines.compressed.Compressible;
import io.ncbpfluffybear.fluffymachines.compressed.CompressedUtils;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction;
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.inventory.DirtyChestMenu;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AutoCrafter extends SlimefunItem implements EnergyNetComponent, Compressible, RecipeDisplayItem {

    private static final String WIKI_PAGE = "machines/auto-crafters";

    public static final int ENERGY_CONSUMPTION = 128;
    public static final int CAPACITY = ENERGY_CONSUMPTION * 3;
    private final int[] border = {0, 1, 3, 4, 5, 7, 8, 13, 14, 15, 16, 17, 50, 51, 52, 53};
    private final int[] inputBorder = {9, 10, 11, 12, 13, 18, 22, 27, 31, 36, 40, 45, 46, 47, 48, 49};
    private final int[] outputBorder = {23, 24, 25, 26, 32, 35, 41, 42, 43, 44};
    private final int[] inputSlots = {19, 20, 21, 28, 29, 30, 37, 38, 39};
    private final int[] outputSlots = {33, 34};
    private final String machineName;
    private final Material material;
    private final MultiBlockMachine mblock;
    private final int multiplier;

    public AutoCrafter(ItemGroup category, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe, String displayName, Material material, String machineName, RecipeType machineRecipes) {
        this(category, item, recipeType, recipe, displayName, material, machineName, machineRecipes, 1);
    }

    /**
     * @param multiplier how many machines are combined into this one (1 for the normal machine)
     */
    public AutoCrafter(ItemGroup category, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe, String displayName, Material material, String machineName, RecipeType machineRecipes, int multiplier) {
        super(category, item, recipeType, recipe);

        this.machineName = machineName;
        this.material = material;
        this.mblock = (MultiBlockMachine) machineRecipes.getMachine();
        this.multiplier = multiplier;

        constructMenu(CompressedUtils.name(displayName, multiplier));
        addItemHandler(onPlace());
        addItemHandler(onBreak());
    }

    @Override
    public void postRegister() {
        addWikiPage(WIKI_PAGE);
    }

    private void constructMenu(String displayName) {
        new BlockMenuPreset(getId(), displayName) {

            @Override
            public void init() {
                constructMenu(this);
            }

            @Override
            public void newInstance(@Nonnull BlockMenu menu, @Nonnull Block b) {
                SlimefunBlockData blockData = StorageCacheUtils.getBlock(b.getLocation());
                if (blockData.getData("enabled") == null || String.valueOf(false).equals(blockData.getData("enabled"))) {
                    menu.replaceExistingItem(6, new CustomItemStack(Material.GUNPOWDER, "&7Enabled: &4\u2718", "",
                        "&e> Click to enable this Machine")
                    );
                    menu.addMenuClickHandler(6, (p, slot, item, action) -> {
                        blockData.setData("enabled", String.valueOf(true));
                        newInstance(menu, b);
                        return false;
                    });
                } else {
                    menu.replaceExistingItem(6, new CustomItemStack(Material.REDSTONE, "&7Enabled: &2\u2714",
                        "", "&e> Click to disable this Machine")
                    );
                    menu.addMenuClickHandler(6, (p, slot, item, action) -> {
                        blockData.setData("enabled", String.valueOf(false));
                        newInstance(menu, b);
                        return false;
                    });
                }
            }

            @Override
            public boolean canOpen(@Nonnull Block b, @Nonnull Player p) {
                return p.hasPermission("slimefun.inventory.bypass")
                    || Slimefun.getProtectionManager().hasPermission(p, b.getLocation(),
                    Interaction.INTERACT_BLOCK);
            }

            @Override
            public int[] getSlotsAccessedByItemTransport(ItemTransportFlow flow) {
                return new int[0];
            }

            @Override
            public int[] getSlotsAccessedByItemTransport(DirtyChestMenu menu, ItemTransportFlow flow, ItemStack item) {
                return getCustomItemTransport(menu, flow, item);
            }
        };
    }

    protected int[] getCustomItemTransport(DirtyChestMenu menu, ItemTransportFlow flow, ItemStack item) {
        if (flow == ItemTransportFlow.WITHDRAW) {
            return getOutputSlots();
        }

        List<Integer> slots = new ArrayList<>();
        for (int slot : getInputSlots()) {
            if (menu.getItemInSlot(slot) != null) {
                slots.add(slot);
            }
        }

        slots.sort(compareSlots(menu));

        int[] array = new int[slots.size()];

        for (int i = 0; i < slots.size(); i++) {
            array[i] = slots.get(i);
        }

        return array;
    }

    private BlockPlaceHandler onPlace() {
        return new BlockPlaceHandler(true) {

            @Override
            public void onPlayerPlace(@Nonnull BlockPlaceEvent e) {
                StorageCacheUtils.setData(e.getBlock().getLocation(), "enabled", String.valueOf(false));
            }

            @Override
            public void onBlockPlacerPlace(@Nonnull BlockPlacerPlaceEvent e) {
                StorageCacheUtils.setData(e.getBlock().getLocation(), "enabled", String.valueOf(false));
            }
        };
    }

    private BlockBreakHandler onBreak() {
        return new BlockBreakHandler(false, false) {
            @Override
            public void onPlayerBreak(@Nonnull BlockBreakEvent e, @Nonnull ItemStack i, @Nonnull List<ItemStack> list) {
                Block b = e.getBlock();
                BlockMenu inv = StorageCacheUtils.getMenu(b.getLocation());

                if (inv != null) {
                    inv.dropItems(b.getLocation(), getInputSlots());
                    inv.dropItems(b.getLocation(), getOutputSlots());
                }

            }
        };
    }

    protected Comparator<Integer> compareSlots(DirtyChestMenu menu) {
        return Comparator.comparingInt(slot -> menu.getItemInSlot(slot).getAmount());
    }

    protected void constructMenu(BlockMenuPreset preset) {
        borders(preset, border, inputBorder, outputBorder);

        for (int i : getOutputSlots()) {
            preset.addMenuClickHandler(i, new ChestMenu.AdvancedMenuClickHandler() {

                @Override
                public boolean onClick(Player p, int slot, ItemStack cursor, ClickAction action) {
                    return false;
                }

                @Override
                public boolean onClick(InventoryClickEvent e, Player p, int slot, ItemStack cursor,
                                       ClickAction action) {
                    if (cursor == null) return true;
                    return cursor.getType() == Material.AIR;
                }
            });
        }

        preset.addItem(2, new CustomItemStack(new ItemStack(material), "&eRecipe",
                "", "&bPut in the Recipe you want to craft", "&4" + machineName + " &4Recipes ONLY"
            ),
            (p, slot, item, action) -> false);
    }

    /**
     * @return the energy used by one craft
     */
    public int getEnergyConsumption() {
        return ENERGY_CONSUMPTION;
    }

    public int getCapacity() {
        return CompressedUtils.scale(CAPACITY, multiplier);
    }

    @Override
    public int getMultiplier() {
        return multiplier;
    }

    @Nonnull
    @Override
    public List<ItemStack> getDisplayRecipes() {
        return CompressedUtils.displayRecipes(multiplier);
    }

    public int[] getInputSlots() {
        return inputSlots;
    }

    public int[] getOutputSlots() {
        return outputSlots;
    }

    @Nonnull
    @Override
    public EnergyNetComponentType getEnergyComponentType() {
        return EnergyNetComponentType.CONSUMER;
    }

    @Override
    public void preRegister() {
        addItemHandler(new BlockTicker() {

            @Override
            public void tick(Block b, SlimefunItem sf, SlimefunBlockData data) {
                AutoCrafter.this.tick(b);
            }

            @Override
            public boolean isSynchronized() {
                return false;
            }
        });
    }

    protected void tick(Block block) {
        if (String.valueOf(false).equals(StorageCacheUtils.getData(block.getLocation(), "enabled"))) {
            return;
        }

        if (getCharge(block.getLocation()) < getEnergyConsumption()) {
            return;
        }

        craftIfValid(block);
    }

    private void craftIfValid(Block block) {
        BlockMenu menu = StorageCacheUtils.getMenu(block.getLocation());

        // Make sure at least 1 slot is free
        for (int outSlot : getOutputSlots()) {
            ItemStack outItem = menu.getItemInSlot(outSlot);
            if (outItem == null || outItem.getAmount() < outItem.getMaxStackSize()) {
                break;
            } else if (outSlot == getOutputSlots()[1]) {
                return;
            }
        }

        // Find matching recipe
        for (ItemStack[] input : RecipeType.getRecipeInputList(mblock)) {
            if (isCraftable(menu, input)) {
                ItemStack output = RecipeType.getRecipeOutputList(mblock, input).clone();

                // A compressed crafter crafts as many times as the inputs, the energy and the output space allow
                int times = Math.min(multiplier, getAvailableCrafts(menu));
                times = CompressedUtils.affordable(getCharge(block.getLocation()), getEnergyConsumption(), times);
                times = CompressedUtils.maxFitting(menu, output, times, getOutputSlots());
                if (times <= 0) {
                    return;
                }

                craft(output, menu, times);
                removeCharge(block.getLocation(), getEnergyConsumption() * times);
                return;
            }
        }
        // we're only executing the last possible shaped recipe
        // we don't want to allow this to be pressed instead of the default timer-based
        // execution to prevent abuse and auto clickers
    }

    private boolean isCraftable(BlockMenu inv, ItemStack[] recipe) {
        for (int j = 0; j < 9; j++) {
            ItemStack item = inv.getItemInSlot(getInputSlots()[j]);
            if ((item != null && item.getAmount() == 1 && item.getType().getMaxStackSize() != 1)
                || !SlimefunUtils.isItemSimilar(inv.getItemInSlot(getInputSlots()[j]), recipe[j], true)) {
                return false;
            }
        }

        return true;
    }

    /**
     * How many crafts the inputs allow: a stackable item always keeps 1 in its slot as the template,
     * an unstackable item is used up.
     */
    private int getAvailableCrafts(BlockMenu inv) {
        int available = Integer.MAX_VALUE;
        for (int slot : getInputSlots()) {
            ItemStack item = inv.getItemInSlot(slot);
            if (item == null || item.getType() == Material.AIR) {
                continue;
            }
            boolean keepTemplate = item.getType().getMaxStackSize() != 1;
            // a compressed crafter waits until every slot is full enough for the whole batch
            if (!CompressedUtils.isFullEnough(item, multiplier, keepTemplate)) {
                return 0;
            }
            int usable = keepTemplate ? item.getAmount() - 1 : item.getAmount();
            available = Math.min(available, usable);
        }
        return available == Integer.MAX_VALUE ? 0 : available;
    }

    private void craft(ItemStack output, BlockMenu inv, int times) {
        for (int j = 0; j < 9; j++) {
            ItemStack item = inv.getItemInSlot(getInputSlots()[j]);

            if (item != null && item.getType() != Material.AIR) {
                inv.consumeItem(getInputSlots()[j], times);
            }
        }

        CompressedUtils.pushInStacks(inv, output, times, outputSlots);
    }

    static void borders(BlockMenuPreset preset, int[] border, int[] inputBorder, int[] outputBorder) {
        for (int i : border) {
            preset.addItem(i, new CustomItemStack(new ItemStack(Material.GRAY_STAINED_GLASS_PANE), " "),
                (p, slot, item, action) -> false);
        }

        for (int i : inputBorder) {
            preset.addItem(i, new CustomItemStack(new ItemStack(Material.CYAN_STAINED_GLASS_PANE), " "),
                (p, slot, item, action) -> false);
        }

        for (int i : outputBorder) {
            preset.addItem(i, new CustomItemStack(new ItemStack(Material.ORANGE_STAINED_GLASS_PANE), " "),
                (p, slot, item, action) -> false);
        }
    }
}

