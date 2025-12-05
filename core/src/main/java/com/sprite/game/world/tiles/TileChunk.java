package com.sprite.game.world.tiles;

/**
 * A fixed-size chunk of tiles. Each chunk is 8x8 tiles.
 * Each tile is represented by a Tile instance. The Tile holds an integer id
 * for palette/persistence and can cache a resolved TileType and metadata.
 * 0 = empty/air by convention; non-zero values can map to game-specific tiles.
 */
public class TileChunk {
    public static final int SIZE = 8;

    private final int chunkX;
    private final int chunkY;
    private final Tile[][] tiles = new Tile[SIZE][SIZE];

    public TileChunk(int chunkX, int chunkY) {
        this.chunkX = chunkX;
        this.chunkY = chunkY;
        // Initialize all tiles to air tiles
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                tiles[x][y] = new Tile(0);
            }
        }
    }

    public int chunkX() { return chunkX; }
    public int chunkY() { return chunkY; }

    /**
     * Backward-compatible integer id accessor.
     */
    public int get(int x, int y) { return tiles[x][y].id(); }

    /**
     * Backward-compatible integer id mutator.
     */
    public void set(int x, int y, int id) { tiles[x][y].id(id); }

    /**
     * Returns the Tile instance at local coordinates.
     */
    public Tile getTile(int x, int y) { return tiles[x][y]; }

    /**
     * Replaces the Tile instance at local coordinates. If null is provided, sets air.
     */
    public void setTile(int x, int y, Tile tile) { tiles[x][y] = tile == null ? new Tile(0) : tile; }

    public void fill(int id) {
        for (int x = 0; x < SIZE; x++)
            for (int y = 0; y < SIZE; y++)
                tiles[x][y].id(id);
    }
}
