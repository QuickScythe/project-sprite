package com.sprite.data.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.sprite.data.registries.Registries;
import com.sprite.data.registries.Registry;
import com.sprite.resource.ResourceMeta;
import com.sprite.resource.animations.Animation;
import com.sprite.resource.Resource;
import com.sprite.resource.controllers.Controller;
import com.sprite.resource.entities.EntityType;
import com.sprite.resource.input.Input;
import com.sprite.resource.models.Model;
import com.sprite.resource.texture.GameSprite;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

import static com.sprite.resource.Resource.Type.DATA;

/**
 * Resource manager responsible for scanning, registering, and providing access to game resources.
 * <p>
 * It builds registries for domain objects (models, animations, textures, entities) and exposes
 * a generic registry of all raw Resource entries by logical location (namespace:path).
 */
public class Resources {

    /** Global registry of all discovered resources (json and textures). */
    public final Registry<Resource> RESOURCE_REGISTRY = Registries.register("resources", () -> null);
    /** Registry access wrapper for models. */
    public final Resource.RegistryAccess<Model> MODELS;
    /** Registry access wrapper for animations. */
    public final Resource.RegistryAccess<Animation> ANIMATIONS;
    /** Registry access wrapper for textures/sprites. */
    public final Resource.RegistryAccess<GameSprite> TEXTURES;
    /** Registry access wrapper for entity types. */
    public final Resource.RegistryAccess<EntityType> ENTITIES;
    /** Registry access wrapper for inputs. */
    public final Resource.RegistryAccess<Input> INPUTS;
    /** Registry access wrapper for entity controllers. */
    public final Resource.RegistryAccess<Controller> CONTROLLERS;

    /**
     * Creates a Resources manager, scanning both internal (classpath) and external (local) assets
     * under the provided root folder.
     * @param rootFolder the root assets folder (e.g., "resources")
     */
    public Resources(String rootFolder) {
        INPUTS = new Resource.RegistryAccess<>() {
            @Override
            public String key() {
                return "input";
            }

            @Override
            public Input load(String location) {
                Input input = new Input(location);
                registry.register(location, input);
                return input;
            }
        };
        CONTROLLERS = registerRegistry("controllers", (location, registry) -> {
            if (registry.get(location).isPresent()) {
                Gdx.app.debug("Resource", "Controller already loaded: " + location);
                return registry.get(location).get();
            }
            Gdx.app.log("Resource", "Loading controller: " + location);
            Resource resource = Utils.resources().get(location).orElseThrow();
            Controller controller = new Controller(resource);
            registry.register(location, controller);
            return controller;
        });
        MODELS = registerRegistry("models", (location, registry) -> {
            if (registry.get(location).isPresent()) {
                Gdx.app.debug("Resource", "Model already loaded: " + location);
                return registry.get(location).get();
            }
            Gdx.app.log("Resource", "Loading model: " + location);
            Resource resource = Utils.resources().get(location).orElseThrow();
            Model model = new Model(resource);
            registry.register(location, model);
            return model;
        });
        ANIMATIONS = registerRegistry("animations", (location, registry) -> {
            if (registry.get(location).isPresent()) {
                Gdx.app.debug("Resource", "Animation already loaded: " + location);
                return registry.get(location).get();
            }
            Gdx.app.log("Resource", "Loading animation: " + location);
            Resource resource = Utils.resources().get(location).orElseThrow();
            Animation animation = new Animation(resource);

            registry.register(location, animation);
            return animation;
        });
        TEXTURES = registerRegistry("textures", (location, registry) -> {
            if (registry.get(location).isPresent()) {
                Gdx.app.debug("Resource", "Texture already loaded: " + location);
                return registry.get(location).get();
            }
            Gdx.app.log("Resource", "Loading texture: " + location);
            Resource resource = Utils.resources().get(location).orElseThrow();
            GameSprite sprite = new GameSprite(resource);
            registry.register(location, sprite);
            return sprite;
        });
        ENTITIES = registerRegistry("entities", (location, registry) -> {
            if (registry.get(location).isPresent()) {
                Gdx.app.debug("Resource", "Texture already loaded: " + location);
                return registry.get(location).get();
            }
            Gdx.app.log("Resource", "Loading entity: " + location);
            Resource resource = Utils.resources().get(location).orElseThrow();
            EntityType sprite = new EntityType(resource);
            registry.register(location, sprite);
            return sprite;
        });

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
            path = path.contains("/") ? path.substring(namespace.length()+1).trim() : path;
            String ext = handle.extension();
            path = path.replace("." + ext, "");
            Gdx.app.log("Resource", "Registering internal resource: " + namespace + ":" + path);

            Resource.Location location = new Resource.Location(namespace, path);
            Resource resource = new Resource(location, Gdx.files.classpath(originalPath),  true);
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

    /**
     * Utility to build a typed registry for a specific resource domain.
     * @param key registry key (e.g., "models")
     * @param load function that loads and registers an object of type T from a location
     */
    private <T> Resource.RegistryAccess<T> registerRegistry(String key, BiFunction<String, Registry<T>, T> load) {
        Gdx.app.log("Resource", "Registering resource registry: " + key);
        return new Resource.RegistryAccess<>() {
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
            if(RESOURCE_REGISTRY.has(location.toString())){
                Resource existing = RESOURCE_REGISTRY.get(location.toString()).orElseThrow();
                if(existing.type().equals(DATA)){
                    ResourceMeta.Json existingMeta = (ResourceMeta.Json) existing.data.data();
                    JSONObject existingJson = existingMeta.get();
                    JSONObject newJson = ((ResourceMeta.Json) resource.data.data()).get();
                    for(String key : newJson.keySet()){
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
     * Returns a list of resources that belong to the provided namespace.
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
     * @param resourceLocation location string
     * @return optional resource
     */
    public Optional<Resource> get(String resourceLocation) {
        return RESOURCE_REGISTRY.get(resourceLocation);
    }
}
