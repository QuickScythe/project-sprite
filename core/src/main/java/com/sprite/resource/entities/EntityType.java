package com.sprite.resource.entities;

import com.sprite.data.TranslatableString;
import com.sprite.resource.Resource;
import com.sprite.resource.models.Model;
import com.sprite.utils.Utils;
import org.json.JSONObject;

public class EntityType  {

    public final Model model;
    public final int health;
    public final float speed;
    public final TranslatableString name;

    public EntityType(Resource resource) {
        String read = resource.file().readString();
        JSONObject data = new JSONObject(read);
        model = Utils.resources().MODELS.load(data.getString("model"));
        health = data.getInt("health");
        speed = (float) data.getDouble("speed");
        name = new TranslatableString(data.getString("name"));
    }

}
