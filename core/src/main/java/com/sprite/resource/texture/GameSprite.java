package com.sprite.resource.texture;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.sprite.resource.Resource;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collection;
import java.util.Map;

/**
 * Wrapper around a LibGDX {@link Sprite} with optional hitbox/hurtbox polygons.
 */
public class GameSprite extends Resource.Object {

    /** Optional map of polygons keyed by type. */
    Map<Polygon.Type, Polygon> polygons;
    /** Underlying LibGDX sprite. */
    public Sprite sprite;

    /**
     * Loads a texture from the given resource and wraps it in a Sprite.
     * @param resource texture resource (image file)
     */
    public GameSprite(Resource<?> resource) {
        sprite = new Sprite(new Texture(resource.file()));
    }

    /**
     * Creates a sprite from an existing texture region and polygons metadata.
     */
    public GameSprite(Texture texture, int x, int y, int width, int height, Map<Polygon.Type, Polygon> polygons) {
//            super(resource, i, i1, width, height);
        this.polygons = polygons;
        sprite = new Sprite(texture, x, y, width, height);
    }

    /**
     * Returns all polygons associated with this sprite.
     */
    public Collection<Polygon> polygons(){
        return polygons.values();
    }

    /** @return the underlying LibGDX Sprite. */
    public Sprite sprite() {
        return sprite;
    }

    /** @return the sprite width in pixels. */
    public int width() {
        return (int) sprite.getWidth();
    }

    /** @return the sprite height in pixels. */
    public int height() {
        return (int) sprite.getHeight();
    }

    /**
     * Polygon description for collision/interaction boxes.
     */
    public static class Polygon {

        private final Type type;
        private final float[] vertices;

        /**
         * Creates a polygon with a type and flat vertices array (x1,y1,x2,y2,...).
         */
        public Polygon(Type type, float[] vertices) {
            this.type = type;
            this.vertices = vertices;
        }

        /**
         * Builds a polygon from a JSON object with fields: type and points (float array).
         */
        public static Polygon of(JSONObject json) {
            Type type = Type.valueOf(json.getString("type").toUpperCase());
            JSONArray points = json.getJSONArray("points");
            float[] floats = new float[points.length()];
            for (int i = 0; i < points.length(); i++) {
                floats[i] = (float) points.getDouble(i);
            }
            return new Polygon(type, floats);
        }

        /** Types of polygons supported. */
        public enum Type {
            HIT_BOX,
            HURT_BOX,
            COLLISION_BOX, ATTACK_BOX
        }

    }
}
