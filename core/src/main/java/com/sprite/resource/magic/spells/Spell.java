package com.sprite.resource.magic.spells;

import com.badlogic.gdx.graphics.Color;
import com.sprite.game.combat.CombatManager;
import com.sprite.game.combat.DamageType;
import com.sprite.game.combat.Projectile;
import com.sprite.game.stats.Stats;
import com.sprite.game.stats.StatusEffect;
import com.sprite.game.world.entities.Entity;
import com.sprite.resource.magic.elements.Element;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface Spell {

    Element[] elements();

    String name();

    default Object id() {
        return name();
    }

    default String description() {
        return "";
    }

    default int manaCost() {
        int cost = extraManaCost();
        if (elements() != null) {
            for (Element e : elements()) {
                if (e != null) {
                    cost += e.cost();
                }
            }
        }
        return cost;
    }

    default int extraManaCost() {
        return 0;
    }

    default float cooldown() {
        return 1.0f;
    }

    default float currentCooldown() {
        return 0f;
    }

    default void update(float delta) {
    }

    default void triggerCooldown() {
        setCooldown(cooldown());
    }

    default void resetCooldown() {
        setCooldown(0f);
    }

    default void setCooldown(float cd) {
    }

    default boolean isReady() {
        return currentCooldown() <= 0f;
    }

    default boolean canCast(Stats stats) {
        return isReady() && (stats == null || stats.currentMana() >= manaCost());
    }

    default float damage() {
        return 35.0f;
    }

    default float basePower() {
        return damage();
    }

    default DamageType damageType() {
        return DamageType.ARCANE;
    }

    default boolean matches(Element[] elements) {
        if (elements == null || elements.length < elements().length) return false;
        Map<Element, Integer> required = new HashMap<>();
        for (Element e : elements()) {
            required.merge(e, 1, Integer::sum);
        }
        Map<Element, Integer> available = new HashMap<>();
        for (Element e : elements) {
            available.merge(e, 1, Integer::sum);
        }
        for (Map.Entry<Element, Integer> entry : required.entrySet()) {
            if (available.getOrDefault(entry.getKey(), 0) < entry.getValue()) {
                return false;
            }
        }
        return true;
    }

    default boolean matchesRecipe(List<Element> crafted) {
        if (crafted == null || crafted.size() < elements().length) return false;
        Element[] array = crafted.toArray(new Element[0]);
        return matches(array);
    }

    default boolean cast(Stats stats, Entity caster, float dirX, float dirY, CombatManager combatManager) {
        if (combatManager == null) return false;
        triggerCooldown();

        float originX = caster != null ? caster.position().x + caster.width() / 2f : 0f;
        float originY = caster != null ? caster.position().y + caster.height() / 2f : 0f;

        float len = (float) Math.hypot(dirX, dirY);
        if (len < 0.001f) {
            dirX = (caster != null && caster.director.isFacingRight()) ? 1f : -1f;
            dirY = 0f;
        } else {
            dirX /= len;
            dirY /= len;
        }

        float magicScaling = stats != null ? stats.magicPower() : 10f;
        float totalPower = damage() + magicScaling;
        String n = name() != null ? name().toLowerCase() : "";

        if (n.contains("hellfire")) {
            float speed = 640f;
            StatusEffect burn = new StatusEffect(5.0f, totalPower * 0.3f, 1.0f, StatusEffect.Type.BURN);
            Projectile hellfire = new Projectile(
                originX, originY, dirX * speed, dirY * speed, 18f,
                caster, totalPower * 1.55f, DamageType.FIRE, burn,
                new Color(1f, 0.15f, 0.05f, 1f), 2.5f
            );
            combatManager.spawnProjectile(hellfire);
        } else if (n.contains("inferno")) {
            float speed = 590f;
            StatusEffect burn = new StatusEffect(4.0f, totalPower * 0.2f, 1.0f, StatusEffect.Type.BURN);
            Projectile inferno = new Projectile(
                originX, originY, dirX * speed, dirY * speed, 15f,
                caster, totalPower * 1.35f, DamageType.FIRE, burn,
                new Color(1f, 0.45f, 0.1f, 1f), 2.2f
            );
            combatManager.spawnProjectile(inferno);
        } else if (n.contains("magma")) {
            float speed = 530f;
            StatusEffect burn = new StatusEffect(3.5f, totalPower * 0.15f, 1.0f, StatusEffect.Type.BURN);
            Projectile magma = new Projectile(
                originX, originY, dirX * speed, dirY * speed, 16f,
                caster, totalPower * 1.25f, DamageType.FIRE, burn,
                new Color(0.95f, 0.3f, 0.1f, 1f), 2.0f
            );
            combatManager.spawnProjectile(magma);
        } else if (n.contains("wind")) {
            float speed = 860f;
            Projectile wind = new Projectile(
                originX, originY, dirX * speed, dirY * speed, 13f,
                caster, totalPower * 0.95f, DamageType.ARCANE, null,
                new Color(0.35f, 0.9f, 1.0f, 1f), 1.8f
            );
            combatManager.spawnProjectile(wind);
        } else if (n.contains("earth")) {
            float speed = 510f;
            Projectile earth = new Projectile(
                originX, originY, dirX * speed, dirY * speed, 16f,
                caster, totalPower * 1.15f, DamageType.PHYSICAL, null,
                new Color(0.55f, 0.4f, 0.2f, 1f), 2.0f
            );
            combatManager.spawnProjectile(earth);
        } else if (n.contains("fireball")) {
            float speed = 580f;
            StatusEffect burn = new StatusEffect(4.0f, totalPower * 0.15f, 1.0f, StatusEffect.Type.BURN);
            Projectile fireball = new Projectile(
                originX, originY, dirX * speed, dirY * speed, 14f,
                caster, totalPower, DamageType.FIRE, burn,
                new Color(1f, 0.45f, 0.1f, 1f), 2.5f
            );
            combatManager.spawnProjectile(fireball);
        } else if (n.contains("frost") || n.contains("ice")) {
            int count = 6;
            float speed = 420f;
            StatusEffect frost = new StatusEffect(3.5f, 0.5f, StatusEffect.Type.FROST);
            for (int i = 0; i < count; i++) {
                double angle = (Math.PI * 2.0 / count) * i;
                float vx = (float) Math.cos(angle) * speed;
                float vy = (float) Math.sin(angle) * speed;
                Projectile shard = new Projectile(
                    originX, originY, vx, vy, 10f,
                    caster, totalPower * 0.65f, DamageType.FROST, frost,
                    new Color(0.2f, 0.8f, 1.0f, 1f), 1.2f
                );
                combatManager.spawnProjectile(shard);
            }
        } else if (n.contains("thunder") || n.contains("lightning")) {
            float speed = 880f;
            Projectile bolt = new Projectile(
                originX, originY, dirX * speed, dirY * speed, 12f,
                caster, totalPower * 1.3f, DamageType.LIGHTNING, null,
                new Color(1f, 0.95f, 0.2f, 1f), 1.8f
            );
            combatManager.spawnProjectile(bolt);
        } else if (n.contains("heal")) {
            float healAmount = 35f + magicScaling * 0.8f;
            if (stats != null) {
                stats.heal(healAmount);
                stats.addEffect(new StatusEffect(8.0f, 8.0f, 1.0f, StatusEffect.Type.REGENERATION));
            }
            combatManager.spawnHealText(originX, originY + 20f, healAmount);
        } else if (n.contains("meteor")) {
            float speed = 550f;
            StatusEffect burn = new StatusEffect(5.0f, totalPower * 0.25f, 1.0f, StatusEffect.Type.BURN);
            Projectile meteor = new Projectile(
                originX + dirX * 120f, originY + 250f, dirX * 180f, -speed, 22f,
                caster, totalPower * 1.8f, DamageType.FIRE, burn,
                new Color(1f, 0.2f, 0.1f, 1f), 1.8f
            );
            combatManager.spawnProjectile(meteor);
        } else if (n.contains("fire")) {
            float speed = 580f;
            StatusEffect burn = new StatusEffect(3.5f, totalPower * 0.15f, 1.0f, StatusEffect.Type.BURN);
            Projectile fire = new Projectile(
                originX, originY, dirX * speed, dirY * speed, 14f,
                caster, totalPower * 1.05f, DamageType.FIRE, burn,
                new Color(1f, 0.5f, 0.1f, 1f), 2.0f
            );
            combatManager.spawnProjectile(fire);
        } else {
            float speed = 600f;
            Projectile generic = new Projectile(
                originX, originY, dirX * speed, dirY * speed, 14f,
                caster, totalPower, damageType(), null,
                new Color(0.7f, 0.4f, 1.0f, 1f), 2.0f
            );
            combatManager.spawnProjectile(generic);
        }

        return true;
    }
}
