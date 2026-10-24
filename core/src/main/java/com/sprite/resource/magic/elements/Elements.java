package com.sprite.resource.magic.elements;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.sprite.data.registries.Registries;
import com.sprite.data.registries.Registry;
import com.sprite.resource.Resource;
import com.sprite.resource.ResourceMeta;
import com.sprite.data.utils.resources.Resources;
import com.sprite.data.utils.Utils;
import org.json.JSONObject;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class Elements {

    private static boolean initialized = false;

    public static Element FIRE;
    public static Element EARTH;
    public static Element WIND;

    static {
        initialize();
    }

    public static Registry<Element> registry() {
        return Registries.register("elements", () -> null);
    }

    public static synchronized void initialize() {
        Registry<Element> reg = registry();
        Resources resources = Utils.resources();
        int i = 0;
        if (resources != null) {
            try {
                List<Resource> magic = resources.list("magic");
                for (Resource resource : magic) {
                    if (resource == null || resource.location() == null) continue;
                    if (!resource.location().path().startsWith("element/")) continue;
                    i = i + 1;
                    JSONObject data = null;
                    if (resource.data != null && resource.data.data() instanceof ResourceMeta.Json jsonMeta) {
                        data = jsonMeta.get();
                    } else if (resource.file() != null && resource.file().exists()) {
                        data = new JSONObject(resource.file().readString());
                    }
                    if (data == null) continue;
                    Element element = getElement(resource, data);

                    if (Gdx.app != null) {
                        Gdx.app.log("Elements", "Registering element: " + resource.location());
                    }

                    String fullLoc = resource.location().toString(); // e.g. "magic:element/fire"
                    String path = resource.location().path();       // e.g. "element/fire"
                    String simpleName = path.substring("element/".length()); // e.g. "fire"

                    reg.register(fullLoc, element);
                    reg.register(path, element);
                    reg.register(simpleName, element);
                    if (element.name() != null) {
                        reg.register(element.name().toLowerCase(), element);
                    }
                }
                if (Gdx.app != null) {
                    Gdx.app.log("Elements", "Loaded " + i + " elements.");
                }
            } catch (Throwable e) {
                if (Gdx.app != null) {
                    Gdx.app.log("Elements", "Error scanning magic elements: " + e.getMessage());
                }
            }
        }

        // Register robust fallbacks if not present
        if (reg.get("fire").isEmpty() && reg.get("magic:element/fire").isEmpty()) {
            Element fire = createDefault("fire", 10, 1, new Color(1.0f, 0.35f, 0.1f, 1f));
            registerAllKeys(reg, "fire", "element/fire", "magic:element/fire", fire);
        }
        if (reg.get("earth").isEmpty() && reg.get("magic:element/earth").isEmpty()) {
            Element earth = createDefault("earth", 30, 5, new Color(0.45f, 0.75f, 0.25f, 1f));
            registerAllKeys(reg, "earth", "element/earth", "magic:element/earth", earth);
        }
        if (reg.get("wind").isEmpty() && reg.get("magic:element/wind").isEmpty()) {
            Element wind = createDefault("wind", 5, 2, new Color(0.35f, 0.85f, 0.95f, 1f));
            registerAllKeys(reg, "wind", "element/wind", "magic:element/wind", wind);
        }

        FIRE = get("fire").orElse(null);
        EARTH = get("earth").orElse(null);
        WIND = get("wind").orElse(null);
        initialized = true;
    }

    private static void registerAllKeys(Registry<Element> reg, String key1, String key2, String key3, Element element) {
        reg.register(key1, element);
        reg.register(key2, element);
        reg.register(key3, element);
    }

    private static Element createDefault(String name, int manaCost, int cooldown, Color color) {
        return new Element() {
            @Override
            public String name() { return name; }
            @Override
            public int cost() { return manaCost; }
            @Override
            public int cooldown() { return cooldown; }
            @Override
            public Color color() { return color; }
        };
    }

    private static Element getElement(Resource resource, JSONObject data) {
        String name = data.optString("name", resource.location().path());
        int manaCost = data.optInt("mana_cost", data.optInt("cost", 10));
        int cooldown = data.optInt("cooldown", 1);
        String colorHex = data.optString("color", null);
        Color parsedColor = colorHex != null ? Color.valueOf(colorHex) : null;
        if (parsedColor == null) {
            String lower = name.toLowerCase();
            if (lower.contains("fire")) {
                parsedColor = new Color(1.0f, 0.35f, 0.1f, 1f);
            } else if (lower.contains("earth")) {
                parsedColor = new Color(0.45f, 0.75f, 0.25f, 1f);
            } else if (lower.contains("wind") || lower.contains("air")) {
                parsedColor = new Color(0.35f, 0.85f, 0.95f, 1f);
            }
        }
        final Color finalColor = parsedColor;

        return new Element() {
            @Override
            public String name() {
                return name;
            }

            @Override
            public int cost() {
                return manaCost;
            }

            @Override
            public int cooldown() {
                return cooldown;
            }

            @Override
            public Color color() {
                return finalColor != null ? finalColor : Element.super.color();
            }
        };
    }

    public static Optional<Element> get(String key) {
        if (key == null) return Optional.empty();
        Registry<Element> reg = registry();
        String clean = key.trim().toLowerCase();
        Optional<Element> opt = reg.get(clean);
        if (opt.isPresent()) return opt;
        if (clean.startsWith("magic:")) {
            opt = reg.get(clean.substring(6));
            if (opt.isPresent()) return opt;
        }
        if (clean.startsWith("element/")) {
            opt = reg.get(clean.substring(8));
            if (opt.isPresent()) return opt;
        }
        return reg.get("magic:element/" + clean);
    }

    public static Collection<Element> values() {
        return registry().values();
    }
}
