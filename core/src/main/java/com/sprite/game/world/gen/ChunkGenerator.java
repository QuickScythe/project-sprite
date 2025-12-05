package com.sprite.game.world.gen;

import com.sprite.game.world.tiles.TileChunk;
import org.json.JSONObject;

/**
 * Functional interface to generate the contents of a single 8x8 tile chunk.
 * Implementations should deterministically fill the provided chunk based on
 * the chunk coordinates so generation can be repeated on demand.
 */
public interface ChunkGenerator {
    void generate(int chunkX, int chunkY, TileChunk chunk);

    JSONObject data();
}
