package cn.yanshiqwq.enchantment.Listener;

import cn.yanshiqwq.enchantment.Main;
import com.google.common.collect.Multimap;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

import static cn.yanshiqwq.enchantment.utils.getAttachedCount;
import static cn.yanshiqwq.enchantment.utils.setAttachedCount;

public class InventoryClickListener implements Listener {
    public static final Material modifiersAdder = Material.ECHO_SHARD;
    public static final Material modifiersEraser = Material.GLOW_INK_SAC;
    public static final Material enchantmentEraser = Material.AMETHYST_SHARD;
    public static final ArrayList<Material> enchantableItem = new ArrayList<> (List.of(
            Material.TURTLE_HELMET, Material.LEATHER_HELMET, Material.GOLDEN_HELMET, Material.CHAINMAIL_HELMET, Material.IRON_HELMET, Material.DIAMOND_HELMET, Material.NETHERITE_HELMET,
            Material.LEATHER_CHESTPLATE, Material.GOLDEN_CHESTPLATE, Material.CHAINMAIL_CHESTPLATE, Material.IRON_CHESTPLATE, Material.DIAMOND_CHESTPLATE, Material.NETHERITE_CHESTPLATE,
            Material.LEATHER_LEGGINGS, Material.GOLDEN_LEGGINGS, Material.CHAINMAIL_LEGGINGS, Material.IRON_LEGGINGS, Material.DIAMOND_LEGGINGS, Material.NETHERITE_LEGGINGS,
            Material.LEATHER_BOOTS, Material.GOLDEN_BOOTS, Material.CHAINMAIL_BOOTS, Material.IRON_BOOTS, Material.DIAMOND_BOOTS, Material.NETHERITE_BOOTS,
            Material.FLINT_AND_STEEL, Material.SHIELD, Material.ELYTRA, Material.TRIDENT, Material.FISHING_ROD, Material.SHEARS
    )){{
        addAll(Tag.ITEMS_TOOLS.getValues());
    }};

    public static final ArrayList<AttributeFactor> attachableAttributes = new ArrayList<>(List.of(
            new AttributeFactor(Attribute.GENERIC_ARMOR, AttributeModifier.Operation.ADD_NUMBER, new double[]{0.25, 0.5, 0.75, 1}),
            new AttributeFactor(Attribute.GENERIC_ARMOR_TOUGHNESS, AttributeModifier.Operation.ADD_NUMBER, new double[]{0.5, 0.75, 1, 1.25}),
            new AttributeFactor(Attribute.GENERIC_ATTACK_DAMAGE, AttributeModifier.Operation.ADD_NUMBER, new double[]{0.25, 0.3, 0.35, 0.4}),
            new AttributeFactor(Attribute.GENERIC_ATTACK_DAMAGE, AttributeModifier.Operation.ADD_SCALAR, new double[]{0.2, 0.25, 0.3, 0.4}),
            new AttributeFactor(Attribute.GENERIC_LUCK, AttributeModifier.Operation.ADD_NUMBER, new double[]{0.2, 0.4, 0.5, 0.6}),
            new AttributeFactor(Attribute.GENERIC_MAX_HEALTH, AttributeModifier.Operation.ADD_NUMBER, new double[]{0.8, 1.0, 1.5, 2.0}),
            new AttributeFactor(Attribute.GENERIC_MAX_HEALTH, AttributeModifier.Operation.ADD_SCALAR, new double[]{0.04, 0.05, 0.08, 0.1}),
            new AttributeFactor(Attribute.GENERIC_KNOCKBACK_RESISTANCE, AttributeModifier.Operation.ADD_NUMBER, new double[]{0.075, 0.085, 0.095, 0.1}),
            new AttributeFactor(Attribute.GENERIC_KNOCKBACK_RESISTANCE, AttributeModifier.Operation.ADD_SCALAR, new double[]{0.5, 0.75, 1.0, 1.25}),
            new AttributeFactor(Attribute.GENERIC_MOVEMENT_SPEED, AttributeModifier.Operation.ADD_SCALAR, new double[]{0.016, 0.018, 0.024, 0.028}),
            new AttributeFactor(Attribute.GENERIC_ATTACK_SPEED, AttributeModifier.Operation.ADD_SCALAR, new double[]{0.085, 0.1, 0.12, 0.15}),
            new AttributeFactor(Attribute.GENERIC_ATTACK_KNOCKBACK, AttributeModifier.Operation.ADD_SCALAR, new double[]{0.20, 0.25, 0.35, 0.5})
    ));

