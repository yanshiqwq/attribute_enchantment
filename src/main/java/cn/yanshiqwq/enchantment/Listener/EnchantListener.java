package cn.yanshiqwq.enchantment.Listener;

import cn.yanshiqwq.enchantment.Main;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class EnchantListener implements Listener {
    // 监听附魔事件
    @EventHandler
    public void onEnchant(EnchantItemEvent event) {
        Player player = event.getEnchanter();

        NamespacedKey key = new NamespacedKey(Main.getInstance(), "enchant_count");
        PersistentDataContainer container = player.getPersistentDataContainer();
        int enchantCount = container.getOrDefault(key, PersistentDataType.INTEGER, 0);
        enchantCount++;
        container.set(key, PersistentDataType.INTEGER, enchantCount);

        int maxEnchantCount = 80;
        Random random = new Random();
        ItemStack item = event.getItem();
        if (random.nextDouble() <= 0.1 || enchantCount >= maxEnchantCount) {
            if (random.nextDouble() <= 0.15 || enchantCount >= maxEnchantCount) {
                addRandomTreasureEnchantment(item, random);
                container.set(key, PersistentDataType.INTEGER, 0);
            } else {
                addRandomNormalEnchantment(item, random);
            }
        }
    }

    // 附加随机普通附魔
    private void addRandomNormalEnchantment(ItemStack item, Random random) {
        for (int i = 0; i < 16; i++) {
            Enchantment enchantment = Enchantment.values()[random.nextInt(Enchantment.values().length)];
            // 判断附魔是否可用，并且不是宝藏附魔
            if (enchantment.canEnchantItem(item) && !enchantment.isTreasure()) {
                // 随机附魔的等级
                item.addEnchantment(enchantment, random.nextInt(enchantment.getStartLevel(), enchantment.getMaxLevel()));
                break;
            }
        }
    }

    // 附加随机宝藏附魔
    private void addRandomTreasureEnchantment(ItemStack item, Random random) {
        Bukkit.getLogger().info("addRandomTreasureEnchantment");
        if (random.nextDouble() <= 0.5) {
            for (int i = 0; i < 16; i++) {
                Enchantment enchantment = Enchantment.values()[random.nextInt(Enchantment.values().length)];
                if (!enchantment.canEnchantItem(item)) {
                    continue;
                }
                if (enchantment.isCursed()) {
                    item.addEnchantment(enchantment, random.nextInt(1, enchantment.getMaxLevel() + 1));
                    break;
                }
            }
        } else {
            for (int i = 0; i < 16; i++) {
                Enchantment enchantment = Enchantment.values()[random.nextInt(Enchantment.values().length)];
                if (!enchantment.canEnchantItem(item)) {
                    continue;
                }
                if (enchantment.isTreasure()) {
                    item.addEnchantment(enchantment, random.nextInt(1, enchantment.getMaxLevel() + 1));
                    break;
                }
            }
        }
    }
}
