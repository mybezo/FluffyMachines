package io.ncbpfluffybear.fluffymachines.compressed;

/**
 * A machine that can have compressed versions: several machines combined into one.
 * <p>
 * The multiplier is 1 for the normal machine. A compressed machine handles up to
 * {@link #getMultiplier()} operations in one go instead of one.
 */
public interface Compressible {

    /**
     * @return how many machines are combined into this one, 1 for the normal machine
     */
    int getMultiplier();

    /**
     * @return whether this is a compressed version of a machine
     */
    default boolean isCompressed() {
        return getMultiplier() > 1;
    }
}
