package com.sprite.resource.models;

import com.sprite.resource.Resource;
import com.sprite.resource.ResourceMeta;
import com.sprite.resource.animations.Animation;
import com.sprite.resource.texture.GameSprite;
import com.sprite.utils.Utils;
import org.json.JSONObject;

/**
 * Immutable description of a renderable model, including its texture, size, and animations.
 */
public class Model implements Cloneable {

    /** Base texture for the model. */
    public final GameSprite texture;
    /** Animations configured for this model. */
    public final Animation[] animations;
    /** Width in pixels. */
    public final int width;
    /** Height in pixels. */
    public final int height;

    /**
     * Constructs a model by reading a JSON resource and resolving referenced assets.
     * Expected JSON format: {"texture":"textures:...","width":..,"height":..,"animations":{...}}
     * @param resource model JSON resource
     */
    public Model(Resource resource) {
        if(!resource.type().equals(Resource.Type.DATA)) throw new IllegalStateException("Controller resource must be of type DATA");
        ResourceMeta.Json data = (ResourceMeta.Json) resource.data.data();
        texture = Utils.resources().TEXTURES.load(data.get().optString("texture", "textures:missing"));
        width = data.get().optInt("width", texture.width());
        height = data.get().optInt("height", texture.height());
        JSONObject animationsData = data.get().optJSONObject("animations");
        animations = new Animation[animationsData.length()];
        int i = 0;
        for (String key : animationsData.keySet()) {
            animations[i] = Utils.resources().ANIMATIONS.load(animationsData.getString(key));
            i = i + 1;
        }
    }

    /**
     * Performs a shallow clone. Fields are immutable references in current design.
     */
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
