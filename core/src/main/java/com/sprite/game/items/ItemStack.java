package com.sprite.game.items;

import com.sprite.data.TranslatableString;
import com.sprite.data.annotations.NotNull;
import com.sprite.data.utils.Utils;
import com.sprite.game.items.component.ComponentMap;
import com.sprite.game.items.component.ComponentType;
import com.sprite.game.items.component.Components;
import com.sprite.game.items.component.ConsumableComponent;
import com.sprite.game.items.component.DurabilityComponent;
import com.sprite.game.items.component.EquippableComponent;
import com.sprite.game.items.component.FoodComponent;
import com.sprite.game.items.component.SpellComponent;
import com.sprite.game.items.component.StatModifiersComponent;
import com.sprite.game.items.component.WeaponComponent;
import com.sprite.game.stats.StatusEffect;
import com.sprite.resource.items.ItemType;
import com.sprite.resource.magic.spells.Spell;
import com.sprite.resource.texture.GameSprite;

import java.util.List;
import java.util.Objects;

/**
 * Represents a physical copy / stack of an ItemType in an inventory, equipment slot, or the world.
 */
public class ItemStack {

    private final ItemType type;
    private int amount;

    private TranslatableString name;
    private TranslatableString description;

    // Per-instance dynamic component overrides (Minecraft-style Data Components)
    private final ComponentMap customComponents = new ComponentMap();

    // Embedded Magic Spell & Durability State
    private Spell embeddedSpell;
    private int durability = 100;
    private int maxDurability = 100;

    public ItemStack(@NotNull ItemType type, int amount) {
        this.type = Objects.requireNonNull(type);
        this.amount = Math.max(0, amount);
        this.name = this.type.name() != null ? this.type.name() : new TranslatableString(this.type.id());
        this.description = this.type.description() != null ? this.type.description() : new TranslatableString("");
        initDurabilityFromComponents();
    }

    public ItemStack(String id, int amount) {
        this(Utils.resources().ITEMS.load(id.startsWith("items:") ? id : "items:" + id), amount);
    }

    public ItemStack(ItemType type) {
        this(type, 1);
    }

    private void initDurabilityFromComponents() {
        DurabilityComponent dur = get(Components.DURABILITY);
        if (dur != null) {
            this.maxDurability = dur.maxDurability();
            this.durability = dur.durability();
        }
        SpellComponent sc = get(Components.SPELL);
        if (sc != null && sc.spell() != null) {
            this.embeddedSpell = sc.spell();
        }
    }

    // --- Dynamic Component Accessors ---

    public <T> T get(ComponentType<T> compType) {
        if (compType == null) return null;
        if (customComponents.has(compType)) {
            return customComponents.get(compType);
        }
        if (type != null && type.has(compType)) {
            return type.get(compType);
        }
        return null;
    }

    public <T> T getOrDefault(ComponentType<T> compType, T defaultValue) {
        T val = get(compType);
        return val != null ? val : defaultValue;
    }

    public <T> boolean has(ComponentType<T> compType) {
        if (compType == null) return false;
        return customComponents.has(compType)
            || (type != null && type.has(compType));
    }

    @SuppressWarnings("unchecked")
    public <T> ItemStack set(ComponentType<T> compType, T value) {
        customComponents.set(compType, value);
        if (compType == Components.DURABILITY && value instanceof DurabilityComponent dur) {
            this.durability = dur.durability();
            this.maxDurability = dur.maxDurability();
        } else if (compType == Components.SPELL && value instanceof SpellComponent sc) {
            this.embeddedSpell = sc.spell();
        }
        return this;
    }

    public <T> T remove(ComponentType<T> compType) {
        return customComponents.remove(compType);
    }

    public boolean hasTag(String tag) {
        if (tag == null) return false;
        if (customComponents.hasTag(tag)) return true;
        if (type != null && type.hasTag(tag)) return true;
        List<String> tags = get(Components.TAGS);
        if (tags != null) {
            for (String t : tags) {
                if (tag.equalsIgnoreCase(t)) return true;
            }
        }
        return false;
    }

