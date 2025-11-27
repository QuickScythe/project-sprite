package com.sprite.resource.ui;

import com.badlogic.gdx.math.Vector2;
import com.sprite.data.registries.Registry;
import com.sprite.data.registries.RegistryKey;
import com.sprite.data.utils.Utils;
import com.sprite.render.screen.GameScreen;
import com.sprite.resource.ui.dialog.Button;
import com.sprite.resource.ui.dialog.DialogElement;
import com.sprite.resource.ui.dialog.Label;
import com.sprite.resource.ui.dialog.Slider;
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
                Slider slider = new Slider(
                    key,
                    sliderData.optString("label", "ui.slider.value"),
                    sliderData.optInt("min", 0),
                    sliderData.optInt("max", 10),
                    sliderData.optInt("value", 5),
                    sliderData.optInt("step", 1),
                    sliderData.has("label"),
                    new Vector2(positionArray.getInt(0),
                        positionArray.getInt(1)),
                    new Vector2(scaleArray.getInt(0),
                        scaleArray.getInt(1)),
                    DialogUI.this);
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
