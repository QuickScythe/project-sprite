package com.sprite.resource.animations;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.sprite.resource.Resource;
import com.sprite.resource.models.Model;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Animation extends Resource.Object {

    public final int frames;
    public final int speed;
    public final JSONObject frameData;
    public final FrameData[] frameArray;
    public final int index;
    public final String name;

    int frame = 0;
    long update = 0;

    public Animation(Resource<?> resource) {
        String read = resource.file().readString();
        JSONObject data = new JSONObject(read);
        this.frameData = data.getJSONObject("frame_data");
        this.frames = data.getInt("frames");
        this.speed = data.getInt("speed");
        this.index = data.getInt("index");
        this.name = data.optString("name", resource.location().path());

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
                            frameArray[i].collisions.add(value);
                        }
                        continue;
                    }
                    int index = Integer.parseInt(frameKey);
                    if (index < 0 || index >= frames) continue;
                    frameArray[index].collisions.add(value);
                }
            }
        }


    }

    public TextureRegion frame(Model model) {
        if (new Date().getTime() - update >= speed * 1) {
            update = new Date().getTime();
            frame = frame + 1;
            if (frame >= frames) {
                frame = 0;
            }
        }
        return new TextureRegion(model.texture.sprite, frame * model.width, index * model.height, model.width, model.height);


    }

    public static class FrameData {

        public final List<String> collisions = new ArrayList<>();

        public final int index;

        public FrameData(int index) {
            this.index = index;
        }

    }
}
