package com.sprite.game.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.sprite.data.utils.Utils;
import com.sprite.game.world.entities.Entity;
import com.sprite.game.world.gen.ChunkGenerator;
import com.sprite.game.world.gen.ChunkGeneratorFactory;
import com.sprite.game.world.gen.FlatBottomGenerator;
import com.sprite.game.world.tiles.Tile;
import com.sprite.game.world.tiles.TileChunk;
import com.sprite.render.camera.GameCamera;
import com.sprite.render.screen.GameScreen;
import com.sprite.resource.entities.EntityType;
import com.sprite.resource.tiles.TileType;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

public class World {


    public final List<Entity> entities = new ArrayList<>();

    private final Map<Long, TileChunk> chunks = new HashMap<>();
    private final ChunkGenerator generator;
    private final int tileSize; // world units per tile
    private final long seed;
    private final String name;
    // Physics parameters
    private final float gravity;
    private final float linearDamping;
    private final float groundFriction;
    private final float restitution;
    private final float worldFloorY;
    // Data-driven palette: tile id -> TileType resolved from generator data
    private final Map<Integer, TileType> tilePalette = new HashMap<>();
    // Persistence configuration
    private String saveDirectory = null; // if set, chunks will be saved/loaded to/from this directory (local storage)
    // Loading state: becomes true once a 5x5 chunk radius around the camera is cached
    private boolean loaded = false;
    private boolean step = false;

    public World(Builder builder) {
        // Default to a pleasant rolling hills demo; caller can switch generators later
        this.generator = builder.generator();
        this.seed = builder.seed();
        this.name = builder.name();
        this.tileSize = builder.tileSize();
        this.gravity = builder.gravity();
        this.linearDamping = builder.linearDamping();
        this.groundFriction = builder.groundFriction();
        this.restitution = builder.restitution();
        this.worldFloorY = builder.floor();
        setSaveDirectory("saves/" + builder.name());

        JSONObject data = new JSONObject();
        data.put("seed", seed)
            .put("name", name)
            .put("tileSize", tileSize)
            .put("gravity", gravity)
            .put("linearDamping", linearDamping)
            .put("groundFriction", groundFriction)
            .put("restitution", restitution)
            .put("worldFloorY", worldFloorY)
            .put("generator", generator.data());

        FileHandle options = Gdx.files.local(saveDirectory + "/options.json");
        options.writeString(data.toString(), false);

    }

    public World(JSONObject data) {
        this.seed = data.getLong("seed");
        this.name = data.getString("name");
        this.tileSize = data.getInt("tileSize");
        this.gravity = data.getFloat("gravity");
        this.linearDamping = data.getFloat("linearDamping");
        this.groundFriction = data.getFloat("groundFriction");
        this.restitution = data.getFloat("restitution");
        this.worldFloorY = data.getInt("worldFloorY");
        this.generator = ChunkGeneratorFactory.create(data.getJSONObject("generator"));
        setSaveDirectory("saves/" + name);
    }

    private static long packKey(int x, int y) {
        return (((long) x) << 32) ^ (y & 0xffffffffL);
    }

    private static int floorDiv(int a, int b) {
        int r = a / b;
        // adjust for negative toward -inf
        if ((a ^ b) < 0 && (r * b != a)) r--;
        return r;
    }

    /**
     * Positive modulus that works with negative a values (0..b-1).
     */
    private static int mod(int a, int b) {
        int m = a % b;
        if (m < 0) m += b;
        return m;
    }

    public Entity spawn(EntityType type, int x, int y) {
        Entity entity = new Entity(type, x, y);
        entities.add(entity);
        return entity;
    }

    /**
     * Returns true once a 5x5 chunk radius has been loaded into cache at least once.
     */
    public boolean isLoaded() {
        return loaded;
    }

    /**
     * Enable or change the chunk save directory at runtime.
     */
    public void enableChunkPersistence(String directory) {
        this.setSaveDirectory(directory);
    }

    public float gravity() {
        return gravity;
    }

    public float floor() {
        return worldFloorY;
    }

    public float linearDamping() {
        return linearDamping;
    }

    public float groundFriction() {
        return groundFriction;
    }

    public float restitution() {
        return restitution;
    }

    private void step(float dt) {
        if (dt <= 0) return;
        for (Entity e : entities) {
            e.update(this, dt);

        }
    }

