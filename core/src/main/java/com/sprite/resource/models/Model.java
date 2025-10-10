package com.sprite.resource.models;

import com.sprite.resource.Resource;
import com.sprite.resource.animations.Animation;
import com.sprite.resource.texture.GameSprite;
import com.sprite.utils.Utils;
import org.json.JSONObject;

public class Model extends Resource.Object implements Cloneable {


    public final GameSprite texture;
    public final Animation[] animations;
    public final int width;
    public final int height;

    public Model(Resource<?> resource) {
        String read = resource.file().readString();
        JSONObject data = new JSONObject(read);
        texture = Utils.resources().TEXTURES.load(data.optString("texture", "textures:missing"));
        width = data.optInt("width", texture.width());
        height = data.optInt("height", texture.height());
        JSONObject animationsData = data.optJSONObject("animations");
        animations = new Animation[animationsData.length()];
        int i = 0;
        for (String key : animationsData.keySet()) {
            animations[i] = Utils.resources().ANIMATIONS.load(animationsData.getString(key));
            i = i + 1;
        }
    }

    @Override
    public Model clone() {
        try {
            Model clone = (Model) super.clone();
            // TODO: copy mutable state here, so the clone can't change the internals of the original
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
