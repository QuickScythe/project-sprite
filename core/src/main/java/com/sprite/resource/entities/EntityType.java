package com.sprite.resource.entities;

import com.sprite.data.TranslatableString;
import com.sprite.resource.Resource;
import com.sprite.resource.ResourceMeta;
import com.sprite.resource.controllers.Controller;
import com.sprite.resource.input.Input;
import com.sprite.resource.models.Model;
import com.sprite.utils.Utils;
import org.json.JSONObject;

public class EntityType  {

    public final Model model;
    public final Controller controller;
    public final int health;
    public final float speed;
    public final TranslatableString name;
    public final float width;
    public final float height;

    public EntityType(Resource resource) {
        if(!resource.type().equals(Resource.Type.DATA)) throw new IllegalStateException("Controller resource must be of type DATA");
        ResourceMeta.Json data = (ResourceMeta.Json) resource.data.data();
        model = Utils.resources().MODELS.load(data.get().getString("model"));
        health = data.get().getInt("health");
        speed = (float) data.get().getDouble("speed");
        name = new TranslatableString(data.get().getString("name"));
        width = (float) data.get().getDouble("width");
        height = (float) data.get().getDouble("height");
        controller = Utils.resources().CONTROLLERS.load(data.get().getString("controller"));
    }

}
