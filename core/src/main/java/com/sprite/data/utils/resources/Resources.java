package com.sprite.data.utils.resources;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.sprite.data.registries.Registries;
import com.sprite.data.registries.Registry;
import com.sprite.data.utils.Utils;
import com.sprite.game.world.gen.ChunkGenerator;
import com.sprite.game.world.gen.ChunkGeneratorFactory;
import com.sprite.resource.Resource;
import com.sprite.resource.ResourceMeta;
import com.sprite.resource.animations.Animation;
import com.sprite.resource.controllers.Controller;
import com.sprite.resource.entities.EntityType;
import com.sprite.resource.input.Input;
import com.sprite.resource.items.ItemType;
import com.sprite.resource.loot.LootTable;
import com.sprite.resource.models.Model;
import com.sprite.resource.texture.GameSprite;
import com.sprite.resource.tiles.TileType;
import com.sprite.resource.ui.UIAction;
import com.sprite.resource.ui.UIDefinition;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.sprite.resource.Resource.Type.DATA;

/**
 * Resource manager responsible for scanning, registering, and providing access to game resources.
 * <p>
 * It builds registries for domain objects (models, animations, textures, entities) and exposes
 * a generic registry of all raw Resource entries by logical location (namespace:path).
 */
public class Resources {

    /**
     * Global registry of all discovered resources (json and textures).
     */
    public final Registry<Resource> RESOURCE_REGISTRY = Registries.register("resources", () -> null);
    /**
     * Registry access wrapper for models.
     */
    public final Resource.RegistryAccess<Model> MODELS;
    /**
     * Registry access wrapper for animations.
     */
    public final Resource.RegistryAccess<Animation> ANIMATIONS;
    /**
     * Registry access wrapper for textures/sprites.
     */
    public final Resource.RegistryAccess<GameSprite> TEXTURES;
    /**
     * Registry access wrapper for entity types.
     */
    public final Resource.RegistryAccess<EntityType> ENTITIES;
    /**
     * Registry access wrapper for inputs.
     */
    public final Resource.RegistryAccess<Input> INPUTS;
    /**
     * Registry access wrapper for entity controllers.
     */
    public final Resource.RegistryAccess<Controller> CONTROLLERS;
    /**
     * Registry access wrapper for UIs.
     */
    public final Resource.RegistryAccess<UIDefinition> USER_INTERFACES;

    /**
     * Registry access wrapper for UI Actions.
     */
    public final Resource.RegistryAccess<UIAction> ACTIONS;

    /**
     * Registry access wrapper for music assets.
     */
    public final Resource.RegistryAccess<Music> MUSIC;

    /**
     * Registry access wrapper for sound assets.
     */
    public final Resource.RegistryAccess<Sound> SOUNDS;

    /**
     * Registry access wrapper for tiles
     */
    public final Resource.RegistryAccess<TileType> TILES;

    /**
     * Registry access wrapper for Items.
     */
    public final Resource.RegistryAccess<ItemType> ITEMS;

    /**
     * Registry access wrapper for loot tables.
     */
    public final Resource.RegistryAccess<LootTable> LOOT;

    /**
     * Registry access wrapper for ChunkGenerators
     */
    public final Resource.RegistryAccess<ChunkGenerator> CHUNK_GENERATORS;

    /**
     * Creates a Resources manager, scanning both internal (classpath) and external (local) assets
     * under the provided root folder.
     *
     * @param rootFolder the root assets folder (e.g., "resources")
     */
    public Resources(String rootFolder) {
        CHUNK_GENERATORS = registerRegistry("chunk_generators", (resource -> ChunkGeneratorFactory.create(((ResourceMeta.Json)resource.data.data()).get())));
        LOOT = new Resource.RegistryAccess<>(null) {
            @Override
            public String key() {
                return "loot";
            }

            @Override
            public LootTable load(String location) {
                return null;
            }

        };
        MUSIC = registerRegistry("music", (location,r) -> {
            Resource resource = Utils.resources().get(location).orElseThrow();
            return Gdx.audio.newMusic(resource.file());
        });
        SOUNDS = registerRegistry("sounds", resource -> Gdx.audio.newSound(resource.file()));
        TILES = registerRegistry("tiles", TileType::new);
        ITEMS = registerRegistry("items", ItemType::new);
        ACTIONS = registerRegistry("actions", UIAction::new);
        USER_INTERFACES = registerRegistry("ui", UIDefinition::new);
        INPUTS = registerRegistry("input", Input::new);
        CONTROLLERS = registerRegistry("controllers", Controller::new);
        MODELS = registerRegistry("models", Model::new);
        ANIMATIONS = registerRegistry("animations", Animation::new);
        TEXTURES = registerRegistry("textures", (location, registry) -> {
            if (registry.get(location).isPresent()) {
                Gdx.app.debug("Resource", "Texture already loaded: " + location);
                return registry.get(location).get();
            }
            Gdx.app.log("Resource", "Loading texture: " + location);
            Resource resource = Utils.resources().get(location).orElse(Utils.resources().get("textures:missing").orElseThrow());
            GameSprite sprite = new GameSprite(resource);
            registry.register(location, sprite);
            return sprite;
        });
        ENTITIES = registerRegistry("entities", EntityType::new);
        FileHandle handle = Gdx.files.classpath("assets.txt");
        String read = handle.readString();
        loadInternal(rootFolder, read);

        loadExternal(rootFolder);

    }

