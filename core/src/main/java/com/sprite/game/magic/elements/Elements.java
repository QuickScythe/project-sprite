package com.sprite.game.magic.elements;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.sprite.data.registries.Registries;
import com.sprite.data.registries.Registry;
import com.sprite.resource.Resource;
import com.sprite.data.utils.Resources;
import com.sprite.data.utils.Utils;
import org.json.JSONObject;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class Elements {

    private static final Registry<Element> registry = Registries.register("elements", () -> null);

    public static void initialize() {
        Resources resources = Utils.resources();
        List<Resource> magic = resources.list("magic");
        int i = 0;
        for (Resource resource : magic) {
            if (!resource.location().path().startsWith("element/")) continue;
            i = i + 1;
            FileHandle handle = resource.file();
            String read = handle.readString();
            Element element = getElement(resource, read);

            Gdx.app.log("Elements", "Registering element: " + resource.location());

            registry.register(resource.location().toString(), element);
        }
        Gdx.app.log("Elements", "Loaded " + i + " elements.");
    }

    private static Element getElement(Resource resource, String read) {

        JSONObject data = new JSONObject(read);
        return new Element() {

            @Override
            public String name() {
                return data.getString("name");
            }

            @Override
            public int cost() {
                return data.getInt("mana_cost");
            }

            @Override
            public int cooldown() {
                return data.getInt("cooldown");
            }
        };
    }

    public static Optional<Element> get(String key) {
        return registry.get(key);
    }

    public static Collection<Element> values() {
        return registry.values();
    }
}