    public static EquipmentSlot matchItemSlot(ItemStack item, Random random) {
        Material type = item.getType();
        if (type == Material.ENCHANTED_BOOK) {
            Multimap<Attribute, AttributeModifier> attributeModifiers = item.getItemMeta().getAttributeModifiers();
            if (attributeModifiers != null){
                return getMostFrequentSlot(attributeModifiers);
            }
            return EquipmentSlot.values()[random.nextInt(EquipmentSlot.values().length)];
        }
        EquipmentSlot slot;
        if (type.name().endsWith("_HELMET") || type == Material.TURTLE_HELMET) {
            slot = EquipmentSlot.HEAD;
        } else if (type.name().endsWith("_CHESTPLATE")) {
            slot = EquipmentSlot.CHEST;
        } else if (type.name().endsWith("_LEGGINGS")) {
            slot = EquipmentSlot.LEGS;
        } else if (type.name().endsWith("_BOOTS")) {
            slot = EquipmentSlot.FEET;
        } else if (type == Material.SHIELD) {
            slot = EquipmentSlot.OFF_HAND;
            if (random.nextDouble() <= 0.05) {
                slot = EquipmentSlot.HAND;
            }
        } else if (type.name().endsWith("_SWORD") || type.name().endsWith("_AXE") || type.name().endsWith("_SHOVEL") || type.name().endsWith("_HOE") || type.name().endsWith("_PICKAXE")) {
            slot = EquipmentSlot.HAND;
        } else {
            slot = EquipmentSlot.HAND;
            if (random.nextDouble() <= 0.05) {
                slot = EquipmentSlot.OFF_HAND;
            }
        }
        return slot;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event){
        if (!(event.getClickedInventory() instanceof AnvilInventory inventory)){
            return;
        }

        InventoryAction action = event.getAction();
        Bukkit.getLogger().info(String.format("actionType = %s", action.name()));

        ItemStack firstItem = inventory.getFirstItem();
        ItemStack secondItem = inventory.getSecondItem();
        if (firstItem == null || secondItem == null){
            return;
        }

        Bukkit.getLogger().info(String.format("InventoryClickEvent, items = [%s]", Arrays.toString(inventory.getContents())));
        Bukkit.getLogger().info(String.format("slot = %s", event.getSlot()));

        if (event.getSlot() != 2){
            return;
        }
        if (!isEnchantableItem(firstItem) || hasAttributeModifiers(firstItem)){
            return;
        }
        if (secondItem.getType() != modifiersAdder) {
            return;
        }

        ItemStack result = addModifiers(inventory);
        result.setAmount(1);
        Bukkit.getLogger().info(String.format("resultMeta = %s", result.getItemMeta().toString()));
        if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            Inventory playerInventory = event.getWhoClicked().getInventory();
            if (isInventoryFull(playerInventory)) {
                event.setCancelled(true);
                return;
            }
            playerInventory.addItem(result);
        } else {
            event.setCursor(result);
        }
        if (!event.isCancelled()) {
            firstItem.setAmount(firstItem.getAmount() - 1);
            secondItem.setAmount(secondItem.getAmount() - 1);
            Objects.requireNonNull(inventory.getResult()).setAmount(inventory.getResult().getAmount() - 1);
        }



