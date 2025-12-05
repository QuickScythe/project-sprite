package com.sprite.game.world.gen;

import org.json.JSONObject;

public class ChunkGeneratorFactory {
    public static ChunkGenerator create(JSONObject generator) {
        if (generator == null) {
            return new FlatBottomGenerator();
        }

        // Optional reflective path for full data-driven support
        if (generator.has("class")) {
            String cls = generator.getString("class");
            try {
                Class<?> c = Class.forName(cls);
                if (!ChunkGenerator.class.isAssignableFrom(c)) {
                    throw new IllegalArgumentException("Class does not implement ChunkGenerator: " + cls);
                }
                // Prefer constructor that accepts JSONObject
                try {
                    return (ChunkGenerator) c.getConstructor(JSONObject.class).newInstance(generator);
                } catch (NoSuchMethodException ignored) {
                    // Fallback to no-arg constructor
                    return (ChunkGenerator) c.getConstructor().newInstance();
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to instantiate ChunkGenerator class: " + cls, e);
            }
        }

        String type = generator.optString("type", "").toLowerCase();
        switch (type) {
            case "flat_bottom":
            case "flatbottom":
            case "flat":
                return new FlatBottomGenerator(generator);

            case "smooth_hills":
            case "smoothrollinghills":
            case "smooth_rolling_hills": {
                // Use JSON constructor to support both numeric schemas and palette
                return new SmoothRollingHillsGenerator(generator);
            }

            default:
                // Fallback to flat to avoid crashes if type missing/unknown
                return new FlatBottomGenerator(generator);
        }
    }
}
