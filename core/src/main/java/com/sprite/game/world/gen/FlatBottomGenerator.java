package com.sprite.game.world.gen;

import com.sprite.game.world.tiles.TileChunk;
import org.json.JSONObject;

/**
 * Generates a world with a single flat layer of tiles at y=0 (global).
 * All tiles with global y < 0 are empty, and y == 0 are filled (id=1).
 */
public class FlatBottomGenerator implements ChunkGenerator {

    /**
     * Mapping from tile id -> TileType resource location (e.g., "tiles:grass").
     * 0 is implicitly air and typically omitted.
     */
    private final java.util.Map<Integer, String> palette = new java.util.HashMap<>();

    /** Default constructor with legacy palette (1=grass, 2=dirt, 3=stone). */
    public FlatBottomGenerator() {
        palette.put(1, "tiles:grass");
        palette.put(2, "tiles:dirt");
        palette.put(3, "tiles:stone");
    }

    /** JSON-driven constructor allowing modders to override the palette. */
    public FlatBottomGenerator(JSONObject json) {
        this();
        if (json != null && json.has("palette")) {
            JSONObject pal = json.optJSONObject("palette");
            if (pal != null) {
                for (String key : pal.keySet()) {
                    try {
                        int id = Integer.parseInt(key);
                        String loc = pal.optString(key, null);
                        if (loc != null && !loc.isEmpty()) {
                            palette.put(id, loc);
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
    }

    @Override
    public void generate(int chunkX, int chunkY, TileChunk chunk) {
        System.out.println("FlatBottomGenerator");

        // Global Y=0 corresponds to local tile index y==0 in chunks where chunkY==0
        for (int x = 0; x < TileChunk.SIZE; x++) {
            for (int y = 0; y < TileChunk.SIZE; y++) {
                int globalY = chunkY * TileChunk.SIZE + y;
                // Fill exactly the bottom layer at global y==0
                if (globalY == 0) {
                    chunk.set(x, y, 1);
                } else {
                    chunk.set(x, y, 0);
                }
            }
        }
    }

    @Override
    public JSONObject data() {
        JSONObject json = new JSONObject();
        json.put("type", "flat_bottom");
        // serialize palette
        JSONObject pal = new JSONObject();
        for (java.util.Map.Entry<Integer, String> e : palette.entrySet()) {
            pal.put(Integer.toString(e.getKey()), e.getValue());
        }
        json.put("palette", pal);
        return json;
    }
}
