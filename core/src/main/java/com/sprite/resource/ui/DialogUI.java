package com.sprite.resource.ui;

 import com.badlogic.gdx.math.Vector2;
 import com.sprite.data.registries.Registry;
 import com.sprite.data.registries.RegistryKey;
 import com.sprite.data.utils.Utils;
 import com.sprite.render.screen.GameScreen;
 import com.sprite.resource.ui.dialog.*;
 import org.json.JSONArray;
 import org.json.JSONObject;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DialogUI extends UIType {

    private final Registry<DialogElement> elements = new Registry<>(RegistryKey.of("elements"));

    public float scale() {
        return 10f;
    }

    @Override
    public void create(UIDefinition uiDefinition) {
        texture = Utils.resources().TEXTURES.load(uiDefinition.texture().toString());
        textureWidth = texture.width() * scale();
        textureHeight = texture.height() * scale();
        JSONObject uiData = uiDefinition.data().getJSONObject("dialog");
        columns = uiData.getInt("columns");
        rows = uiData.getInt("rows");
        gridWidthSize = textureWidth / columns;
        gridHeightSize = textureHeight / rows;
        if (!uiData.has("items")) throw new IllegalArgumentException("Dialog UI must have items");
        JSONObject itemsData = uiData.getJSONObject("items");
        for (String key : itemsData.keySet()) {
            JSONObject itemData = itemsData.getJSONObject(key);
            JSONArray positionArray = itemData.getJSONArray("position");
            JSONArray scaleArray = itemData.getJSONArray("scale");
            if (!itemData.has("type")) throw new IllegalArgumentException("Dialog item (" + key + ") must have type");
            if (itemData.getString("type").equalsIgnoreCase("image")) {
                JSONObject imageData = itemData.getJSONObject("image");
                Image label = new Image(key,
                    imageData.getString("texture"),
                    new Vector2(positionArray.getInt(0),
                        positionArray.getInt(1)),
                    new Vector2(scaleArray.getInt(0),
                        scaleArray.getInt(1)),
                    DialogUI.this);
                elements.register(key, label);

            }
            if (itemData.getString("type").equalsIgnoreCase("label")) {
                JSONObject labelData = itemData.getJSONObject("label");
                Label label = new Label(key,
                    labelData.getString("title"),
                    new Vector2(positionArray.getInt(0),
                        positionArray.getInt(1)),
                    new Vector2(scaleArray.getInt(0),
                        scaleArray.getInt(1)),
                    DialogUI.this);
                elements.register(key, label);
            }
            if (itemData.getString("type").equalsIgnoreCase("slider")) {
                JSONObject sliderData = itemData.getJSONObject("slider");
                // Resolve the default value, supporting patterns like "${settings:volume}" or "${settings:volume|50}"
                ResolvedValue resolved = resolveSliderValue(sliderData.opt("value"), sliderData.optInt("value", 5));
                Slider slider = new Slider(
                    key,
                    sliderData.optString("label", "ui.slider.value"),
                    sliderData.optInt("min", 0),
                    sliderData.optInt("max", 10),
                    resolved.value,
                    sliderData.optInt("step", 1),
                    sliderData.has("label"),
                    new Vector2(positionArray.getInt(0),
                        positionArray.getInt(1)),
                    new Vector2(scaleArray.getInt(0),
                        scaleArray.getInt(1)),
                    DialogUI.this);
                if (resolved.settingsKey != null) {
                    slider.setSettingsKey(resolved.settingsKey);
                }
                elements.register(key, slider);


            }
            if (itemData.getString("type").equalsIgnoreCase("button")) {
                JSONObject buttonData = itemData.getJSONObject("button");
                if(!buttonData.has("action")) throw new IllegalArgumentException("Dialog item (" + key + ") must have action");
                UIAction action;
                if(buttonData.get("action") instanceof JSONObject json){
                    action = new UIAction(json);
                } else {
                    action = Utils.resources().ACTIONS.load(buttonData.getString("action"));
                }
                Button button = new Button(key,
                    buttonData.getString("title"),
                    new Vector2(positionArray.getInt(0),
                        positionArray.getInt(1)),
                    new Vector2(scaleArray.getInt(0),
                        scaleArray.getInt(1)),
                    action,
                    DialogUI.this);
                elements.register(key, button);
            }
        }
    }

    /**
     * Resolves a slider default value that may be a number or a settings placeholder like:
     * - 42
     * - "${settings:volume}"
     * - "${settings:volume|50}" (with inline default)
     */
    private static class ResolvedValue {
        final int value;
        final String settingsKey;
        ResolvedValue(int value, String settingsKey) { this.value = value; this.settingsKey = settingsKey; }
    }

    private ResolvedValue resolveSliderValue(Object rawValue, int fallback) {
        if (rawValue instanceof Number n) {
            return new ResolvedValue(n.intValue(), null);
        }
        if (rawValue instanceof String s) {
            String str = s.trim();
            if (str.startsWith("${") && str.endsWith("}")) {
                String inner = str.substring(2, str.length() - 1); // remove ${ and }
                int colon = inner.indexOf(':');
                if (colon > 0) {
                    String domain = inner.substring(0, colon).trim();
                    String rest = inner.substring(colon + 1).trim();
                    if ("settings".equalsIgnoreCase(domain) && !rest.isEmpty()) {
                        String key = rest;
                        int providedDefault = fallback;
                        int pipe = rest.indexOf('|');
                        if (pipe >= 0) {
                            key = rest.substring(0, pipe).trim();
                            String defStr = rest.substring(pipe + 1).trim();
                            try {
                                providedDefault = Integer.parseInt(defStr);
                            } catch (NumberFormatException ignored) {
                                // keep fallback
                            }
                        }
                        int resolved;
                        try {
                            // Try integer preference first
                            resolved = Utils.settings().getInteger(key, providedDefault);
                        } catch (Throwable t) {
                            // Fallback: try read string and parse
                            try {
                                String v = Utils.settings().getString(key, Integer.toString(providedDefault));
                                resolved = Integer.parseInt(v);
                            } catch (Throwable ignored) {
                                resolved = providedDefault;
                            }
                        }
                        return new ResolvedValue(resolved, key);
                    }
                }
            } else {
                // Attempt to parse as plain integer string
                try {
                    return new ResolvedValue(Integer.parseInt(str), null);
                } catch (NumberFormatException ignored) { }
            }
        }
        return new ResolvedValue(fallback, null);
    }

    public Registry<DialogElement> elements() {
        return elements;
    }

    @Override
    public void draw(GameScreen screen) {
        Vector2 topLeft = uiTopLeft(screen);
        screen.sprite().draw(texture.sprite(), topLeft.x, topLeft.y, textureWidth, textureHeight);
//        screen.shape().end();
//        screen.sprite().begin();
    }
}
