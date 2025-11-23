package com.sprite.resource.ui;

import com.badlogic.gdx.math.Vector2;
import com.sprite.data.registries.Registry;
import com.sprite.data.registries.RegistryKey;
import com.sprite.data.utils.Utils;
import com.sprite.render.screen.GameScreen;
import com.sprite.resource.ui.dialog.Button;
import com.sprite.resource.ui.dialog.DialogElement;
import org.json.JSONArray;
import org.json.JSONObject;

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
            if (itemData.getString("type").equalsIgnoreCase("button")) {
                JSONObject buttonData = itemData.getJSONObject("button");
                Button button = new Button(key, buttonData.getString("title"), new Vector2(positionArray.getInt(0), positionArray.getInt(1)), new Vector2(scaleArray.getInt(0), scaleArray.getInt(1)));
                elements.register(key, button);
            }
        }
    }

    public Registry<DialogElement> buttons() {
        return elements;
    }

    @Override
    public void draw(GameScreen screen) {
        screen.sprite().draw(texture.sprite(), (screen.camera().viewportWidth / 2) - (textureWidth / 2), (screen.camera().viewportHeight / 2) - (textureHeight / 2), textureWidth, textureHeight);
//        screen.shape().end();
//        screen.sprite().begin();
    }
}