    public void render(GameScreen screen) {
        // Use a fixed per-frame step to remove dependency on real-time delta
        if (step) {
            step(1f);
        }
        if (loaded) {
            step = true;
            // Step physics before rendering this frame

        } else step(0);
//
//        step(1);
        // Ensure a 5x5 chunk area (radius 2) around the camera center is present in cache,
        // and flip the loaded flag once this condition is met at least once.
        int ts = tileSize();
        int tx = (int) Math.floor(screen.camera().position.x / ts);
        int ty = (int) Math.floor(screen.camera().position.y / ts);
        int cx = Math.floorDiv(tx, TileChunk.SIZE);
        int cy = Math.floorDiv(ty, TileChunk.SIZE);
        int radius = 2; // 5x5
        // Preload ensures presence before render potentially evicts off-screen chunks
        preloadSquare(cx, cy, radius);
        if (!loaded && hasSquareLoaded(cx, cy, radius)) {
            loaded = true;
        }

        // Draw generated tile chunks first (background terrain)
        render(screen.camera(), screen.sprite());

        for (Entity entity : entities) {
            entity.render(screen);
        }
    }

    public int tileSize() {
        return tileSize;
    }

    public FileHandle getSaveDirectory() {
        return Gdx.files.local(saveDirectory);
    }

    /**
     * Enables on-disk persistence for chunks. If a directory path is provided, TileWorld will
     * try to load chunks from disk before generating and will save and unload chunks that are
     * no longer needed in memory. Uses Gdx.files.local(saveDirectory).
     */
    public void setSaveDirectory(String saveDirectory) {
        this.saveDirectory = saveDirectory;
        // Ensure directory exists
        if (saveDirectory != null) {
            try {
                FileHandle dir = Gdx.files.local(saveDirectory);
                if (!dir.exists()) dir.mkdirs();
            } catch (Throwable ignored) {
            }
        }
    }

    public TileChunk getOrCreate(int chunkX, int chunkY) {
        long key = packKey(chunkX, chunkY);
        TileChunk chunk = chunks.get(key);
        if (chunk == null) {
            // Try to load from disk first if persistence is enabled
            if (saveDirectory != null) {
                TileChunk loaded = loadChunk(chunkX, chunkY);
                if (loaded != null) {
                    chunks.put(key, loaded);
                    return loaded;
                }
            }
            // Otherwise generate new
            chunk = new TileChunk(chunkX, chunkY);
            if (generator != null) generator.generate(chunkX, chunkY, chunk);
            chunks.put(key, chunk);
        }
        return chunk;
    }

    /**
     * Returns the tile id at tile coordinates (tx, ty), generating the chunk if necessary.
     */
    public int getTileId(int tx, int ty) {
        int cx = floorDiv(tx, TileChunk.SIZE);
        int cy = floorDiv(ty, TileChunk.SIZE);
        TileChunk chunk = getOrCreate(cx, cy);
        int lx = mod(tx, TileChunk.SIZE);
        int ly = mod(ty, TileChunk.SIZE);
        return chunk.get(lx, ly);
    }

    /**
     * Returns the Tile instance at tile coordinates (tx, ty), generating the chunk if necessary.
     */
    public Tile getTile(int tx, int ty) {
        int cx = floorDiv(tx, TileChunk.SIZE);
        int cy = floorDiv(ty, TileChunk.SIZE);
        TileChunk chunk = getOrCreate(cx, cy);
        int lx = mod(tx, TileChunk.SIZE);
        int ly = mod(ty, TileChunk.SIZE);
        return chunk.getTile(lx, ly);
    }

    /**
     * Returns the tile id at world-space coordinates (wx, wy).
     */
    public int getTileIdAtWorld(float wx, float wy) {
        int ts = tileSize;
        int tx = (int) Math.floor(wx / ts);
        int ty = (int) Math.floor(wy / ts);
        return getTileId(tx, ty);
    }

    /**
     * Returns the Tile instance at world-space coordinates (wx, wy).
     */
    public Tile getTileAtWorld(float wx, float wy) {
        int ts = tileSize;
        int tx = (int) Math.floor(wx / ts);
        int ty = (int) Math.floor(wy / ts);
        return getTile(tx, ty);
    }

    /**
     * True if the tile id corresponds to a solid/collidable tile.
     */
    public boolean isSolid(int id) {
        return id != 0; // by convention, 0 is air
    }

    /**
     * True if the world-space position is inside a solid tile.
     */
    public boolean isSolidAtWorld(float wx, float wy) {
        return isSolid(getTileIdAtWorld(wx, wy));
    }

