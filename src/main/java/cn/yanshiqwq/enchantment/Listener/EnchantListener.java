package cn.yanshiqwq.enchantment.Listener;

import cn.yanshiqwq.enchantment.Main;
import com.google.common.collect.Multimap;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class EnchantListener implements Listener {
    // 存储玩家已附魔的次数的映射表
    private final Map<Player, Integer> enchantCountMap = new HashMap<>();
    // 宝藏附魔列表
    private final ArrayList<Enchantment> treasureEnchants = new ArrayList<>(List.of(
            Enchantment.FROST_WALKER, Enchantment.SWIFT_SNEAK, Enchantment.SOUL_SPEED, Enchantment.MENDING, Enchantment.BINDING_CURSE, Enchantment.VANISHING_CURSE
    ));
    // 可附加属性列表
    private final ArrayList<AttributeFactor> attachableAttributes = new ArrayList<>(List.of(
            new AttributeFactor(Attribute.GENERIC_ARMOR, AttributeModifier.Operation.ADD_NUMBER, new double[]{0.25, 0.5, 0.75, 1}),
            new AttributeFactor(Attribute.GENERIC_ARMOR_TOUGHNESS, AttributeModifier.Operation.ADD_NUMBER, new double[]{2, 2.5, 3, 4}),
            new AttributeFactor(Attribute.GENERIC_ATTACK_DAMAGE, AttributeModifier.Operation.ADD_NUMBER, new double[]{0.25, 0.3, 0.35, 0.4}),
            new AttributeFactor(Attribute.GENERIC_ATTACK_DAMAGE, AttributeModifier.Operation.ADD_SCALAR, new double[]{0.2, 0.25, 0.3, 0.4}),
            new AttributeFactor(Attribute.GENERIC_LUCK, AttributeModifier.Operation.ADD_NUMBER, new double[]{0.2, 0.4, 0.5, 0.6}),
            new AttributeFactor(Attribute.GENERIC_MAX_HEALTH, AttributeModifier.Operation.ADD_NUMBER, new double[]{0.8, 1.0, 1.5, 2.0}),
            new AttributeFactor(Attribute.GENERIC_MAX_HEALTH, AttributeModifier.Operation.ADD_SCALAR, new double[]{0.04, 0.05, 0.08, 0.1}),
            new AttributeFactor(Attribute.GENERIC_KNOCKBACK_RESISTANCE, AttributeModifier.Operation.ADD_NUMBER, new double[]{0.075, 0.08, 0.09, 0.1}),
            new AttributeFactor(Attribute.GENERIC_MOVEMENT_SPEED, AttributeModifier.Operation.ADD_SCALAR, new double[]{0.016, 0.018, 0.024, 0.028}),
            new AttributeFactor(Attribute.GENERIC_ATTACK_SPEED, AttributeModifier.Operation.ADD_SCALAR, new double[]{0.85, 0.1, 0.12, 0.15})
    ));

    // 监听附魔事件
    @EventHandler
    public void onEnchant(EnchantItemEvent event) {
        Player player = event.getEnchanter();
        // 获取玩家已附魔的次数
        int enchantCount = enchantCountMap.getOrDefault(player, 0);
        enchantCount++;
        // 更新玩家已附魔的次数
        enchantCountMap.put(player, enchantCount);

        // 有一定的概率附加宝藏附魔，否则附加普通附魔
        Random random = new Random();
        if (random.nextDouble() <= 0.1 || enchantCount >= 40) {
            if (random.nextDouble() <= 0.12 || enchantCount >= 40) {
                addRandomTreasureEnchantment(event.getItem(), random);
                // 重置玩家已附魔的次数
                enchantCountMap.put(player, 0);
            } else {
                addRandomNormalEnchantment(event.getItem(), random);
            }
        }
    }

    // 附加随机普通附魔
    private void addRandomNormalEnchantment(ItemStack item, Random random) {
        for (int i = 0; i < 16; i++) {
            Enchantment enchantment = Enchantment.values()[random.nextInt(Enchantment.values().length)];
            // 判断附魔是否可用，并且不是宝藏附魔
            if (enchantment.canEnchantItem(item) && !treasureEnchants.contains(enchantment)) {
                // 随机附魔的等级
                item.addEnchantment(enchantment, random.nextInt(enchantment.getStartLevel(), enchantment.getMaxLevel()));
                break;
            }
        }
    }

    // 附加随机宝藏附魔
    private void addRandomTreasureEnchantment(ItemStack item, Random random) {
        for (int i = 0; i < 16; i++) {
            Enchantment enchantment = treasureEnchants.get(random.nextInt(Enchantment.values().length));
            // 判断附魔是否可用
            if (enchantment.canEnchantItem(item)) {
                // 随机附魔的等级
                item.addEnchantment(enchantment, random.nextInt(enchantment.getStartLevel(), enchantment.getMaxLevel()));
                break;
            }
        }
        if(item.getType() != Material.ENCHANTED_BOOK) {
            addRandomAdditionValue(item, random);
            addRandomAdditionValue(item, random);
        }
    }

    // 为物品添加随机的附加值
    private void addRandomAdditionValue(ItemStack item, Random random) {
        ItemMeta itemMeta = item.getItemMeta();
        Multimap<Attribute, AttributeModifier> itemModifiers = itemMeta.getAttributeModifiers();

        NamespacedKey key = new NamespacedKey(Main.getInstance(), "attribute_attached_count");
        PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        int attachedCount = container.getOrDefault(key, PersistentDataType.INTEGER, 0);
        boolean addNewAttribute = attachedCount <= 4;
        container.set(key, PersistentDataType.INTEGER, attachedCount + 1);

        ArrayList<Attribute> itemAttributes;
        if (itemModifiers != null) {
            itemAttributes = new ArrayList<>(itemModifiers.keys());
        } else {
            addNewAttribute = true;
            itemAttributes = new ArrayList<>();
        }

        AttributeFactor factor = getRandomAttributeFactor(addNewAttribute, itemAttributes, random);
        if (factor != null) {
            addAdditionValue(item, random, factor.getAttribute(),
                    new AttributeModifier(UUID.randomUUID(), factor.getAttribute().toString(), factor.getRandomAdditionValue(random), factor.getOperation(), matchItemSlot(item.getType(), random))
            );
        }
    }

    private AttributeFactor getRandomAttributeFactor(boolean addNewAttribute, ArrayList<Attribute> itemAttributes, Random random) {
        AttributeFactor factor = null;
        List<AttributeFactor> attachableAttributesCopy = new ArrayList<>(attachableAttributes);

        if (!addNewAttribute) {
            List<AttributeFactor> removableAttributes = new ArrayList<>();
            for (AttributeFactor attributeFactor : attachableAttributesCopy) {
                if (!itemAttributes.contains(attributeFactor.getAttribute())) {
                    removableAttributes.add(attributeFactor);
                }
            }
            attachableAttributesCopy.removeAll(removableAttributes);
        }

        Collections.shuffle(attachableAttributesCopy, random);
        for (AttributeFactor attributeFactor : attachableAttributesCopy) {
            if (!itemAttributes.contains(attributeFactor.getAttribute())) {
                factor = attributeFactor;
                break;
            }
        }

        return factor;
    }


    public static EquipmentSlot matchItemSlot(Material item, Random random){
        if (item == Material.ENCHANTED_BOOK) {
            return EquipmentSlot.values()[random.nextInt(EquipmentSlot.values().length)];
        }

        EquipmentSlot slot;
        if (item.name().endsWith("_HELMET") || item == Material.TURTLE_HELMET) {
            slot = EquipmentSlot.HEAD;
        } else if (item.name().endsWith("_CHESTPLATE")) {
            slot = EquipmentSlot.CHEST;
        } else if (item.name().endsWith("_LEGGINGS")) {
            slot = EquipmentSlot.LEGS;
        } else if (item.name().endsWith("_BOOTS")) {
            slot = EquipmentSlot.FEET;
        } else if (item == Material.SHIELD) {
            slot = EquipmentSlot.OFF_HAND;
            if (random.nextDouble() <= 0.05){
                slot = EquipmentSlot.HAND;
            }
        } else {
            slot = EquipmentSlot.HAND;
            if (random.nextDouble() <= 0.45){
                slot = EquipmentSlot.OFF_HAND;
            }
        }
        return slot;
    }

    public static void addAdditionValue(ItemStack item, Random random, Attribute additionAttribute, AttributeModifier additionModifier) {
        ItemMeta itemMeta = item.getItemMeta();
        Multimap<Attribute, AttributeModifier> itemModifiers = itemMeta.getAttributeModifiers();

        NamespacedKey key = new NamespacedKey(Main.getInstance(), "attribute_attached_count");
        PersistentDataContainer container = itemMeta.getPersistentDataContainer();

        ArrayList<Attribute> itemAttributes = new ArrayList<>();
        if (itemModifiers != null) {
            itemAttributes.addAll(itemModifiers.keys());
        }

        int attachedCount = container.getOrDefault(key, PersistentDataType.INTEGER, 0);
        container.set(key, PersistentDataType.INTEGER, attachedCount + 1);

        AttributeModifier matchingModifier = getMatchingAttributeModifier(random, itemAttributes, itemModifiers, additionModifier.getOperation());
        if (matchingModifier != null) {
            itemMeta.removeAttributeModifier(additionAttribute, matchingModifier);
        }
        itemMeta.addAttributeModifier(additionAttribute, additionModifier);

        item.setItemMeta(itemMeta);
    }

    private static AttributeModifier getMatchingAttributeModifier(Random random, List<Attribute> attributes, Multimap<Attribute, AttributeModifier> modifiers, AttributeModifier.Operation operation) {
        if (modifiers == null || modifiers.isEmpty()) {
            return null;
        }

        Attribute selectedAttribute = attributes.get(random.nextInt(attributes.size()));
        Collection<AttributeModifier> matchingModifiers = modifiers.get(selectedAttribute);

        for (AttributeModifier modifier : matchingModifiers) {
            if (modifier.getOperation().equals(operation)) {
                return modifier;
            }
        }
        return null;
    }


    private static class AttributeFactor {
        private final Attribute attribute;
        private final AttributeModifier.Operation operation;
        private final double[] additionValue;

        private AttributeFactor(Attribute attribute, AttributeModifier.Operation operation, double[] additionValue) {
            this.attribute = attribute;
            this.operation = operation;
            this.additionValue = additionValue;
        }

        public Attribute getAttribute() {
            return this.attribute;
        }

        public AttributeModifier.Operation getOperation() {
            return this.operation;
        }

        // 获取随机的附加值
        public double getRandomAdditionValue(Random random) {
            double base = additionValue[random.nextInt(this.additionValue.length)];
            if(random.nextDouble() <= 0.025){
                base *= 2;
            }
            if(random.nextDouble() <= 0.05){
                base *= -0.75;
            }
            return base;
        }
    }
}
