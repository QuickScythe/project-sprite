package com.sprite.resource.entities;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.sprite.data.utils.Utils;
import com.sprite.game.world.entities.Entity;
import com.sprite.resource.Resource;
import com.sprite.resource.ResourceMeta;
import com.sprite.resource.animations.Animation;
import com.sprite.resource.texture.GameSprite;
import org.json.JSONArray;
import org.json.JSONObject;

public class CollisionData {

    public final Part[] parts;


    public CollisionData(Resource resource) {
        if(!resource.type().equals(Resource.Type.DATA)) throw new IllegalStateException("Controller resource must be of type DATA");
        ResourceMeta.Json data = (ResourceMeta.Json) resource.data.data();
        JSONObject json = data.get();
        if(!json.has("data")) {
            throw new IllegalArgumentException("Collision data must contain a 'data' key");
        }
        JSONArray partsData = json.getJSONArray("data");
        parts = new Part[partsData.length()];
        for (int i = 0; i < partsData.length(); i++) {
            JSONObject partData = partsData.getJSONObject(i);
            String name = partData.optString("part", "part_" + i);
            if(!partData.has("box"))
                throw new IllegalArgumentException("Collision data must contain a 'box' key for each part");
            JSONObject box = partData.getJSONObject("box");
            if(!box.has("from") || !box.has("to"))
                throw new IllegalArgumentException("Collision data must contain 'from' and 'to' keys for each part");
            if(!(box.get("from") instanceof JSONArray) && !(box.get("to") instanceof JSONArray)) {
                throw new IllegalArgumentException("Collision data 'from' and 'to' keys must be arrays");
            }
            JSONArray fromArray = box.getJSONArray("from");
            JSONArray toArray = box.getJSONArray("to");
            Vector2 from = new Vector2(fromArray.getFloat(0), fromArray.getFloat(1));
            Vector2 to = new Vector2(toArray.getFloat(0), toArray.getFloat(1));
            parts[i] = new Part(name, from, to);
        }
    }

    public record Part(String name, Vector2 from, Vector2 to) {

    }
}