    /**
     * Loads and registers resources available within the classpath assets, filtered by the root folder.
     */
    private void loadInternal(String rootFolder, String read) {
        String[] paths = read.split("\n");
        for (String path : paths) {
            path = path.trim();
            if (!path.startsWith(rootFolder + "/")) {
                continue;
            }
            FileHandle handle = Gdx.files.classpath(path);
            String originalPath = path;
            path = path.substring((rootFolder + "/").length()).trim();
            String namespace = path.contains("/") ? path.split("/")[0] : "internal";
            path = path.contains("/") ? path.substring(namespace.length() + 1).trim() : path;
            String ext = handle.extension();
            path = path.replace("." + ext, "");
            Gdx.app.log("Resource", "Registering internal resource: " + namespace + ":" + path);

            Resource.Location location = new Resource.Location(namespace, path);
            Resource resource = new Resource(location, Gdx.files.classpath(originalPath), true);
            RESOURCE_REGISTRY.register(location.toString(), resource);
        }
    }

    /**
     * Scans the local file system for additional resources in the given root and registers them.
     * External resources can augment or override internal resources.
     */
    private void loadExternal(String rootFolder) {
        FileHandle assetsFolder = Gdx.files.local(rootFolder);
        if (!assetsFolder.exists() && assetsFolder.isDirectory()) {
            throw new RuntimeException(rootFolder + " folder does not exist or is not a directory");
        }
        for (FileHandle handle : assetsFolder.list()) {
            if (!handle.isDirectory()) {
                continue;
            }
            String namespace = handle.name();
            Gdx.app.log("Resource", "Registering external resources in namespace: " + namespace + ".");
            registerResources(handle, namespace, "");
        }
    }

    private <T> Resource.RegistryAccess<T> registerRegistry(String key, Function<Resource, T> creator){
        Gdx.app.log("Resource", "Registering resource registry: " + key);
        return new Resource.RegistryAccess<>(creator) {
            @Override
            public String key() {
                return key;
            }
        };
    }

    /**
     * Utility to build a typed registry for a specific resource domain.
     *
     * @param key  registry key (e.g., "models")
     * @param load function that loads and registers an object of type T from a location
     */
    private <T> Resource.RegistryAccess<T> registerRegistry(String key, BiFunction<String, Registry<T>, T> load) {
        Gdx.app.log("Resource", "Registering resource registry: " + key);
        return new Resource.RegistryAccess<>(null) {
            @Override
            public String key() {
                return key;
            }

            @Override
            public T load(String location) {
                return load.apply(location, registry);
            }
        };
    }

    /**
     * Recursively registers all files under a namespace folder, merging JSON data when duplicates exist.
     */
    private void registerResources(FileHandle folder, String namespace, String path) {
        for (FileHandle file : folder.list()) {
            String newPath = path.isEmpty() ? file.nameWithoutExtension() : path + "/" + file.nameWithoutExtension();
            if (file.isDirectory()) {
                registerResources(file, namespace, newPath);
                continue;
            }

            Resource.Location location = new Resource.Location(namespace, newPath);
            Resource resource = new Resource(location, file, false);
            if (RESOURCE_REGISTRY.has(location.toString())) {
                Resource existing = RESOURCE_REGISTRY.get(location.toString()).orElseThrow();
                if (existing.type().equals(DATA)) {
                    ResourceMeta.Json existingMeta = (ResourceMeta.Json) existing.data.data();
                    JSONObject existingJson = existingMeta.get();
                    JSONObject newJson = ((ResourceMeta.Json) resource.data.data()).get();
                    for (String key : newJson.keySet()) {
                        existingJson.put(key, newJson.get(key));
                    }
                    Gdx.app.log("Resource", "Merged resource data for: " + location);
                    ResourceMeta.Json newMeta = new ResourceMeta.Json(existingJson);
                    Resource.Data newData = new Resource.Data(DATA, newMeta);
                    resource.data(newData);

                }
            }

            RESOURCE_REGISTRY.register(location.toString(), resource);
        }
    }

    /**
     * Disposes loaded resources and clears registries.
     */
    public void dispose() {
        try {
            // Dispose textures
            for (GameSprite sprite : TEXTURES.all()) {
                try {
                    sprite.dispose();
                } catch (Throwable ignored) {
                }
            }
        } finally {
            // Clear all registries to release references
            Registries.reset();
        }
    }

    /**
     * Returns a list of resources that belong to the provided namespace.
     *
     * @param namespace the namespace id
     * @return list of resources within the namespace
     */
    public List<Resource> list(String namespace) {
        List<Resource> resources = new ArrayList<>();
        for (Resource resource : RESOURCE_REGISTRY.values()) {
            if (resource.location().namespace().equals(namespace)) {
                resources.add(resource);
            }
        }
        return resources;
    }

    /**
     * Gets a resource by its full logical location (namespace:path).
     *
     * @param resourceLocation location string
     * @return optional resource
     */
    public Optional<Resource> get(String resourceLocation) {
        return RESOURCE_REGISTRY.get(resourceLocation);
    }
}
