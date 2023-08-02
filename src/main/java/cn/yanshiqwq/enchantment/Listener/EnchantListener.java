package cn.yanshiqwq.enchantment.Listener;

import cn.yanshiqwq.enchantment.Main;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
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
    private final Map<Player, Integer> enchantCountMap = new HashMap<>();
    private final ArrayList<Enchantment> treasureEnchants = new ArrayList<>(List.of(
            Enchantment.FROST_WALKER, Enchantment.SOUL_SPEED, Enchantment.MENDING, Enchantment.BINDING_CURSE, Enchantment.VANISHING_CURSE
    ));
    private final ArrayList<AttributeFactor> attachableAttributes = new ArrayList<>(List.of(
            new AttributeFactor(Attribute.GENERIC_ARMOR, AttributeModifier.Operation.ADD_NUMBER, new double[]{0.84, 1.26, 1.42, 1.58}),
            new AttributeFactor(Attribute.GENERIC_ARMOR_TOUGHNESS, AttributeModifier.Operation.ADD_NUMBER, new double[]{0.12, 0.16, 0.24, 0.28}),
            new AttributeFactor(Attribute.GENERIC_ATTACK_DAMAGE, AttributeModifier.Operation.ADD_NUMBER, new double[]{0.52, 0.64, 0.75, 0.84}),
            new AttributeFactor(Attribute.GENERIC_ATTACK_DAMAGE, AttributeModifier.Operation.ADD_SCALAR, new double[]{0.052, 0.084, 0.124, 0.148}),
            new AttributeFactor(Attribute.GENERIC_LUCK, AttributeModifier.Operation.ADD_NUMBER, new double[]{0.24, 0.48, 0.72, 0.96}),
            new AttributeFactor(Attribute.GENERIC_MAX_HEALTH, AttributeModifier.Operation.ADD_NUMBER, new double[]{0.82, 0.85, 1.08, 1.26}),
            new AttributeFactor(Attribute.GENERIC_MAX_HEALTH, AttributeModifier.Operation.ADD_SCALAR, new double[]{0.052, 0.056, 0.064, 0.072}),
            new AttributeFactor(Attribute.GENERIC_KNOCKBACK_RESISTANCE, AttributeModifier.Operation.ADD_NUMBER, new double[]{0.052, 0.056, 0.064, 0.075}),
            new AttributeFactor(Attribute.GENERIC_MOVEMENT_SPEED, AttributeModifier.Operation.ADD_SCALAR, new double[]{0.016, 0.018, 0.024, 0.028}),
            new AttributeFactor(Attribute.GENERIC_ATTACK_SPEED, AttributeModifier.Operation.ADD_SCALAR, new double[]{0.012, 0.014, 0.016, 0.018})
    ));

    @EventHandler
    public void onEnchant(EnchantItemEvent event) {
        Player player = event.getEnchanter();
        int enchantCount = enchantCountMap.getOrDefault(player, 0);
        enchantCount++;
        enchantCountMap.put(player, enchantCount);

        if (Math.random() <= 0.1 || enchantCount >= 40) {
            if (Math.random() <= 0.12 || enchantCount >= 40) {
                addRandomTreasureEnchant(event.getItem());
                enchantCountMap.put(player, 0);
            } else {
                addRandomNormalEnchant(event.getItem());
            }
        }
    }

    private void addRandomNormalEnchant(ItemStack item) {
        for (int i = 0; i < 16; i++) {
            Random random = new Random();
            Enchantment ench = Enchantment.values()[random.nextInt(Enchantment.values().length)];
            if (ench.canEnchantItem(item) && !treasureEnchants.contains(ench)) {
                item.addEnchantment(ench, random.nextInt(ench.getStartLevel(), ench.getMaxLevel()));
                break;
            }
        }
    }

    private void addRandomTreasureEnchant(ItemStack item) {
        Random random = new Random();
        for (int i = 0; i < 16; i++) {
            Enchantment ench = treasureEnchants.get(random.nextInt(Enchantment.values().length));
            if (ench.canEnchantItem(item)) {
                item.addEnchantment(ench, random.nextInt(ench.getStartLevel(), ench.getMaxLevel()));
                break;
            }
        }

        NamespacedKey key = new NamespacedKey(Main.getInstance(), "attribute_attached_count");
        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        int attachedCount = container.getOrDefault(key, PersistentDataType.INTEGER, 0);
        addRandomAdditionValue(item, attachedCount <= 4);
        container.set(key, PersistentDataType.INTEGER, attachedCount + 1);
    }

    private void addRandomAdditionValue(ItemStack item, boolean addNew) {
        AttributeFactor factor = null;
        Random random = new Random();
        ArrayList<Attribute> itemAttributes = new ArrayList<>(Objects.requireNonNull(item.getItemMeta().getAttributeModifiers()).keys());
        AttributeFactor attributeFactor;
        for (int i = 0; i < 16; i++) {
            attributeFactor = attachableAttributes.get(random.nextInt(attachableAttributes.size()));
            if (addNew ^ itemAttributes.contains(attributeFactor.getAttribute())) {
                factor = attributeFactor;
                break;
            }
            if (i == 15) {
                return;
            }
        }
        if (factor == null) {
            return;
        }

        for (AttributeModifier modifier : Objects.requireNonNull(item.getItemMeta().getAttributeModifiers()).get(
                itemAttributes.get(random.nextInt(itemAttributes.size()))
        )) {
            Attribute attribute = factor.getAttribute();
            if (modifier.getOperation().equals(factor.getOperation())) {
                item.getItemMeta().removeAttributeModifier(attribute, modifier);
                Objects.requireNonNull(item.getItemMeta().getAttributeModifiers()).put(
                        attribute, new AttributeModifier(attribute.toString(), modifier.getAmount() + factor.getRandomAdditionValue(), factor.getOperation())
                );
            }
        }
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

        public double getRandomAdditionValue() {
            Random random = new Random();
            return additionValue[random.nextInt(this.additionValue.length)];
        }
    }
}