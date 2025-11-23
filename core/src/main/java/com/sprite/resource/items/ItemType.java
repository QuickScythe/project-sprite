package com.sprite.resource.items;

import com.sprite.data.utils.Utils;
import com.sprite.resource.Resource;
import com.sprite.resource.ResourceMeta;
import com.sprite.resource.texture.GameSprite;

public class ItemType {

    private final GameSprite texture;
    private final Resource.Location location;

    public ItemType(Resource resource) {
        if(!resource.type().equals(Resource.Type.DATA)) throw new IllegalStateException("Controller resource must be of type DATA");
        ResourceMeta.Json data = (ResourceMeta.Json) resource.data.data();
        texture = Utils.resources().TEXTURES.load(data.get().optString("texture", "textures:missing"));
        this.location = resource.location();
    }

    public GameSprite texture() {
        return texture;
    }

    public Resource.Location resource() {
        return location;
    }
}
