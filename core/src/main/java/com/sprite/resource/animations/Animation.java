package com.sprite.resource.animations;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.sprite.data.utils.Utils;
import com.sprite.resource.Resource;
import com.sprite.resource.ResourceMeta;
import com.sprite.game.world.entities.Entity;
import com.sprite.resource.entities.CollisionData;
import org.json.JSONObject;

import java.util.*;

public class Animation {

    public final int frames;
    public final int speed;
//    public final JSONObject frameData;
    public final FrameData[] frameArray;
    public final int index;
    public final String name;

    int frame = 0;
    long update = 0;

    public Animation(Resource resource) {
        if(!resource.type().equals(Resource.Type.DATA)) throw new IllegalStateException("Controller resource must be of type DATA");
        ResourceMeta.Json data = (ResourceMeta.Json) resource.data.data();
        final JSONObject frameData = data.get().getJSONObject("frame_data");
        this.frames = data.get().getInt("frames");
        this.speed = data.get().getInt("speed");
        this.index = data.get().getInt("index");
        this.name = data.get().optString("name", resource.location().path());


        //todo fix:
        frameArray = new FrameData[frames];

        for (int i = 0; i < frames; i++) {
            frameArray[i] = new FrameData(i); //todo placeholder
        }

        //frameKey could be "all" or "0", "1", "2", etc.
        for (String frameKey : frameData.keySet()) {
            JSONObject frame = frameData.getJSONObject(frameKey);
            if (frame.has("collisions")) {
                JSONObject collisions = frame.getJSONObject("collisions");
                for (String collisionKey : collisions.keySet()) {
                    String value = collisions.getString(collisionKey);
                    if (value == null || value.isEmpty() || value.equalsIgnoreCase("none")) continue;
                    if (frameKey.equalsIgnoreCase("all")) {
                        for (int i = 0; i < frames; i++) {
                            frameArray[i].collisions.put(collisionKey, value);
                        }
                        continue;
                    }
                    int index = Integer.parseInt(frameKey);
                    if (index < 0 || index >= frames) continue;
                    frameArray[index].collisions.put(collisionKey, value);
                }
            }
        }


    }

    public int frame(){
        return frame;
    }

    public TextureRegion frame(Entity entity) {
        if (new Date().getTime() - update >= speed * 1) {
            update = new Date().getTime();
            frame = frame + 1;
            if (frame >= frames) {
                frame = 0;
            }
        }
        // Use the entity's model to fetch the proper sprite frame
        var model = entity.model;
        return new TextureRegion(model.texture.sprite, frame * model.width, index * model.height, model.width, model.height);


    }

    public static class FrameData {

        private final Map<String, String> collisions = new HashMap<>();
        private final Map<String, CollisionData> cache = new HashMap<>();

        public final int index;

        public FrameData(int index) {
            this.index = index;
        }

        public List<String> collisions() {
            return new ArrayList<>(collisions.keySet());
        }

        public CollisionData collisions(String key) {
            if(cache.containsKey(key))
                return cache.get(key);
            if(!collisions.containsKey(key))
                throw new IllegalStateException("Collision Box not found: " + key);
            CollisionData data = Utils.resources().COLLISION_DATA.load(collisions.get(key));
            cache.put(key, data);
            return data;
        }

    }
}
