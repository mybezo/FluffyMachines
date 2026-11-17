package io.ncbpfluffybear.fluffymachines.objects;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.groups.NestedItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.groups.SubItemGroup;
import io.github.thebusybiscuit.slimefun4.api.player.PlayerProfile;
import io.github.thebusybiscuit.slimefun4.core.guide.GuideHistory;
import io.github.thebusybiscuit.slimefun4.core.guide.SlimefunGuide;
import io.github.thebusybiscuit.slimefun4.core.guide.SlimefunGuideMode;
import io.github.thebusybiscuit.slimefun4.core.services.sounds.SoundEffect;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.implementation.guide.SurvivalSlimefunGuide;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A {@link NestedItemGroup} that can also show other item groups at fixed slots
 * (for example the "Compressed Items" button at slot 40), and that can be kept out of the main guide menu.
 */
public class FluffyNestedGroup extends NestedItemGroup {

    // sub groups are listed from slot 9, up to the slot before the first fixed slot area
    private static final int FIRST_SLOT = 9;
    private static final int LAST_SLOT = 35;

    private final List<SubItemGroup> subGroups = new ArrayList<>();
    private final Map<Integer, ItemGroup> fixedGroups = new LinkedHashMap<>();
    private final boolean showInMainMenu;

    @ParametersAreNonnullByDefault
    public FluffyNestedGroup(NamespacedKey key, ItemStack item, boolean showInMainMenu) {
        super(key, item);
        this.showInMainMenu = showInMainMenu;
    }

    @Override
    public void addSubGroup(@Nonnull SubItemGroup group) {
        super.addSubGroup(group);
        subGroups.add(group);
    }

    @Override
    public void removeSubGroup(@Nonnull SubItemGroup group) {
        super.removeSubGroup(group);
        subGroups.remove(group);
    }

    /**
     * Shows the given item group at a fixed slot of this menu.
     */
    public void setFixedGroup(int slot, @Nonnull ItemGroup group) {
        fixedGroups.put(slot, group);
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean isVisible(Player p, PlayerProfile profile, SlimefunGuideMode mode) {
        return showInMainMenu && super.isVisible(p, profile, mode);
    }

    @Override
    @ParametersAreNonnullByDefault
    public void open(Player p, PlayerProfile profile, SlimefunGuideMode mode) {
        GuideHistory history = profile.getGuideHistory();
        if (mode == SlimefunGuideMode.SURVIVAL_MODE) {
            history.add(this, 1);
        }

        ChestMenu menu = new ChestMenu(Slimefun.getLocalization().getMessage(p, "guide.title.main"));
        SurvivalSlimefunGuide guide = (SurvivalSlimefunGuide) Slimefun.getRegistry().getSlimefunGuide(mode);

        menu.setEmptySlotsClickable(false);
        menu.addMenuOpeningHandler(SoundEffect.GUIDE_BUTTON_CLICK_SOUND::playFor);
        guide.createHeader(p, profile, menu);

        menu.addItem(1, new CustomItemStack(ChestMenuUtils.getBackButton(
            p, "", ChatColor.GRAY + Slimefun.getLocalization().getMessage(p, "guide.back.guide"))));
        menu.addMenuClickHandler(1, (pl, s, is, action) -> {
            // back to the previous page, which is the main menu or the parent group
            history.goBack(guide);
            return false;
        });

        int slot = FIRST_SLOT;
        for (SubItemGroup group : subGroups) {
            if (slot > LAST_SLOT) {
                break;
            }
            if (!group.isVisibleInNested(p)) {
                continue;
            }
            addGroupButton(menu, slot++, group.getItem(p), group, profile, mode);
        }

        for (Map.Entry<Integer, ItemGroup> entry : fixedGroups.entrySet()) {
            ItemGroup group = entry.getValue();
            if (!hasVisibleContent(group, p, profile, mode)) {
                continue;
            }
            addGroupButton(menu, entry.getKey(), group.getItem(p), group, profile, mode);
        }

        menu.open(p);
    }

    private static void addGroupButton(ChestMenu menu, int slot, ItemStack icon, ItemGroup group,
                                       PlayerProfile profile, SlimefunGuideMode mode) {
        menu.addItem(slot, icon);
        menu.addMenuClickHandler(slot, (pl, s, item, action) -> {
            SlimefunGuide.openItemGroup(profile, group, mode, 1);
            return false;
        });
    }

    private static boolean hasVisibleContent(ItemGroup group, Player p, PlayerProfile profile, SlimefunGuideMode mode) {
        if (group instanceof FluffyNestedGroup nested) {
            return nested.hasVisibleSubGroup(p);
        }
        return group.isVisible(p);
    }

    /**
     * @return whether any sub group of this menu has something to show
     */
    public boolean hasVisibleSubGroup(@Nonnull Player p) {
        for (SubItemGroup group : subGroups) {
            if (group.isVisibleInNested(p)) {
                return true;
            }
        }
        return false;
    }
}
