package com.sprite.resource;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.sprite.data.JsonSerializable;
import com.sprite.data.registries.Registries;
import com.sprite.data.registries.Registry;
import org.json.JSONObject;

public record Resource<T extends Resource.Object>(Location location, FileHandle file, boolean internal) {

    public record Location(String namespace, String path) {

        @Override
        public String toString() {
            return namespace + ":" + path;
        }

        public FileHandle file() {
            String newPath = "assets/" + namespace + "/" + path;
            newPath = newPath.substring(0, newPath.lastIndexOf('/'));
            FileHandle fileHandle = Gdx.files.local(newPath);
            String fileNameWithoutExt = path.substring(path.lastIndexOf('/') + 1);

            for (FileHandle searchHandle : fileHandle.list()) {
                if (searchHandle.name().equals(fileNameWithoutExt)) {
                    return searchHandle;
                }
            }

            return Gdx.files.internal("assets/" + namespace + "/" + path);
        }
    }

    public abstract static class RegistryAccess<T extends Object> {


        public final Registry<T> registry = Registries.register(key(), () -> null);

        public abstract String key();

        public abstract T load(String location);

    }

    public static class Object implements JsonSerializable {

        @Override
        public String toString() {
            return json().toString(2);
        }
    }


}