    /**
     * Draws tiles for chunks inside the current camera view with a small margin.
     */
    public void render(GameCamera camera, SpriteBatch batch) {
        ensurePalette();

        float left = camera.position.x - camera.viewportWidth / 2f;
        float right = camera.position.x + camera.viewportWidth / 2f;
        float bottom = camera.position.y - camera.viewportHeight / 2f;
        float top = camera.position.y + camera.viewportHeight / 2f;

        int ts = tileSize;
        int minTileX = (int) Math.floor(left / ts) - TileChunk.SIZE;
        int maxTileX = (int) Math.ceil(right / ts) + TileChunk.SIZE;
        int minTileY = (int) Math.floor(bottom / ts) - TileChunk.SIZE;
        int maxTileY = (int) Math.ceil(top / ts) + TileChunk.SIZE;

        int minChunkX = floorDiv(minTileX, TileChunk.SIZE);
        int maxChunkX = floorDiv(maxTileX, TileChunk.SIZE);
        int minChunkY = floorDiv(minTileY, TileChunk.SIZE);
        int maxChunkY = floorDiv(maxTileY, TileChunk.SIZE);

        // Track which chunks should be retained in memory after this render call
        Set<Long> retain = new HashSet<>();
        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cy = minChunkY; cy <= maxChunkY; cy++) {
                TileChunk chunk = getOrCreate(cx, cy);
                drawChunk(chunk, batch, ts);
                retain.add(packKey(cx, cy));
            }
        }

        // Persist and evict chunks that are not in the visible set
        if (saveDirectory != null && !chunks.isEmpty()) {
            // Copy keys to avoid concurrent modification
            Set<Long> keys = new HashSet<>(chunks.keySet());
            for (Long key : keys) {
                if (!retain.contains(key)) {
                    persistAndRemove(key);
                }
            }
        }
    }

    /**
     * Preloads (generates or loads from disk) all chunks within a square radius around the
     * provided center chunk coordinates. This only ensures they are present in the in-memory
     * cache at the time of the call; subsequent renders with persistence enabled may evict
     * non-visible chunks again. Intended for bootstrapping/loading checks.
     *
     * @param centerCx center chunk x
     * @param centerCy center chunk y
     * @param radius   number of chunks outward in each axis (radius 2 -> 5x5 square)
     */
    public void preloadSquare(int centerCx, int centerCy, int radius) {
        if (radius < 0) return;
        for (int cx = centerCx - radius; cx <= centerCx + radius; cx++) {
            for (int cy = centerCy - radius; cy <= centerCy + radius; cy++) {
                getOrCreate(cx, cy);
            }
        }
    }

    /**
     * Returns true if all chunks in the given square radius around center are present in
     * the in-memory cache. Does not attempt to load missing chunks.
     */
    public boolean hasSquareLoaded(int centerCx, int centerCy, int radius) {
        if (radius < 0) return true;
        for (int cx = centerCx - radius; cx <= centerCx + radius; cx++) {
            for (int cy = centerCy - radius; cy <= centerCy + radius; cy++) {
                long key = packKey(cx, cy);
                if (!chunks.containsKey(key)) return false;
            }
        }
        return true;
    }

    private void drawChunk(TileChunk chunk, SpriteBatch batch, int ts) {
        int baseX = chunk.chunkX() * TileChunk.SIZE * ts;
        int baseY = chunk.chunkY() * TileChunk.SIZE * ts;
        for (int x = 0; x < TileChunk.SIZE; x++) {
            for (int y = 0; y < TileChunk.SIZE; y++) {
                Tile tile = chunk.getTile(x, y);
                int id = tile.id();
                if (id == 0) continue; // air
                // Resolve and cache the TileType if missing
                TileType type = tile.type();
                if (type == null) {
                    type = tilePalette.get(id);
                    if (type != null) tile.type(type);
                }
                if (type != null && type.texture() != null) {
                    batch.draw(type.texture().sprite(), baseX + x * ts, baseY + y * ts, ts, ts);
                }
            }
        }
    }

    private void ensurePalette() {
        // If already populated, nothing to do
        if (!tilePalette.isEmpty()) return;
        try {
            JSONObject data = generator.data();
            if (data != null && data.has("palette")) {
                JSONObject pal = data.optJSONObject("palette");
                if (pal != null) {
                    for (String key : pal.keySet()) {
                        try {
                            int id = Integer.parseInt(key);
                            String loc = pal.optString(key, null);
                            if (loc != null && !loc.isEmpty()) {
                                try {
                                    TileType type = Utils.resources().TILES.load(loc);
                                    tilePalette.put(id, type);
                                } catch (Throwable t) {
                                    // Fallback to test tile if load fails
                                    try {
                                        TileType fallback = Utils.resources().TILES.load("tiles:test");
                                        tilePalette.put(id, fallback);
                                    } catch (Throwable ignored) {
                                    }
                                }
                            }
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
            }
            // As a legacy fallback, if palette is still empty, populate defaults
            if (tilePalette.isEmpty()) {
                try { tilePalette.put(1, Utils.resources().TILES.load("tiles:grass")); } catch (Throwable ignored) {}
                try { tilePalette.put(2, Utils.resources().TILES.load("tiles:dirt")); } catch (Throwable ignored) {}
                try { tilePalette.put(3, Utils.resources().TILES.load("tiles:stone")); } catch (Throwable ignored) {}
            }
        } catch (Throwable ignored) {
        }
    }

    // -------------------- Persistence helpers --------------------

    private void persistAndRemove(Long key) {
        TileChunk chunk = chunks.get(key);
        if (chunk == null) return;
        try {
            saveChunk(chunk);
        } catch (Throwable ignored) {
        }
        chunks.remove(key);
    }

    private FileHandle chunkFile(int cx, int cy) {
        String name = cx + "_" + cy + ".bin";
        return Gdx.files.local(saveDirectory + "/chunks/" + name);
    }

    private void saveChunk(TileChunk chunk) {
        if (saveDirectory == null) return;
        FileHandle fh = chunkFile(chunk.chunkX(), chunk.chunkY());
        DataOutputStream out = null;
        try {
            out = new DataOutputStream(fh.write(false));
            // Simple header
            out.writeInt(0x54434831); // 'TCH1'
            out.writeInt(1); // version
            out.writeInt(chunk.chunkX());
            out.writeInt(chunk.chunkY());
            // Tiles, x-major then y
            for (int x = 0; x < TileChunk.SIZE; x++) {
                for (int y = 0; y < TileChunk.SIZE; y++) {
                    out.writeInt(chunk.get(x, y));
                }
            }
        } catch (IOException ignored) {
        } finally {
            if (out != null) try {
                out.close();
            } catch (IOException ignored) {
            }
        }
    }

    private TileChunk loadChunk(int cx, int cy) {
        if (saveDirectory == null) return null;
        FileHandle fh = chunkFile(cx, cy);
        if (!fh.exists()) return null;
        DataInputStream in = null;
        try {
            in = new DataInputStream(fh.read());
            int magic = in.readInt();
            int version = in.readInt();
            int fileCx = in.readInt();
            int fileCy = in.readInt();
            if (magic != 0x54434831 || version != 1 || fileCx != cx || fileCy != cy) {
                return null; // invalid
            }
            TileChunk chunk = new TileChunk(cx, cy);
            for (int x = 0; x < TileChunk.SIZE; x++) {
                for (int y = 0; y < TileChunk.SIZE; y++) {
                    int id = in.readInt();
                    chunk.set(x, y, id);
                }
            }
            return chunk;
        } catch (IOException ignored) {
            return null;
        } finally {
            if (in != null) try {
                in.close();
            } catch (IOException ignored) {
            }
        }
    }

    public static class Builder {

        String name;
        private long seed;
        private ChunkGenerator generator = new FlatBottomGenerator();
        private int tileSize = 128;

        private float gravity = -0.75f; // units per sec^2 (stronger gravity for snappier feel)
        private float linearDamping = 1; // air drag each step (per-frame factor)
        private float groundFriction = 0.85f; // applied to vx when on ground
        private float restitution = 0.0f; // bounce factor on ground
        private float worldFloorY = 0f; // simple ground plane at y=0

        public Builder(String name) {
            this.name = name;
            this.seed = System.currentTimeMillis() * new Random().nextInt();
        }

        public Builder generator(ChunkGenerator generator) {
            this.generator = generator;
            return this;
        }

        public Builder tileSize(int tileSize) {
            this.tileSize = tileSize;
            return this;
        }

        public Builder gravity(float gravity) {
            this.gravity = gravity;
            return this;
        }

        public Builder floor(float floor) {
            this.worldFloorY = floor;
            return this;
        }

        public World build() {
            return new World(this);
        }

        public ChunkGenerator generator() {
            return generator;
        }

        public int tileSize() {
            return tileSize;
        }

        public float gravity() {
            return gravity;
        }

        public float floor() {
            return worldFloorY;
        }

        public float linearDamping() {
            return linearDamping;
        }

        public float groundFriction() {
            return groundFriction;
        }

        public float restitution() {
            return restitution;
        }

        public long seed() {
            return seed;
        }

        public String name() {
            return name;
        }
    }


}
