package com.sprite.resource;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.sprite.data.registries.Registries;
import com.sprite.data.registries.Registry;

import java.util.Collection;
import java.util.Locale;

import static com.sprite.resource.Resource.Type.DATA;

/**
 * Represents a resource located under the game assets, such as JSON data files or textures.
 * <p>
 * A Resource couples a logical {@link Location} (namespace:path) with the physical {@link FileHandle}
 * and provides lazy decoding via {@link ResourceMeta} wrappers for JSON and Texture types.
 */
public class Resource {

    /**
     * Logical resource location (namespace and path without extension).
     */
    public final Location location;
    /**
     * Physical file backing this resource.
     */
    public final FileHandle file;
    /**
     * Whether this resource originates from internal classpath assets.
     */
    public final boolean internal;
    /**
     * Decoded data wrapper associated with this resource.
     */
    public Data data;

    /**
     * Creates a new Resource.
     *
     * @param location logical namespace:path identifier
     * @param file     the backing file handle
     * @param internal true if sourced from internal classpath, false for external/local assets
     */
    public Resource(Location location, FileHandle file, boolean internal) {
        this.location = location;
        this.internal = internal;
        this.file = file;
        switch (type()) {
            case TEXTURE -> {
                if (!file.extension().equals("png"))
                    throw new IllegalArgumentException("Texture file must be a PNG imag: " + file.path());
                this.data = new Data(type(), new ResourceMeta.Texture(file));
            }
            case DATA -> {
                if (!file.extension().equals("json"))
                    throw new IllegalArgumentException("JSON file must be a JSON file: " + file.path());
                this.data = new Data(type(), new ResourceMeta.Json(file));
            }
            case SOUND -> {
                if (!file.extension().equals("wav"))
                    throw new IllegalArgumentException("Sound file must be a WAV: " + file.path());
                this.data = new Data(type(), new ResourceMeta.Sound(file));
            }
        }
        switch (type()) {
            case TEXTURE:
                this.data = new Data(type(), new ResourceMeta.Texture(file));
                break;

            case DATA:
                this.data = new Data(type(), new ResourceMeta.Json(file));
                break;

            case SOUND:
                this.data = new Data(type(), new ResourceMeta.Sound(file));
                break;

        }

    }

    /**
     * Returns the logical location of this resource.
     *
     * @return location record
     */
    public Location location() {
        return location;
    }

    /**
     * Returns the file handle for this resource.
     *
     * @return file handle
     */
    public FileHandle file() {
        return file;
    }

    /**
     * Indicates whether this resource is internal.
     *
     * @return true if classpath/internal, false if external/local
     */
    public boolean isInternal() {
        return internal;
    }

    /**
     * Detects the resource type based on the file extension.
     *
     * @return DATA for .json files, otherwise TEXTURE
     */
    public Type type() {
        return switch (file.extension().toLowerCase(Locale.ENGLISH)) {
            case ("json") -> DATA;
            case ("wav") -> Type.SOUND;
            case ("mp3") -> Type.MUSIC;
            default -> Type.TEXTURE;
        };
    }

    /**
     * Overrides the decoded data wrapper for this resource (e.g., after merging JSON).
     *
     * @param data new data wrapper
     */
    public void data(Data data) {
        this.data = data;
    }

    /**
     * Supported resource payload types.
     */
    public enum Type {
        /**
         * JSON-based descriptive data.
         */
        DATA,
        /**
         * Texture-based image data.
         */
        TEXTURE,
        /**
         * Sound-based audio data.
         */
        SOUND,
        /**
         * Music-based audio data.
         */
        MUSIC
    }

    /**
     * Pair of type and decoded metadata of the resource.
     */
    public record Data(Type type, ResourceMeta<?> data) {
    }

    /**
     * Logical address of a resource, formed as namespace:path (without file extension).
     */
    public record Location(String namespace, String path) {

        /**
         * Parses a resource location from a string.
         */
        public static Location fromString(String resourceLocation) {
            String[] parts = resourceLocation.split(":");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid resource location format: " + resourceLocation);
            }
            return new Location(parts[0], parts[1]);
        }

        @Override
        public String toString() {
            return namespace + ":" + path;
        }

        /**
         * Resolves a FileHandle by searching local assets first and falling back to internal assets.
         *
         * @return a FileHandle pointing to the resolved file (without extension matching either local or internal)
         */
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

    /**
     * Base for resource-specific registries that know how to load objects of type T from a resource location.
     *
     * @param <T> the object type stored in the registry
     */
    public abstract static class RegistryAccess<T> {

        /**
         * The backing registry for this resource domain.
         */
        public final Registry<T> registry = Registries.register(key(), () -> null);

        /**
         * @return the registry key (e.g., "models", "animations").
         */
        public abstract String key();

        /**
         * Loads an object of type T from a resource location and registers it.
         *
         * @param location resource location string (namespace:path)
         * @return loaded object instance
         */
        public abstract T load(String location);

        public Collection<T> all() {
            return registry.values();
        }
    }


}
