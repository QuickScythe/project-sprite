package com.sprite.resource.magic.spells;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.sprite.data.registries.Registries;
import com.sprite.data.registries.Registry;
import com.sprite.game.combat.DamageType;
import com.sprite.resource.magic.elements.Element;
import com.sprite.resource.magic.elements.Elements;
import com.sprite.resource.Resource;
import com.sprite.data.utils.resources.Resources;
import com.sprite.data.utils.Utils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Spells {

    private static boolean initialized = false;

    static {
        initialize();
    }

    public static Registry<Spell> registry() {
        return Registries.register("spells", () -> null);
    }

    public static synchronized void initialize() {
        Registry<Spell> reg = registry();
        Resources resources = Utils.resources();
        if (Gdx.app != null) {
            Gdx.app.log("Spells", "Loading spells...");
        }

        List<java.util.Map.Entry<String, Spell>> toRegister = new ArrayList<>();
        if (resources != null) {
            try {
                List<Resource> spells = new ArrayList<>();
                try {
                    spells.addAll(resources.list("magic"));
                } catch (Exception ignored) {}
                try {
                    spells.addAll(resources.list("spells"));
                } catch (Exception ignored) {}
                try {
                    if (resources.RESOURCE_REGISTRY != null) {
                        for (Resource res : resources.RESOURCE_REGISTRY.values()) {
                            if (res != null && res.location() != null) {
                                String loc = res.location().toString();
                                if ((loc.startsWith("magic:spell/") || loc.startsWith("spells:") || loc.startsWith("spell:")) && !spells.contains(res)) {
                                    spells.add(res);
                                }
                            }
                        }
                    }
                } catch (Exception ignored) {}

                for (Resource resource : spells) {
                    if (resource == null || resource.location() == null) continue;
                    String locPath = resource.location().path();
                    if (!locPath.startsWith("spell/") && !resource.location().toString().startsWith("spells:") && !resource.location().toString().startsWith("magic:spell/")) {
                        continue;
                    }

                    JSONObject json = null;
                    if (resource.data != null && resource.data.data() instanceof com.sprite.resource.ResourceMeta.Json jsonMeta) {
                        json = jsonMeta.get();
                    } else if (resource.file() != null && resource.file().exists()) {
                        json = new JSONObject(resource.file().readString());
                    }
                    if (json == null) continue;
                    JSONObject data = json.optJSONObject("data");
                    if (data == null) data = json;
                    JSONObject requirements = data.optJSONObject("requirements");
                    Element[] elements = new Element[0];
                    if (requirements != null && requirements.has("elements")) {
                        JSONArray elemJsonArray = requirements.getJSONArray("elements");
                        List<Element> elemList = new ArrayList<>();
                        for (int i = 0; i < elemJsonArray.length(); i++) {
                            String elemKey = elemJsonArray.getString(i);
                            Elements.get(elemKey).ifPresent(elemList::add);
                        }
                        elements = elemList.toArray(new Element[0]);
                    }

                    String name = data.optString("name", resource.location().path());
                    String description = data.optString("description", "");
                    int extraMana = data.optInt("mana_cost", data.optInt("extra_mana_cost", 0));
                    float cooldown = (float) data.optDouble("cooldown", 1.5);
                    float damage = (float) data.optDouble("damage", 30.0 + elements.length * 15.0);
                    String dmgTypeStr = data.optString("damage_type", data.optString("damageType", "ARCANE"));
                    com.sprite.game.combat.DamageType parsedDmgType = com.sprite.game.combat.DamageType.ARCANE;
                    try {
                        parsedDmgType = com.sprite.game.combat.DamageType.valueOf(dmgTypeStr.toUpperCase());
                    } catch (Exception ignored) {}
                    final com.sprite.game.combat.DamageType finalDmgType = parsedDmgType;

                    final Element[] finalElements = elements;
                    Spell spell = new Spell() {
                        private float currentCooldown = 0f;

                        @Override
                        public Element[] elements() {
                            return finalElements;
                        }

                        @Override
                        public String name() {
                            return name;
                        }

                        @Override
                        public String description() {
                            return description;
                        }

                        @Override
                        public int extraManaCost() {
                            return extraMana;
                        }

                        @Override
                        public float cooldown() {
                            return cooldown;
                        }

                        @Override
                        public float currentCooldown() {
                            return currentCooldown;
                        }

                        @Override
                        public void update(float delta) {
                            if (currentCooldown > 0f) {
                                currentCooldown = Math.max(0f, currentCooldown - delta);
                            }
                        }

                        @Override
                        public void setCooldown(float val) {
                            currentCooldown = Math.max(0f, val);
                        }

                        @Override
                        public com.sprite.game.combat.DamageType damageType() {
                            return finalDmgType;
                        }

                        @Override
                        public float damage() {
                            return damage;
                        }
                    };

                    addEntries(toRegister, resource.location().toString(), spell);
                    addEntries(toRegister, resource.location().path(), spell);
                    String path = resource.location().path();
                    String nameOnly = path.startsWith("spell/") ? path.substring("spell/".length()) : path;
                    addEntries(toRegister, nameOnly, spell);
                    if (name != null) {
                        addEntries(toRegister, name.toLowerCase(), spell);
                    }
                }
            } catch (Throwable e) {
                if (Gdx.app != null) {
                    Gdx.app.log("Spells", "Error scanning magic spells: " + e.getMessage());
                }
            }
        }

        // Register default spell definitions if empty
        if (toRegister.isEmpty() || reg.values().isEmpty()) {
            registerDefaults(toRegister);
        }

        // Sort by number of required elements (descending) so more complex spells take priority
        toRegister.sort((a, b) -> Integer.compare(b.getValue().elements().length, a.getValue().elements().length));

        for (java.util.Map.Entry<String, Spell> entry : toRegister) {
            reg.register(entry.getKey().toLowerCase(), entry.getValue());
        }
        initialized = true;
    }

    private static void registerDefaults(List<java.util.Map.Entry<String, Spell>> toRegister) {
        Element fire = Elements.get("fire").orElse(null);
        Element earth = Elements.get("earth").orElse(null);
        Element wind = Elements.get("wind").orElse(null);

        // Hellfire: Earth + Wind + Fire
        if (earth != null && wind != null && fire != null) {
            Spell hellfire = createSpell("Hellfire", "Unleash a catastrophic barrage of volcanic hellfire.",
                new Element[]{earth, wind, fire}, 20, 3.0f, 75.0f, DamageType.FIRE);
            addEntries(toRegister, "hellfire", hellfire);
        }

        // Inferno: Fire + Wind
        if (fire != null && wind != null) {
            Spell inferno = createSpell("Inferno", "Hurl a raging fiery vortex that incinerates enemies.",
                new Element[]{fire, wind}, 15, 2.0f, 55.0f, DamageType.FIRE);
            addEntries(toRegister, "inferno", inferno);
        }

        // Magma: Fire + Earth
        if (fire != null && earth != null) {
            Spell magma = createSpell("Magma", "Launch an explosive orb of molten magma with crushing knockback.",
                new Element[]{fire, earth}, 10, 2.5f, 50.0f, DamageType.FIRE);
            addEntries(toRegister, "magma", magma);
        }

        // Wind: Wind
        if (wind != null) {
            Spell windSpell = createSpell("Wind", "Release a swift razor wind slicing through foes.",
                new Element[]{wind}, 0, 1.0f, 28.0f, DamageType.ARCANE);
            addEntries(toRegister, "wind", windSpell);
        }

        // Fire: Fire
        if (fire != null) {
            Spell fireSpell = createSpell("Fire", "Cast a bolt of scorching fire.",
                new Element[]{fire}, 0, 1.2f, 32.0f, DamageType.FIRE);
            addEntries(toRegister, "fire", fireSpell);
        }

        // Earth: Earth
        if (earth != null) {
            Spell earthSpell = createSpell("Earth", "Hurl a heavy stone projectile.",
                new Element[]{earth}, 0, 1.8f, 38.0f, DamageType.PHYSICAL);
            addEntries(toRegister, "earth", earthSpell);
        }

        // Standard Spells
        Spell fireball = createSpell("Fireball", "Hurl an explosive orb of roaring flame.",
            fire != null ? new Element[]{fire} : new Element[0], 20, 1.2f, 28.0f, DamageType.FIRE);
        addEntries(toRegister, "fireball", fireball);

        Spell frostNova = createSpell("Frost Nova", "Release an icy explosion that slows enemies.",
            new Element[0], 30, 3.0f, 22.0f, DamageType.FROST);
        addEntries(toRegister, "frost_nova", frostNova);

        Spell thunderBolt = createSpell("Thunderbolt", "Strike foes with a high-velocity lightning bolt.",
            new Element[0], 25, 2.0f, 38.0f, DamageType.LIGHTNING);
        addEntries(toRegister, "thunder_bolt", thunderBolt);
        addEntries(toRegister, "thunderbolt", thunderBolt);

        Spell arcaneHeal = createSpell("Arcane Heal", "Channel restorative magic and gain health regeneration.",
            new Element[0], 35, 6.0f, 40.0f, DamageType.HEAL);
        addEntries(toRegister, "arcane_heal", arcaneHeal);

        Spell meteorStrike = createSpell("Meteor Strike", "Call down a catastrophic burning meteor.",
            fire != null && earth != null ? new Element[]{fire, earth} : new Element[0], 55, 8.0f, 75.0f, DamageType.FIRE);
        addEntries(toRegister, "meteor_strike", meteorStrike);
    }

    private static void addEntries(List<java.util.Map.Entry<String, Spell>> toRegister, String key, Spell spell) {
        toRegister.add(new java.util.AbstractMap.SimpleEntry<>(key, spell));
        toRegister.add(new java.util.AbstractMap.SimpleEntry<>("spell/" + key, spell));
        toRegister.add(new java.util.AbstractMap.SimpleEntry<>("magic:spell/" + key, spell));
    }

    private static Spell createSpell(String name, String desc, Element[] elems, int extraMana, float cd, float dmg, DamageType dmgType) {
        return new Spell() {
            private float currentCooldown = 0f;

            @Override public Element[] elements() { return elems; }
            @Override public String name() { return name; }
            @Override public String description() { return desc; }
            @Override public int extraManaCost() { return extraMana; }
            @Override public float cooldown() { return cd; }
            @Override public float currentCooldown() { return currentCooldown; }
            @Override public void update(float delta) {
                if (currentCooldown > 0f) {
                    currentCooldown = Math.max(0f, currentCooldown - delta);
                }
            }
            @Override public void setCooldown(float val) { currentCooldown = Math.max(0f, val); }
            @Override public DamageType damageType() { return dmgType != null ? dmgType : DamageType.ARCANE; }
            @Override public float damage() { return dmg; }
        };
    }

    private static Spell createSpell(String name, String desc, Element[] elems, int extraMana, float cd, float dmg) {
        return createSpell(name, desc, elems, extraMana, cd, dmg, DamageType.ARCANE);
    }

    /**
     * Synthesizes exactly ONE spell from the ordered list of crafted elements.
     * Evaluates registered composite recipes first (highest complexity / element count),
     * and falls back to synthesized composite or elemental spells.
     */
    public static Spell synthesize(List<Element> elements) {
        if (elements == null || elements.isEmpty()) {
            return null;
        }

        Element[] array = elements.toArray(new Element[0]);

        // 1. Search for best matching composite spell (sorted by recipe length descending)
        for (Spell spell : registry().values()) {
            if (spell.elements().length > array.length) continue;
            if (spell.matches(array)) {
                return spell;
            }
        }

        // 2. If no exact registered recipe, create dynamic elemental burst based on primary element
        Element primary = elements.get(0);
        String name = primary.name().substring(0, 1).toUpperCase() + primary.name().substring(1) + " Burst";
        float baseDmg = 25.0f + elements.size() * 12.0f;
        int extraMana = Math.max(0, (elements.size() - 1) * 5);

        return new Spell() {
            private float currentCooldown = 0f;

            @Override
            public Element[] elements() {
                return array;
            }

            @Override
            public String name() {
                return name;
            }

            @Override
            public String description() {
                return "A synthesized burst channeling " + elements.size() + " elemental reagents.";
            }

            @Override
            public int extraManaCost() {
                return extraMana;
            }

            @Override
            public float cooldown() {
                return 1.5f;
            }

            @Override
            public float currentCooldown() {
                return currentCooldown;
            }

            @Override
            public void update(float delta) {
                if (currentCooldown > 0f) {
                    currentCooldown = Math.max(0f, currentCooldown - delta);
                }
            }

            @Override
            public void setCooldown(float val) {
                currentCooldown = Math.max(0f, val);
            }

            @Override
            public float damage() {
                return baseDmg;
            }
        };
    }

    public static Spell[] generate(Element... elements) {
        if (elements == null || elements.length == 0) return new Spell[0];

        Map<Element, Integer> available = new HashMap<>();
        for (Element e : elements) {
            if (e != null) {
                available.merge(e, 1, Integer::sum);
            }
        }

        List<Spell> spellList = new ArrayList<>();
        for (Spell spell : registry().values()) {
            if (spell.elements().length > elements.length) continue;
            if (!spell.matches(elements)) continue;

            Map<Element, Integer> required = new HashMap<>();
            for (Element e : spell.elements())
                required.merge(e, 1, Integer::sum);
            if (!canCast(required, available)) continue;
            spellList.add(spell);
            required.forEach((key, value) -> available.put(key, available.get(key) - value));
        }
        spellList.sort((a, b) -> Integer.compare(b.elements().length, a.elements().length));
        return spellList.toArray(new Spell[0]);
    }

    private static boolean canCast(Map<Element, Integer> required, Map<Element, Integer> available) {
        for (Map.Entry<Element, Integer> entry : required.entrySet()) {
            Element key = entry.getKey();
            Integer value = entry.getValue();
            int check = available.getOrDefault(key, 0);
            if (check < value)  return false;
        }
        return true;
    }

    public static Optional<Spell> get(String key) {
        if (key == null) return Optional.empty();
        Registry<Spell> reg = registry();
        String clean = key.trim().toLowerCase();
        Optional<Spell> opt = reg.get(clean);
        if (opt.isPresent()) return opt;
        if (clean.startsWith("magic:")) {
            opt = reg.get(clean.substring(6));
            if (opt.isPresent()) return opt;
        }
        if (clean.startsWith("spell/")) {
            opt = reg.get(clean.substring(6));
            if (opt.isPresent()) return opt;
        }
        return reg.get("magic:spell/" + clean);
    }

    public static Collection<Spell> values() {
        return registry().values();
    }
}
