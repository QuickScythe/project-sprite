package com.sprite.resource;

import com.badlogic.gdx.files.FileHandle;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ResourceMeta {

    private final JSONObject meta;
    private final String path;
    private final Type type;

    public ResourceMeta(String path, FileHandle file) throws IOException {
        this.path = path;
        this.meta = new JSONObject(new String(file.read().readAllBytes(), StandardCharsets.UTF_8));
        if (!meta.has("type")) throw new RuntimeException("Resource meta does not have a type");
        type = meta.getEnum(Type.class, "type");
    }

    public ResourceMeta(String path, JSONObject defaultMeta) {
        this.path = path;
        meta = defaultMeta;
        if (!meta.has("type")) throw new RuntimeException("Resource meta does not have a type");
        type = meta.getEnum(Type.class, "type");
    }

    public <R> R get(String key, Class<R> type) {
        return (R) meta.get(key);
    }

    public JSONObject json() {
        return meta;
    }

    public String name() {
        return path;
    }

    @Override
    public String toString() {
        return json().toString();
    }

    public Type type() {
        return type;
    }

    public enum Type {
        TEXTURE,
        ANIMATION,
        SOUND,
        MUSIC,
        BACKGROUND,
        TEXT;
    }
}
