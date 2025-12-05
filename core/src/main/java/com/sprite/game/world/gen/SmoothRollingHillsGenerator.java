package com.sprite.game.world.gen;

import com.sprite.game.world.tiles.TileChunk;
import org.json.JSONObject;

/**
 * Generates rolling terrain using deterministic fractal value noise (fBm).
 * Compared to pure sine waves, this adds natural-looking randomness while
 * remaining repeatable across chunks and runs (for a given seed).
 */
public class SmoothRollingHillsGenerator implements ChunkGenerator {

    // Terrain shaping params
    private final float verticalScaleTiles;   // overall height scale in tiles
    private final float verticalOffset;       // shifts terrain up/down in tiles

    // Fractal noise params
    private final long seed;
    private final int octaves;
    private final double baseFrequency;       // base frequency in cycles per tile
    private final double lacunarity;          // frequency multiplier per octave
    private final double persistence;         // amplitude multiplier per octave

    /**
     * Mapping from tile id -> TileType resource location (e.g., "tiles:grass").
     * 0 is air by convention.
     */
    private final java.util.Map<Integer, String> palette = new java.util.HashMap<>();

    /**
     * Backwards-compat constructor keeping previous signature. The wavelength is
     * mapped to a base frequency (1 / wavelength). The result is then scaled by
     * amplitude and shifted by verticalOffset.
     */
    public SmoothRollingHillsGenerator(float amplitude, float wavelength, float verticalOffset) {
        this(amplitude,
                /*seed*/ 1337L,
                /*octaves*/ 4,
                /*baseFrequency*/ 1.0 / Math.max(1.0, wavelength),
                /*lacunarity*/ 2.0,
                /*persistence*/ 0.5,
                verticalOffset);
    }

    /** Default pleasant hills with subtle randomness. */
    public SmoothRollingHillsGenerator() {
        this(6f, 1337L, 4, 1.0 / 48.0, 2.0, 0.5, -4f);
        // default palette matching legacy ids
        palette.put(1, "tiles:grass");
        palette.put(2, "tiles:dirt");
        palette.put(3, "tiles:stone");
    }

    /** Full-parameter constructor for advanced control. */
    public SmoothRollingHillsGenerator(
            float amplitudeTiles,
            long seed,
            int octaves,
            double baseFrequency,
            double lacunarity,
            double persistence,
            float verticalOffsetTiles) {
        this.verticalScaleTiles = amplitudeTiles;
        this.verticalOffset = verticalOffsetTiles;
        this.seed = seed;
        this.octaves = Math.max(1, octaves);
        this.baseFrequency = Math.max(1e-6, baseFrequency);
        this.lacunarity = Math.max(1.0, lacunarity);
        this.persistence = Math.max(0.0, Math.min(1.0, persistence));
        // default palette matching legacy ids
        palette.put(1, "tiles:grass");
        palette.put(2, "tiles:dirt");
        palette.put(3, "tiles:stone");
    }

    /** JSON-driven constructor that supports both numeric params and a tile palette. */
    public SmoothRollingHillsGenerator(JSONObject generator) {
        this(
                (float) generator.optDouble("amplitude", 6.0),
                generator.has("seed") ? generator.getLong("seed") : 1337L,
                generator.has("octaves") ? generator.getInt("octaves") : 4,
                generator.has("baseFrequency") ? generator.getDouble("baseFrequency")
                        : (generator.has("wavelength") ? (1.0 / Math.max(1.0, generator.optDouble("wavelength", 48.0))) : 1.0 / 48.0),
                generator.has("lacunarity") ? generator.getDouble("lacunarity") : 2.0,
                generator.has("persistence") ? generator.getDouble("persistence") : 0.5,
                (float) generator.optDouble("verticalOffset", -4.0)
        );
        // Override palette if provided
        JSONObject pal = generator.optJSONObject("palette");
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

    @Override
    public void generate(int chunkX, int chunkY, TileChunk chunk) {
        for (int lx = 0; lx < TileChunk.SIZE; lx++) {
            int gx = chunkX * TileChunk.SIZE + lx; // global tile x

            // fBm value noise in [-1, 1] approximately
            double n = fbm(gx);

            // Map noise to tile height: scale and offset
            double h = n * verticalScaleTiles + verticalOffset;
            int height = (int) Math.floor(h);

            // Small deterministic jitter for how deep dirt extends before stone begins
            // Range 0..2 tiles to keep variation subtle
            int stoneDepthJitter = (int) ((mix(seed, gx * 1315423911L) >>> 61) & 0x3L); // 0..3
            if (stoneDepthJitter > 2) stoneDepthJitter = 2; // clamp to 0..2
            int maxDirtDepth = 4 + stoneDepthJitter;

            for (int ly = 0; ly < TileChunk.SIZE; ly++) {
                int gy = chunkY * TileChunk.SIZE + ly; // global tile y
                if (gy == height) {
                    // Top surface: grass
                    chunk.set(lx, ly, 1);
                } else if (gy < height) {
                    // Below surface: 1-4 layers of dirt, then stone
                    int depth = height - gy; // 1 at just below top
                    if (depth >= 1 && depth <= maxDirtDepth) {
                        chunk.set(lx, ly, 2); // dirt
                    } else {
                        chunk.set(lx, ly, 3); // stone
                    }
                } else {
                    // Air above terrain
                    chunk.set(lx, ly, 0);
                }
            }
        }
    }

    @Override
    public JSONObject data() {
        JSONObject json = new JSONObject();
        json.put("type", "smooth_hills");
        json.put("amplitude", verticalScaleTiles);
        json.put("verticalOffset", verticalOffset);
        json.put("seed", seed);
        json.put("octaves", octaves);
        json.put("baseFrequency", baseFrequency);
        json.put("lacunarity", lacunarity);
        json.put("persistence", persistence);
        // serialize palette
        JSONObject pal = new JSONObject();
        for (java.util.Map.Entry<Integer, String> e : palette.entrySet()) {
            pal.put(Integer.toString(e.getKey()), e.getValue());
        }
        json.put("palette", pal);
        return json;
    }

    // --- Noise implementation (deterministic value noise + fBm) ---

    private double fbm(double x) {
        double freq = baseFrequency;
        double amp = 1.0;
        double sum = 0.0;
        double ampSum = 0.0;
        for (int o = 0; o < octaves; o++) {
            sum += amp * noise1D(x * freq);
            ampSum += amp;
            freq *= lacunarity;
            amp *= persistence;
        }
        // Normalize to [-1,1] by dividing by max possible amplitude (ampSum)
        if (ampSum > 1e-9) sum /= ampSum;
        return sum; // already roughly in [-1,1]
    }

    private double noise1D(double x) {
        int xi = (int) Math.floor(x);
        double t = x - xi;
        double a = valueAt(xi);
        double b = valueAt(xi + 1);
        double u = fade(t);
        return lerp(a, b, u); // returns in [-1,1]
    }

    // Smoothstep-like fade (Perlin's 6t^5 - 15t^4 + 10t^3)
    private static double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    private static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    // Deterministic pseudo-random value in [-1,1] for integer coordinate
    private double valueAt(int x) {
        long h = mix(seed, x);
        // Map to [0,1)
        double d = ((h >>> 11) & 0x1fffffL) / (double) (1 << 21); // 21-bit fraction
        return d * 2.0 - 1.0; // [-1,1)
    }

    // 64-bit mix based on splitmix64-like steps for good bit diffusion
    private static long mix(long seed, long x) {
        long z = seed + 0x9E3779B97F4A7C15L * (x + 0xD1B54A32D192ED03L);
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        z = z ^ (z >>> 31);
        return z;
    }
}
