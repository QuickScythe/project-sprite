package com.sprite.resource.ui;

import com.sprite.render.screen.GameScreen;
import com.sprite.resource.DataResource;
import com.sprite.resource.Resource;
import com.sprite.resource.ResourceMeta;
import org.json.JSONObject;

import java.awt.*;

public class UIDefinition extends DataResource {

    private final UIType type;
    private final Resource.Location texture;

    public UIDefinition(Resource resource) {
        super(resource);
        ResourceMeta.Json data = meta();
        String rawType = data.get().getString("type");
        if(rawType == null) throw new IllegalArgumentException("UI type must be specified");
        if(rawType.equalsIgnoreCase("inventory"))
            type = new InventoryUI();
        else if(rawType.equalsIgnoreCase("dialog"))
            type = new DialogUI();
        else throw new IllegalArgumentException("Unknown UI type: " + rawType);

        String full = data.get().getString("texture");
        String[] parts = full.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid resource location format: " + full);
        }
        texture = new Resource.Location(parts[0], parts[1]);
        type.create(this);
    }


    public Resource.Location texture() {
        return texture;
    }

    public UIType type() {
        return type;
    }
}
