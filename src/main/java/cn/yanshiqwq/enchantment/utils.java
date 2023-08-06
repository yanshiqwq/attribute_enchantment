package cn.yanshiqwq.enchantment;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class utils {
    public static int getAttachedCount(ItemMeta itemMeta){
        NamespacedKey key = new NamespacedKey(Main.getInstance(), "attribute_attached_count");
        PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        return container.getOrDefault(key, PersistentDataType.INTEGER, 0);
    }

    public static void setAttachedCount(ItemMeta itemMeta, int attachedCount){
        NamespacedKey key = new NamespacedKey(Main.getInstance(), "attribute_attached_count");
        PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        container.set(key, PersistentDataType.INTEGER, attachedCount);
        Bukkit.getLogger().info(itemMeta.getAsString() + "\n\tattachedCount = " + attachedCount);
    }
}
