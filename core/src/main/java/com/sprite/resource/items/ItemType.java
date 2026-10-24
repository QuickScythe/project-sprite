package com.sprite.resource.items;

import com.sprite.data.TranslatableString;
import com.sprite.data.utils.Utils;
import com.sprite.game.items.ItemStack;
import com.sprite.game.items.ItemRarity;
import com.sprite.game.items.ItemSlot;
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
import com.sprite.resource.DataResource;
import com.sprite.resource.Resource;
import com.sprite.resource.ResourceMeta;
import com.sprite.resource.texture.GameSprite;
import org.json.JSONObject;

import java.util.List;

public class ItemType extends DataResource {

    private final String id;
    private final TranslatableString name;
    private final TranslatableString description;
    private final ItemRarity rarity;
    private final ItemSlot slot;
    private final int value; // Gold value

    // Texture Reference
    private final String textureLocation;
    private GameSprite texture = null;

    private final ComponentMap components;

    public ItemType(Resource resource) {
       super(resource);

       JSONObject data = meta().get();

        this.id = resource.location() != null ? resource.location().path() : "unknown_item";
        this.name = new TranslatableString(data.optString("name", "item." + id + ".name"));
        this.description = new TranslatableString(data.optString("description", "item." + id + ".description"));
        this.rarity = parseRarity(data.optString("rarity", "COMMON"));
        this.slot = parseSlot(data.optString("slot", "MATERIAL"));
        this.value = data.optInt("value", data.optInt("gold", 10));

        this.textureLocation = data.optString("texture", "textures:missing");
        this.components = new ComponentMap();
        try {
            if (Utils.resources() != null && Utils.resources().TEXTURES != null) {
                this.texture = Utils.resources().TEXTURES.load(this.textureLocation);
            }
        } catch (Throwable ignored) {}

        parseComponents(data, this.components);
    }
    private void parseComponents(JSONObject data, ComponentMap components) {
        if (data == null || components == null) return;
        JSONObject compObj = data.optJSONObject("components");

        // Parse tags
        if (compObj != null && compObj.has("tags")) {
            components.set(Components.TAGS, Components.TAGS.parse(compObj.opt("tags")));
        } else if (data.has("tags")) {
            components.set(Components.TAGS, Components.TAGS.parse(data.opt("tags")));
        }

        // Parse stats
        if (compObj != null && compObj.has("stats")) {
            components.set(Components.STATS, Components.STATS.parse(compObj.opt("stats")));
        } else if (data.has("stats")) {
            components.set(Components.STATS, Components.STATS.parse(data.opt("stats")));
        } else {
            StatModifiersComponent stats = StatModifiersComponent.fromJson(data);
            if (!stats.modifiers().isEmpty()) {
                components.set(Components.STATS, stats);
            }
        }

        // Parse equippable
        if (compObj != null && compObj.has("equippable")) {
            components.set(Components.EQUIPPABLE, Components.EQUIPPABLE.parse(compObj.opt("equippable")));
        }

        // Parse weapon
        if (compObj != null && compObj.has("weapon")) {
            components.set(Components.WEAPON, Components.WEAPON.parse(compObj.opt("weapon")));
        } else if (data.has("weapon")) {
            components.set(Components.WEAPON, Components.WEAPON.parse(data.opt("weapon")));
        } else {
            String slotStr = data.optString("slot", "");
            if ("WEAPON".equalsIgnoreCase(slotStr)) {
                components.set(Components.WEAPON, WeaponComponent.fromJson(data));
            }
        }

        // Parse consumable
        if (compObj != null && compObj.has("consumable")) {
            components.set(Components.CONSUMABLE, Components.CONSUMABLE.parse(compObj.opt("consumable")));
        } else if (data.has("consumable")) {
            components.set(Components.CONSUMABLE, Components.CONSUMABLE.parse(data.opt("consumable")));
        } else if ("CONSUMABLE".equalsIgnoreCase(data.optString("slot", "")) || data.has("heal") || data.has("healAmount") || data.has("manaHeal") || data.has("effect")) {
            components.set(Components.CONSUMABLE, ConsumableComponent.fromJson(data));
        }

        // Parse food
        if (compObj != null && compObj.has("food")) {
            components.set(Components.FOOD, Components.FOOD.parse(compObj.opt("food")));
        } else if (data.has("food")) {
            components.set(Components.FOOD, Components.FOOD.parse(data.opt("food")));
        }

        // Parse durability
        if (compObj != null && compObj.has("durability")) {
            components.set(Components.DURABILITY, Components.DURABILITY.parse(compObj.opt("durability")));
        } else if (data.has("durability")) {
            components.set(Components.DURABILITY, Components.DURABILITY.parse(data.opt("durability")));
        }

        // Parse spell
        if (compObj != null && compObj.has("spell")) {
            components.set(Components.SPELL, Components.SPELL.parse(compObj.opt("spell")));
        } else if (data.has("spell")) {
            components.set(Components.SPELL, Components.SPELL.parse(data.opt("spell")));
        }
    }

    private static ItemRarity parseRarity(String str) {
        if (str == null) return ItemRarity.COMMON;
        try {
            return ItemRarity.valueOf(str.trim().toUpperCase());
        } catch (Exception e) {
            return ItemRarity.COMMON;
        }
    }

    private static ItemSlot parseSlot(String str) {
        if (str == null) return ItemSlot.MATERIAL;
        try {
            return ItemSlot.valueOf(str.trim().toUpperCase());
        } catch (Exception e) {
            return ItemSlot.MATERIAL;
        }
    }

    public String id() { return id; }
    public TranslatableString name() { return name; }
    public TranslatableString description() { return description; }
    public ItemRarity rarity() { return rarity; }
    public ItemSlot slot() { return slot; }
    public int value() { return value; }

    public String textureLocation() { return textureLocation; }
    public GameSprite texture() { return texture; }

    public ComponentMap components() {
        return components;
    }

    public <T> T get(ComponentType<T> type) {
        return components.get(type);
    }

    public <T> T getOrDefault(ComponentType<T> type, T defaultValue) {
        return components.getOrDefault(type, defaultValue);
    }

    public boolean has(ComponentType<?> type) {
        return components.has(type);
    }

    public boolean hasTag(String tag) {
        if (tag == null) return false;
        if (components.hasTag(tag)) return true;
        List<String> tags = components.get(Components.TAGS);
        if (tags != null && tags.contains(tag)) return true;
        return false;
    }

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

    public ItemStack create(int amount) {
        return new ItemStack(this, amount);
    }

    public ItemStack create() {
        return new ItemStack(this, 1);
    }

    public boolean isEquippable() {
        return slot != ItemSlot.CONSUMABLE && slot != ItemSlot.MATERIAL;
    }

    public boolean isConsumable() {
        return slot == ItemSlot.CONSUMABLE || has(Components.CONSUMABLE) || has(Components.FOOD);
    }

    public boolean isStaff() {
        if (hasTag("staff")) return true;
        WeaponComponent weapon = get(Components.WEAPON);
        if (weapon != null && weapon.isStaff()) return true;
        return id != null && (id.toLowerCase().contains("staff") || id.toLowerCase().contains("wand"));
    }

    public boolean isScroll() {
        if (hasTag("scroll")) return true;
        return id != null && id.toLowerCase().contains("scroll");
    }
}
