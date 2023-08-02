package cn.yanshiqwq.enchantment.Listener;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Objects;

public class AnvilListener implements Listener {
    // 监听使用铁砧事件
    @EventHandler
    public void onAnvil(PrepareAnvilEvent event){
        ItemStack firstItem = event.getInventory().getFirstItem();
        ItemStack secondItem = event.getInventory().getSecondItem();
        ItemStack resultItem = event.getResult();
        Material modifiersEraser = Material.AMETHYST_SHARD;

        if ((firstItem == null || secondItem == null) || (resultItem == null || secondItem.getType() == modifiersEraser)){
            return;
        }

        Multimap<Attribute, AttributeModifier> mergedModifiers = ArrayListMultimap.create();
        if (secondItem.getType() != Material.BOOK){
            try {
                mergedModifiers.putAll(Objects.requireNonNull(firstItem.getItemMeta().getAttributeModifiers()));
                mergedModifiers.putAll(Objects.requireNonNull(secondItem.getItemMeta().getAttributeModifiers()));
            } catch (Exception ignored) {}
        }
        ItemMeta resultMeta = resultItem.getItemMeta();
        resultMeta.setAttributeModifiers(mergedModifiers);
        firstItem.setItemMeta(resultMeta);
    }
}
