package com.sprite.game.magic.spells;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.sprite.data.registries.Registries;
import com.sprite.data.registries.Registry;
import com.sprite.game.magic.elements.Element;
import com.sprite.game.magic.elements.Elements;
import com.sprite.resource.Resource;
import com.sprite.data.utils.Resources;
import com.sprite.data.utils.Utils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Spells {

    private static final Registry<Spell> registry = Registries.register("spells", () -> null);

    public static void initialize() {
        Resources resources = Utils.resources();
        List<Resource> spells = resources.list("magic");
        Gdx.app.log("Spells", "Loading spells...");

        // Load all spells first
        List<java.util.Map.Entry<String, Spell>> toRegister = new ArrayList<>();
        for (Resource resource : spells) {
            if (!resource.location().path().startsWith("spell/")) continue;
            FileHandle handle = resource.file();
            String read = handle.readString();
            JSONObject data = new JSONObject(read);
            JSONObject requirements = data.getJSONObject("requirements");
            JSONArray elemJsonArray = requirements.getJSONArray("elements");
            Element[] elements = new Element[elemJsonArray.length()];
            for (int i = 0; i < elemJsonArray.length(); i++) {
                String elem = elemJsonArray.getString(i);
                Element element = Elements.get(elem).orElseThrow();
                elements[i] = element;
            }

            Spell spell = new Spell() {
                @Override
                public Element[] elements() {
                    return elements;
                }

                @Override
                public String name() {
                    return data.getString("name");
                }
            };

            toRegister.add(new java.util.AbstractMap.SimpleEntry<>(resource.location().toString(), spell));
        }

        // Sort by number of required elements (descending) so more complex spells are registered first
        toRegister.sort((a, b) -> Integer.compare(b.getValue().elements().length, a.getValue().elements().length));

        // Register in the sorted order; relies on LinkedHashMap in Registry to preserve insertion order
        for (java.util.Map.Entry<String, Spell> entry : toRegister)
            registry.register(entry.getKey(), entry.getValue());

    }


    public static Spell[] generate(Element... elements) {
        Map<Element, Integer> available = new HashMap<>();
        for (Element e : elements)
            available.merge(e, 1, Integer::sum);


        List<Spell> spellList = new ArrayList<>();
        for (Spell spell : registry.values()) {
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
}
