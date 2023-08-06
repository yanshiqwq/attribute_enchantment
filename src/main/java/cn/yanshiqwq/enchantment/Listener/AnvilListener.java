package cn.yanshiqwq.enchantment.Listener;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

import static cn.yanshiqwq.enchantment.Listener.InventoryClickListener.hasAttributeModifiers;
import static cn.yanshiqwq.enchantment.utils.getAttachedCount;
import static cn.yanshiqwq.enchantment.utils.setAttachedCount;
import static cn.yanshiqwq.enchantment.Listener.InventoryClickListener.modifiersEraser;
import static cn.yanshiqwq.enchantment.Listener.InventoryClickListener.modifiersAdder;
import static cn.yanshiqwq.enchantment.Listener.InventoryClickListener.enchantmentEraser;
import static cn.yanshiqwq.enchantment.Listener.InventoryClickListener.isEnchantableItem;
import static cn.yanshiqwq.enchantment.Listener.InventoryClickListener.isEnchantedBook;

public class AnvilListener implements Listener {
    // 监听使用铁砧事件
    @EventHandler
    public void onAnvil(PrepareAnvilEvent event){
        AnvilInventory inventory = event.getInventory();
        ItemStack firstItem = inventory.getFirstItem();
        ItemStack secondItem = inventory.getSecondItem();

        if (firstItem == null || secondItem == null){
            return;
        }

        if (secondItem.getType() != InventoryClickListener.modifiersAdder || hasAttributeModifiers(firstItem)) {
            return;
        }

        // wtf is this
        // 能跑就别动这段了（
        Random random = new Random(firstItem.hashCode());
        ItemStack result = null;
        if (secondItem.getType() == modifiersEraser) {
            if (hasAttributeModifiers(firstItem)){
                return;
            }
            if (isEnchantedBook(firstItem) || isEnchantableItem(firstItem)) {
                result = eraseModifier(inventory);
            }
        } else if (secondItem.getType() == enchantmentEraser) {
            if (hasAttributeModifiers(firstItem)){
                return;
            }
            if (firstItem.getType() != Material.BOOK && !isEnchantableItem(firstItem)){
                return;
            }
            result = eraseEnchantment(inventory);
        } else if (secondItem.getType() == modifiersAdder) {
            if (hasAttributeModifiers(firstItem)){
                return;
            }
            if (firstItem.getType() != Material.BOOK && !isEnchantableItem(firstItem)){
                return;
            }

            ItemStack fakeItem = firstItem.clone();
            if (fakeItem.getType() == Material.BOOK){
                fakeItem.setType(Material.ENCHANTED_BOOK);
            }
            fakeItem.setAmount(1);
            ItemMeta fakeItemMeta = fakeItem.getItemMeta();
            fakeItemMeta.lore(new ArrayList<>(List.of(Component.text("* 随机附加属性", TextColor.color(Color.GRAY.asRGB())).decoration(TextDecoration.ITALIC, false))));
            fakeItem.setItemMeta(fakeItemMeta);
            inventory.setRepairCost((int) (Math.pow(2, random.nextInt(1, 4)) - 1));
            inventory.setRepairCostAmount(1);
            result = fakeItem;
        } else {
            if ((isEnchantedBook(firstItem) || isEnchantableItem(firstItem)) && isEnchantedBook(secondItem) && hasAttributeModifiers(secondItem)
                    || isEnchantableItem(firstItem) && isEnchantableItem(secondItem) && hasAttributeModifiers(secondItem) && firstItem.getType() == secondItem.getType()) {
                result = mergeModifier(inventory);
            }
        }

        event.setResult(result);
    }
    private static ItemStack eraseModifier(AnvilInventory inventory){
        assert inventory.getFirstItem() != null;
        ItemStack item = inventory.getFirstItem().clone();
        inventory.setRepairCost(1);
        inventory.setRepairCostAmount(1);
        Bukkit.getLogger().info(String.format("eraseModifier, item = %s", item.getType().name()));

        ItemMeta resultMeta = item.getItemMeta().clone();
        Multimap<Attribute, AttributeModifier> resultModifiers = HashMultimap.create(Objects.requireNonNull(resultMeta.getAttributeModifiers()));
        List<Map.Entry<Attribute, AttributeModifier>> toRemove = new ArrayList<>();
        resultModifiers.entries().stream()
                .filter(entry -> entry.getValue().getAmount() >= 0)
                .forEach(toRemove::add);
        toRemove.forEach(entry -> resultModifiers.remove(entry.getKey(), entry.getValue()));
        resultMeta.setAttributeModifiers(resultModifiers);
        item.setItemMeta(resultMeta);
        return item;
    }

    private static ItemStack eraseEnchantment(AnvilInventory inventory) {
        assert inventory.getFirstItem() != null;
        ItemStack item = inventory.getFirstItem().clone();
        inventory.setRepairCost(3);
        inventory.setRepairCostAmount(1);
        Bukkit.getLogger().info(String.format("eraseEnchantment, item = %s", item.getType().name()));

        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.getEnchants().entrySet().stream()
                .filter(entry -> !entry.getKey().isCursed())
                .forEach(entry -> item.removeEnchantment(entry.getKey()));
        item.setItemMeta(itemMeta);
        return item;
    }


    private static ItemStack mergeModifier(AnvilInventory inventory){
        ItemStack firstItem = inventory.getFirstItem();
        ItemStack secondItem = inventory.getSecondItem();
        ItemStack resultItem = inventory.getResult();
        assert firstItem != null;
        assert secondItem != null;
        Bukkit.getLogger().info(String.format("mergeModifier, item = [%s, %s]", firstItem.getType().name(), secondItem.getType().name()));

        if (resultItem == null) {
            if (
                    (secondItem.getType() != Material.ENCHANTED_BOOK || firstItem.getItemMeta().getAttributeModifiers() == null) &&
                            (firstItem.getType() != secondItem.getType() || secondItem.getItemMeta().getAttributeModifiers() == null))
            { return null; }
            resultItem = firstItem.clone();
        }

        int finalAttachedCount = getAttachedCount(firstItem.getItemMeta()) + getAttachedCount(secondItem.getItemMeta());
        if (finalAttachedCount > 8) { return null; }

        setAttachedCount(firstItem.getItemMeta(), finalAttachedCount);
        inventory.setRepairCost((int) (Math.floor(Math.pow(finalAttachedCount, 2)) * 1.5 + inventory.getRepairCost()));
        inventory.setRepairCostAmount(finalAttachedCount);
        inventory.setMaximumRepairCost(1024);

        Multimap<Attribute, AttributeModifier> mergedModifiers = ArrayListMultimap.create();
        Multimap<Attribute, AttributeModifier> firstModifiers = firstItem.getItemMeta().getAttributeModifiers();
        if (firstModifiers == null) {
            firstModifiers = ArrayListMultimap.create();
        }
        Multimap<Attribute, AttributeModifier> secondModifiers = secondItem.getItemMeta().getAttributeModifiers();
        if (secondModifiers == null) {
            secondModifiers = ArrayListMultimap.create();
        }
        mergedModifiers.putAll(firstModifiers);
        mergedModifiers.putAll(secondModifiers);

        ItemMeta resultMeta = resultItem.getItemMeta();
        resultMeta.setAttributeModifiers(mergedModifiers);
        for (Map.Entry<Enchantment, Integer> entry : secondItem.getItemMeta().getEnchants().entrySet()) {
            resultMeta.addEnchant(entry.getKey(), entry.getValue(), true);
        }
        resultItem.setItemMeta(resultMeta);
        return resultItem;
    }
}
