package io.ncbpfluffybear.fluffymachines.utils;

import io.github.thebusybiscuit.slimefun4.core.attributes.MachineTier;
import io.github.thebusybiscuit.slimefun4.core.attributes.MachineType;

import javax.annotation.Nonnull;

/**
 * English lore lines for energy, machine tier and speed.
 * <p>
 * The lore builders of the Slimefun and ExtraUtils builds this addon depends on are translated
 * to Chinese, so this addon builds these lines itself. The format (and the numbers) stay the same,
 * only the words are in English.
 */
public final class FluffyLore {

    private FluffyLore() {
    }

    @Nonnull
    public static String powerBuffer(double power) {
        return power(power, " Buffer");
    }

    @Nonnull
    public static String powerPerSecond(double power) {
        return power(dev.j3fftw.extrautils.utils.Utils.perTickToPerSecond(power), "/s");
    }

    @Nonnull
    public static String powerPerTick(double power) {
        return power(power, "/t");
    }

    @Nonnull
    public static String power(double power, @Nonnull String suffix) {
        return "&8⇨ &e⚡ &7" + dev.j3fftw.extrautils.utils.Utils.powerFormatAndFadeDecimals(power) + " J" + suffix;
    }

    @Nonnull
    public static String speed(float speed) {
        return "&8⇨ &b⚡ &7Speed: &b" + speed + 'x';
    }

    @Nonnull
    public static String machine(@Nonnull MachineTier tier, @Nonnull MachineType type) {
        return tierName(tier) + " " + typeName(type);
    }

    @Nonnull
    private static String tierName(@Nonnull MachineTier tier) {
        return switch (tier) {
            case BASIC -> "&eBasic";
            case AVERAGE -> "&6Average";
            case MEDIUM -> "&aMedium";
            case GOOD -> "&2Good";
            case ADVANCED -> "&6Advanced";
            case END_GAME -> "&4End-Game";
        };
    }

    @Nonnull
    private static String typeName(@Nonnull MachineType type) {
        return switch (type) {
            case CAPACITOR -> "Capacitor";
            case GENERATOR -> "Generator";
            case MACHINE -> "Machine";
        };
    }
}
