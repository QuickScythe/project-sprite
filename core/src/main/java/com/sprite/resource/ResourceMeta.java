package com.sprite.resource;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import org.json.JSONObject;

/**
 * Lightweight wrapper interface providing typed access to decoded resource payloads.
 * @param <T> payload type (e.g., JSONObject or Texture)
 */
public interface ResourceMeta<T> {

    /**
     * Returns the decoded payload.
     * @return the underlying payload instance
     */
    T get();

    record Music(com.badlogic.gdx.audio.Music music) implements ResourceMeta<com.badlogic.gdx.audio.Music> {
        public Music(FileHandle file){
            this(Gdx.audio.newMusic(file));
        }
        @Override
        public com.badlogic.gdx.audio.Music get() {
            return music;
        }
    }

    /** Metadata wrapper for Sound resources. */
    record Sound(com.badlogic.gdx.audio.Sound sound) implements ResourceMeta<com.badlogic.gdx.audio.Sound> {

        public Sound(FileHandle file){
            this(Gdx.audio.newSound(file));
        }
        @Override
        public com.badlogic.gdx.audio.Sound get() {
            return sound;
        }
    }

    /** Metadata wrapper for JSON resources. */
    record Json(JSONObject json) implements ResourceMeta<JSONObject> {

        /**
         * Creates a Json wrapper by reading and parsing the provided file as UTF-8 JSON.
         * @param file file handle to read
         */
        public Json(FileHandle file) {
            this(new JSONObject(file.readString()));
        }

        @Override
        public JSONObject get() {
            return json;
        }
    }

    /** Metadata wrapper for Texture resources. */
    record Texture(com.badlogic.gdx.graphics.Texture texture) implements ResourceMeta<com.badlogic.gdx.graphics.Texture> {
            /**
             * Creates a Texture wrapper by loading the provided file as a LibGDX Texture.
             * @param file file handle to read
             */
            public Texture(FileHandle file) {
                this(new com.badlogic.gdx.graphics.Texture(file));
            }

            @Override
            public com.badlogic.gdx.graphics.Texture get() {
                return texture;
            }
        }

}
