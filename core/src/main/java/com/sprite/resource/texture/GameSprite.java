package com.sprite.resource.texture;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.sprite.resource.Resource;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collection;
import java.util.Map;

public class GameSprite extends Resource.Object {

    Map<Polygon.Type, Polygon> polygons;
    public Sprite sprite;

    public GameSprite(Resource<?> resource) {
        sprite = new Sprite(new Texture(resource.file()));
    }

    public GameSprite(Texture texture, int x, int y, int width, int height, Map<Polygon.Type, Polygon> polygons) {
//            super(resource, i, i1, width, height);
        this.polygons = polygons;
        sprite = new Sprite(texture, x, y, width, height);
    }

    public Collection<Polygon> polygons(){
        return polygons.values();
    }

    public Sprite sprite() {
        return sprite;
    }

    public int width() {
        return (int) sprite.getWidth();
    }

    public int height() {
        return (int) sprite.getHeight();
    }

    public static class Polygon {

        private final Type type;
        private final float[] vertices;

        public Polygon(Type type, float[] vertices) {
            this.type = type;
            this.vertices = vertices;
        }

        public static Polygon of(JSONObject json) {
            Type type = Type.valueOf(json.getString("type").toUpperCase());
            JSONArray points = json.getJSONArray("points");
            float[] floats = new float[points.length()];
            for (int i = 0; i < points.length(); i++) {
                floats[i] = (float) points.getDouble(i);
            }
            return new Polygon(type, floats);
        }

        public enum Type {
            HIT_BOX,
            HURT_BOX,
            COLLISION_BOX, ATTACK_BOX
        }


    }
}