        // DEBUG
//        try {
//            ItemStack item1 = event.getCurrentItem();
//            assert item1 != null;
//            ItemStack item2 = event.getView().getItem(2);
//            assert item2 != null;
//            ItemStack item3 = event.getClickedInventory().getItem(2);
//            assert item3 != null;
//            Bukkit.getLogger().info(String.format("[DEBUG] [ICL] event.getCurrentItem(), type = %s, meta = %s", item1.getType(), item1.getItemMeta().getAsString()));
//            Bukkit.getLogger().info(String.format("[DEBUG] [ICL] event.getView().getItem(2), type = %s, meta = %s", item2.getType(), item2.getItemMeta().getAsString()));
//            Bukkit.getLogger().info(String.format("[DEBUG] [ICL] event.getClickedInventory().getItem(2), type = %s, meta = %s", item2.getType(), item2.getItemMeta().getAsString()));
//        } catch (NullPointerException ignored) {}
    }

    private static ItemStack addModifiers(AnvilInventory inventory) {
        ItemStack item = inventory.getFirstItem();
        assert item != null;
        Random random = new Random();
        Bukkit.getLogger().info(String.format("addModifiers, item = %s", item.getType().name()));

        ItemStack returnItem = item.clone();
        if (item.getType() == Material.BOOK){
            returnItem.setType(Material.ENCHANTED_BOOK);
        }

        if (isEnchantedBook(returnItem)) {
            returnItem = addRandomAdditionValue(returnItem, random, 0.65);
        } else {
            returnItem = addRandomAdditionValue(returnItem, random, 1.0);
            returnItem = addRandomAdditionValue(returnItem, random, 1.0);
        }
        return returnItem;
    }

    public static EquipmentSlot getMostFrequentSlot(Multimap<Attribute, AttributeModifier> attributeModifiers) {
        // 统计每个slot的出现次数
        Map<EquipmentSlot, Integer> slotCounts = new HashMap<>();

        for (AttributeModifier attributeModifier : attributeModifiers.values()) {
            EquipmentSlot slot = attributeModifier.getSlot();
            slotCounts.put(slot, slotCounts.getOrDefault(slot, 0) + 1);
        }

        // 找到出现次数最多的slot
        EquipmentSlot mostFrequentSlot = null;
        int maxCount = 0;

        for (Map.Entry<EquipmentSlot, Integer> entry : slotCounts.entrySet()) {
            if (entry.getValue() > maxCount) {
                mostFrequentSlot = entry.getKey();
                maxCount = entry.getValue();
            }
        }

        return mostFrequentSlot;
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

    public static AttributeModifier getMatchingAttributeModifier(Random random, List<Attribute> attributes, Multimap<Attribute, AttributeModifier> modifiers, AttributeModifier.Operation operation) {
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

    public static ItemStack addRandomAdditionValue(ItemStack item, Random random, double correction) {
        ItemStack returnItem = item.clone();
        Bukkit.getLogger().info(String.format("addRandomAdditionValue, item = %s", returnItem.getType().name()));

        ItemMeta itemMeta = returnItem.getItemMeta();
        int attachedCount = getAttachedCount(itemMeta);
        int maxAttachedCount;
        if (returnItem.getType() == Material.ENCHANTED_BOOK) {
            maxAttachedCount = 2;
        } else {
            maxAttachedCount = 4;
        }
        if (attachedCount >= maxAttachedCount) {
            Bukkit.getLogger().info(String.format("attachedCount >= %s (%s), item = %s", maxAttachedCount, attachedCount, returnItem.getType().name()));
            return returnItem;
        }
        Bukkit.getLogger().info(String.format("attachedCount < %s (%s), item = %s", maxAttachedCount, attachedCount, returnItem.getType().name()));
        setAttachedCount(itemMeta, attachedCount + 1);

        Multimap<Attribute, AttributeModifier> itemModifiers = itemMeta.getAttributeModifiers();
        ArrayList<Attribute> itemAttributes;
        if (itemModifiers == null) {
            itemAttributes = new ArrayList<>();
        } else {
            itemAttributes = new ArrayList<>(itemModifiers.keys());
        }

        AttributeFactor factor = getRandomAttributeFactor(true, itemAttributes, random);
        if (factor != null) {
            addAdditionValue(returnItem, random, factor.attribute(),
                    new AttributeModifier(UUID.randomUUID(), "Weapon modifier", factor.getRandomAdditionValue(random, correction), factor.operation(), matchItemSlot(returnItem, random))
            );
        }
        return returnItem;
    }

    public static AttributeFactor getRandomAttributeFactor(boolean addNewAttribute, ArrayList<Attribute> itemAttributes, Random random) {
        List<AttributeFactor> attributeFactorList = new ArrayList<>(attachableAttributes);

        if (addNewAttribute) {
            attributeFactorList.removeIf(attributeFactor -> itemAttributes.contains(attributeFactor.attribute()));
        } else {
            attributeFactorList.removeIf(attributeFactor -> !itemAttributes.contains(attributeFactor.attribute()));
        }

        return attributeFactorList.get(random.nextInt(attributeFactorList.size()));
    }

    public record AttributeFactor(Attribute attribute, AttributeModifier.Operation operation, double[] additionValue) {
        // 获取随机的附加值
        public double getRandomAdditionValue(Random random, double correction) {
            double base = additionValue[random.nextInt(this.additionValue.length)];
            if (random.nextDouble() <= 0.025) {
                Bukkit.getLogger().info("doubleAdditionValue");
                base *= 2;
            }
            if (random.nextDouble() <= 0.05) {
                Bukkit.getLogger().info("negativeAdditionValue");
                base *= -0.75;
            }
            return base * correction;
        }
    }

    public static boolean isEnchantableItem(ItemStack item) {
        return enchantableItem.contains(item.getType());
    }

    public static boolean isEnchantedBook(ItemStack item) {
        return item.getType() == Material.ENCHANTED_BOOK;
    }

    public static boolean hasAttributeModifiers(ItemStack item) {
        if (item.getItemMeta() == null || item.getItemMeta().getAttributeModifiers() == null){
            return false;
        }
        return !item.getItemMeta().getAttributeModifiers().isEmpty();
    }

    public static boolean isInventoryFull(Inventory inventory){
        return inventory.firstEmpty() == -1;
    }
}
