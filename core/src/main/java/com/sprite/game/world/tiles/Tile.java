package com.sprite.game.world.tiles;

import com.sprite.resource.tiles.TileType;
import org.json.JSONObject;

/**
 * Represents a single tile instance in the world. A Tile has an integer id
 * (used by generators, persistence, and palettes) and can optionally hold a
 * resolved TileType reference along with per-instance metadata for future use.
 */
public class Tile {

    /**
     * Integer id used for palette lookup and persistence. 0 = air by convention.
     */
    private int id;

    /**
     * Cached resolved type for rendering/behavior; may be null if not yet resolved.
     */
    private TileType type;

    /**
     * Optional per-instance metadata for modders/gameplay systems.
     */
    private JSONObject metadata;

    public Tile() {
        this(0, null);
    }

    public Tile(int id) {
        this(id, null);
    }

    public Tile(int id, TileType type) {
        this.id = id;
        this.type = type;
        this.metadata = new JSONObject();
    }

    public int id() {
        return id;
    }

    /**
     * Sets the tile id and clears any cached type so it can be re-resolved.
     */
    public void id(int id) {
        this.id = id;
        // Invalidate cached type when id changes; palette lookup can restore it.
        this.type = null;
    }

    public TileType type() {
        return type;
    }

    public void type(TileType type) {
        this.type = type;
    }

    public JSONObject metadata() {
        if (metadata == null) metadata = new JSONObject();
        return metadata;
    }

    public void metadata(JSONObject metadata) {
        this.metadata = metadata == null ? new JSONObject() : metadata;
    }

    public boolean isAir() {
        return id == 0;
    }
}
