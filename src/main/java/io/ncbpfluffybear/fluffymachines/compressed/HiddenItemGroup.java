package io.ncbpfluffybear.fluffymachines.compressed;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * An item group that is never listed in the guide.
 * Its items are still craftable and can be opened from recipes and the guide search.
 */
public class HiddenItemGroup extends ItemGroup {

    @ParametersAreNonnullByDefault
    public HiddenItemGroup(NamespacedKey key, ItemStack item) {
        super(key, item);
    }

    @Override
    public boolean isVisible(@Nonnull Player p) {
        return false;
    }
}
