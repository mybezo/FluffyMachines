package io.ncbpfluffybear.fluffymachines.compressed;

import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Shared helpers for compressed machines.
 */
public final class CompressedUtils {

    private CompressedUtils() {
    }

    /**
     * Multiplies a value, computed as a long and capped at {@link Integer#MAX_VALUE} so it can not overflow.
     */
    public static int scale(int value, int multiplier) {
        return (int) Math.min((long) value * multiplier, Integer.MAX_VALUE);
    }

    /**
     * Name of a compressed machine: the original name followed by the multiplier.
     */
    @Nonnull
    public static String name(@Nonnull String baseName, int multiplier) {
        return multiplier > 1 ? baseName + " &a(" + multiplier + ")" : baseName;
    }

    /**
     * Finds how many times (up to {@code max}) the given output fits into the slots.
     * The output is never pushed here, the amounts are only checked.
     *
     * @param output one unit of output (its amount is the amount per operation)
     * @return the number of operations whose output fits, between 0 and {@code max}
     */
    public static int maxFitting(@Nonnull BlockMenu menu, @Nonnull ItemStack output, int max, int[] slots) {
        if (max <= 0) {
            return 0;
        }

        // the free space only shrinks as the amount grows, so a binary search works (at most 7 checks for x64)
        int low = 0;
        int high = max;
        while (low < high) {
            int mid = (low + high + 1) >>> 1;
            if (fits(menu, output, mid, slots)) {
                low = mid;
            } else {
                high = mid - 1;
            }
        }
        return low;
    }

    private static boolean fits(BlockMenu menu, ItemStack output, int times, int[] slots) {
        long total = (long) output.getAmount() * times;
        if (total <= 0) {
            return true;
        }
        // there are never more than slots * max stack size items in the output, so this can not overflow
        ItemStack check = output.clone();
        check.setAmount((int) Math.min(total, Integer.MAX_VALUE));
        return menu.fits(check, slots);
    }

    /**
     * Pushes the output of {@code times} operations into the slots, split into stacks
     * of at most the max stack size. Only call this after {@link #maxFitting} said it fits.
     */
    public static void pushInStacks(@Nonnull BlockMenu menu, @Nonnull ItemStack output, int times, int[] slots) {
        long remaining = (long) output.getAmount() * times;
        int maxStackSize = Math.max(1, output.getMaxStackSize());

        while (remaining > 0) {
            int amount = (int) Math.min(remaining, maxStackSize);
            ItemStack stack = output.clone();
            stack.setAmount(amount);
            menu.pushItem(stack, slots);
            remaining -= amount;
        }
    }

    /**
     * How many operations the stored energy pays for, up to {@code max}.
     */
    public static int affordable(int charge, int energyPerOperation, int max) {
        if (energyPerOperation <= 0) {
            return max;
        }
        return Math.min(max, charge / energyPerOperation);
    }

    /**
     * Recipe display items for the guide page of a machine, showing its multiplier.
     * Empty for normal machines, so their guide page does not change.
     */
    @Nonnull
    public static List<ItemStack> displayRecipes(int multiplier) {
        if (multiplier <= 1) {
            return Collections.emptyList();
        }

        List<ItemStack> list = new ArrayList<>();
        list.add(new CustomItemStack(Material.KNOWLEDGE_BOOK, "&eMultiplier: &ax" + multiplier,
            "", "&7" + combinesLore(multiplier)
        ));
        // the guide shows recipes in pairs, keep the next entries aligned
        list.add(null);
        return list;
    }

    @Nonnull
    public static String combinesLore(int multiplier) {
        return "Combines " + multiplier + " machines into one.";
    }
}