    public ComponentMap customComponents() {
        return customComponents;
    }

    // --- Standard Item Properties ---

    public String id() {
        if (type != null) return type.id();
        return "unknown";
    }

    public TranslatableString name() {
        return name != null ? name : (type != null ? type.name() : new TranslatableString(id()));
    }

    public void name(TranslatableString name) {
        this.name = name;
    }

    public TranslatableString description() {
        return description != null ? description : (type != null ? type.description() : new TranslatableString(""));
    }

    public void description(TranslatableString description) {
        this.description = description;
    }

    public ItemRarity rarity() {
        if (type != null) return type.rarity();
        return ItemRarity.COMMON;
    }

    public ItemSlot slot() {
        EquippableComponent eq = get(Components.EQUIPPABLE);
        if (eq != null) return eq.slot();
        if (type != null) return type.slot();
        return ItemSlot.MATERIAL;
    }

    public int value() {
        int baseValue = type != null ? type.value() : 10;
        return baseValue * amount;
    }

    public int amount() {
        return amount;
    }

    public void amount(int amount) {
        this.amount = Math.max(0, amount);
    }

    public int count() {
        return amount;
    }

    public void count(int count) {
        amount(count);
    }

    public void setAmount(int amount) {
        amount(amount);
    }

    public void addAmount(int extra) {
        this.amount = Math.max(0, this.amount + extra);
    }

    public void add(int extra) {
        addAmount(extra);
    }

    public boolean consume(int count) {
        if (amount >= count) {
            amount -= count;
            return true;
        }
        return false;
    }

    public boolean consumeOne() {
        return consume(1);
    }

    public ItemType type() {
        return type;
    }

    public GameSprite texture() {
        if (type != null) return type.texture();
        return null;
    }

    public String textureLocation() {
        if (type != null) return type.textureLocation();
        return "textures:missing";
    }

    // --- Stat Bonuses ---

    public float bonusHealth() {
        StatModifiersComponent stats = get(Components.STATS);
        return stats != null ? stats.health() : 0f;
    }

    public float bonusMana() {
        StatModifiersComponent stats = get(Components.STATS);
        return stats != null ? stats.mana() : 0f;
    }

    public float bonusAttack() {
        StatModifiersComponent stats = get(Components.STATS);
        return stats != null ? stats.attack() : 0f;
    }

    public float bonusMagic() {
        StatModifiersComponent stats = get(Components.STATS);
        return stats != null ? stats.magic() : 0f;
    }

    public float bonusDefense() {
        StatModifiersComponent stats = get(Components.STATS);
        return stats != null ? stats.defense() : 0f;
    }

    public float bonusCrit() {
        StatModifiersComponent stats = get(Components.STATS);
        return stats != null ? stats.crit() : 0f;
    }

    public float bonusSpeed() {
        StatModifiersComponent stats = get(Components.STATS);
        return stats != null ? stats.speed() : 0f;
    }

    // --- Consumable Stats ---

    public float healAmount() {
        ConsumableComponent cons = get(Components.CONSUMABLE);
        if (cons != null) return cons.healAmount();
        FoodComponent food = get(Components.FOOD);
        if (food != null) return food.nutrition();
        return 0f;
    }

    public float manaAmount() {
        ConsumableComponent cons = get(Components.CONSUMABLE);
        return cons != null ? cons.manaAmount() : 0f;
    }

    public StatusEffect applyEffect() {
        ConsumableComponent cons = get(Components.CONSUMABLE);
        if (cons != null && cons.applyEffect() != null) return cons.applyEffect();
        FoodComponent food = get(Components.FOOD);
        if (food != null && !food.effects().isEmpty()) return food.effects().get(0);
        return null;
    }

    // --- Magic Spell & Durability ---

    public Spell embeddedSpell() {
        if (embeddedSpell != null) return embeddedSpell;
        SpellComponent sc = get(Components.SPELL);
        return sc != null ? sc.spell() : null;
    }

