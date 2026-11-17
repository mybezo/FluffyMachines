package io.ncbpfluffybear.fluffymachines.machines;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.ncbpfluffybear.fluffymachines.objects.AutoCrafter;

import java.util.ArrayList;
import java.util.List;

import me.mrCookieSlime.Slimefun.api.inventory.DirtyChestMenu;
import me.mrCookieSlime.Slimefun.api.item_transport.ItemTransportFlow;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class AutoMagicWorkbench extends AutoCrafter {

    public AutoMagicWorkbench(ItemGroup category, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        this(category, item, recipeType, recipe, 1);
    }

    /**
     * @param multiplier how many machines are combined into this one (1 for the normal machine)
     */
    public AutoMagicWorkbench(ItemGroup category, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe, int multiplier) {
        super(category, item, recipeType, recipe, "&6Auto Magic Workbench", Material.BOOKSHELF, "&6Magic Workbench", RecipeType.MAGIC_WORKBENCH, multiplier);
    }

    /**
     * Modified to accept non-stackable items
     */
    @Override
    public int[] getCustomItemTransport(DirtyChestMenu menu, ItemTransportFlow flow, ItemStack item) {
        if (flow == ItemTransportFlow.WITHDRAW) {
            return getOutputSlots();
        }

        if (item.getType().getMaxStackSize() == 1) {
            return getInputSlots();
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
}
