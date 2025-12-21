package dev.oumaimaa.models;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Base64;

/**
 * Utility class for serializing and deserializing ItemStacks
 *
 * <p>This class provides methods to convert ItemStacks to Base64 strings
 * and vice versa for storage in MongoDB using modern NBT serialization.</p>
 *
 * @author Oumaimaa
 * @version 1.0.0
 */
public final class ItemStackSerializer {

    private ItemStackSerializer() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Serializes an ItemStack to a Base64 encoded string using NBT bytes
     *
     * @param item the ItemStack to serialize (maybe null or air)
     * @return the Base64 encoded string representation, or null if item is null/air
     */
    public static @Nullable String serialize(@Nullable ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return null; // Or handle empty items differently if needed
        }

        try {
            byte[] bytes = item.serializeAsBytes();
            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize ItemStack", e);
        }
    }

    /**
     * Deserializes a Base64 encoded string to an ItemStack using NBT bytes
     *
     * @param data the Base64 encoded string (maybe null)
     * @return the deserialized ItemStack, or null if data is null/invalid
     */
    public static @Nullable ItemStack deserialize(@Nullable String data) {
        if (data == null || data.isEmpty()) {
            return null;
        }

        try {
            byte[] bytes = Base64.getDecoder().decode(data);
            return ItemStack.deserializeBytes(bytes);
        } catch (Exception e) {
            return null;
        }
    }
}