    public void embeddedSpell(Spell embeddedSpell) {
        this.embeddedSpell = embeddedSpell;
        if (embeddedSpell != null) {
            set(Components.SPELL, new SpellComponent(embeddedSpell));
        } else {
            remove(Components.SPELL);
        }
    }

    public boolean hasEmbeddedSpell() {
        return embeddedSpell() != null;
    }

    public int durability() {
        return durability;
    }

    public void durability(int durability) {
        this.durability = Math.max(0, durability);
        set(Components.DURABILITY, new DurabilityComponent(this.maxDurability, this.durability));
    }

    public int maxDurability() {
        return maxDurability;
    }

    public void maxDurability(int maxDurability) {
        this.maxDurability = maxDurability;
        set(Components.DURABILITY, new DurabilityComponent(this.maxDurability, this.durability));
    }

    public boolean isBroken() {
        return durability <= 0;
    }

    public void damage(int amount) {
        durability(durability - amount);
    }

    public void repair(int amount) {
        durability(Math.min(maxDurability, durability + amount));
    }

    public boolean isEquippable() {
        return slot() != ItemSlot.CONSUMABLE && slot() != ItemSlot.MATERIAL;
    }

    public boolean isConsumable() {
        return slot() == ItemSlot.CONSUMABLE || has(Components.CONSUMABLE) || has(Components.FOOD);
    }

    public boolean isStaff() {
        if (hasTag("staff")) return true;
        WeaponComponent weapon = get(Components.WEAPON);
        if (weapon != null && weapon.isStaff()) return true;
        String idStr = id().toLowerCase();
        return idStr.contains("staff") || idStr.contains("wand");
    }

    public boolean isScroll() {
        if (hasTag("scroll")) return true;
        String idStr = id().toLowerCase();
        return idStr.contains("scroll");
    }

    public String displayName() {
        if (isScroll() && embeddedSpell() != null) {
            return "Scroll of " + embeddedSpell().name();
        }
        if (isStaff() && embeddedSpell() != null) {
            String base = name != null && !name.key().isEmpty() ? name.translated() : (type != null && type.name() != null ? type.name().translated() : "Staff");
            return base + " [" + embeddedSpell().name() + "]";
        }
        if (name != null && !name.key().isEmpty()) return name.translated();
        if (type != null && type.name() != null) return type.name().translated();
        return id();
    }

    public DroppedItem drop(float x, float y, float vx, float vy) {
        return new DroppedItem(this, x, y, vx, vy);
    }

    public DroppedItem drop(float x, float y) {
        return new DroppedItem(this, x, y);
    }

    public boolean canStackWith(ItemStack other) {
        if (other == null) return false;

        // Individualized staves with durability never stack
        if (isStaff() || other.isStaff()) {
            return false;
        }

        // Scrolls with different embedded spells do not stack
        Spell s1 = embeddedSpell();
        Spell s2 = other.embeddedSpell();
        if (s1 != null || s2 != null) {
            if (s1 == null || s2 == null) return false;
            if (!s1.name().equalsIgnoreCase(s2.name())) return false;
        }

        if (type != null && other.type != null) {
            return type.id().equalsIgnoreCase(other.type.id());
        }
        return id().equalsIgnoreCase(other.id());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public ItemStack copy() {
        ItemStack copy = new ItemStack(type, amount);
        for (var entry : customComponents.asMap().entrySet()) {
            copy.set((ComponentType) entry.getKey(), entry.getValue());
        }
        copy.durability = this.durability;
        copy.maxDurability = this.maxDurability;
        copy.embeddedSpell = this.embeddedSpell;
        return copy;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof ItemStack other) {
            return canStackWith(other);
        }
        if (obj instanceof ItemType itemType) {
            return Objects.equals(id(), itemType.id());
        }
        if (obj instanceof String str) {
            return Objects.equals(id(), str) || (name != null && name.equals(str));
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id());
    }

    @Override
    public String toString() {
        return "ItemStack{" + "id=" + id() + ", amount=" + amount + '}';
    }
}
