package dev.oumaimaa.util;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

/**
 * Utility class for serializing and deserializing ItemStacks to/from Base64 strings.
 * This allows items to be stored in MongoDB as strings.
 */
public final class ItemSerializer {

    private ItemSerializer() {
    }

    /**
     * Serializes an ItemStack to a Base64 string.
     *
     * @param item the item to serialize
     * @return the Base64 encoded string, or null if serialization fails
     */
    public static String serialize(ItemStack item) {
        if (item == null) {
            return null;
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream)) {

            dataOutput.writeObject(item);
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Deserializes a Base64 string to an ItemStack.
     *
     * @param data the Base64 encoded string
     * @return the deserialized ItemStack, or null if deserialization fails
     */
    public static ItemStack deserialize(String data) {
        if (data == null || data.isEmpty()) {
            return null;
        }

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(data));
             BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {

            return (ItemStack) dataInput.readObject();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}