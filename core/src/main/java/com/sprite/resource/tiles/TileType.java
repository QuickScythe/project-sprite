package com.sprite.resource.tiles;

import com.sprite.data.Taggable;
import com.sprite.data.TranslatableString;
import com.sprite.data.utils.Utils;
import com.sprite.data.utils.resources.Texts;
import com.sprite.resource.Resource;
import com.sprite.resource.ResourceMeta;
import com.sprite.resource.loot.LootTable;
import com.sprite.resource.texture.GameSprite;
import org.json.JSONArray;

public class TileType implements Taggable {

    private final Resource.Location location;
    private final GameSprite texture;
    private final float hardness;
    private final TranslatableString name;
    private final LootTable drops;
    private final String[] tags;

    public TileType(Resource resource) {
        if(!resource.type().equals(Resource.Type.DATA)) throw new IllegalStateException("Tile resource must be of type DATA");
        ResourceMeta.Json data = (ResourceMeta.Json) resource.data.data();
        texture = Utils.resources().TEXTURES.load(data.get().optString("texture", "textures:missing"));
        hardness = data.get().getFloat("hardness");
        name = new TranslatableString(data.get().getString("name"));
        drops = Utils.resources().LOOT.load(data.get().optString("drops", "loot:empty"));
        JSONArray tags = data.get().optJSONArray("tags");
        if(tags != null) {
            this.tags = new String[tags.length()];
            for(int i = 0; i < tags.length(); i++) {
                if(tags.get(i) instanceof String key) {
                    this.tags[i] = key;
                }
                else throw new IllegalStateException("Invalid tag type: " + tags.get(i));
            }
        } else {
            this.tags = new String[0];
        }
        this.location = resource.location();
    }

    public GameSprite texture() {
        return texture;
    }

    public Resource.Location resource() {
        return location;
    }

    public float hardness() {
        return hardness;
    }

    public String name() {
        return Texts.get(name);
    }

    public LootTable drops() {
        return drops;
    }

    @Override
    public String[] tags() {
        return this.tags;
    }
}
