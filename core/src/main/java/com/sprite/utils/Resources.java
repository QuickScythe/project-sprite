package com.sprite.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.sprite.data.registries.Registries;
import com.sprite.data.registries.Registry;
import com.sprite.resource.animations.Animation;
import com.sprite.resource.Resource;
import com.sprite.resource.entities.EntityType;
import com.sprite.resource.models.Model;
import com.sprite.resource.texture.GameSprite;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

public class Resources {

    public final Registry<Resource<?>> RESOURCE_REGISTRY = Registries.register("resources", () -> null);
    public final Resource.RegistryAccess<Model> MODELS;
    public final Resource.RegistryAccess<Animation> ANIMATIONS;
    public final Resource.RegistryAccess<GameSprite> TEXTURES;
    public final Resource.RegistryAccess<EntityType> ENTITIES;


    public Resources(String rootFolder) {
        MODELS = registerRegistry("models", (location, registry) -> {
            if (registry.get(location).isPresent()) {
                Gdx.app.debug("Resource", "Model already loaded: " + location);
                return registry.get(location).get();
            }
            Gdx.app.log("Resource", "Loading model: " + location);
            Resource<?> resource = Utils.resources().get(location).orElseThrow();
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
            Resource<?> resource = Utils.resources().get(location).orElseThrow();
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
            Resource<?> resource = Utils.resources().get(location).orElseThrow();
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
            Resource<?> resource = Utils.resources().get(location).orElseThrow();
            EntityType sprite = new EntityType(resource);
            registry.register(location, sprite);
            return sprite;
        });

        // Load resources from the specified root folder
        System.out.println(Gdx.files.getExternalStoragePath());
        FileHandle handle1 = Gdx.files.classpath("assets.txt");
        String read = handle1.readString();
        loadInternal(rootFolder, read);

        loadExternal(rootFolder);


    }

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
            Resource<?> resource = new Resource<>(location, Gdx.files.classpath(originalPath), true);
            RESOURCE_REGISTRY.register(location.toString(), resource);
//
//            registerResources(handle, namespace, "");
        }
    }

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

    private <T extends Resource.Object> Resource.RegistryAccess<T> registerRegistry(String key, BiFunction<String, Registry<T>, T> load) {
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

    private void registerResources(FileHandle folder, String namespace, String path) {
        for (FileHandle file : folder.list()) {
            String newPath = path.isEmpty() ? file.nameWithoutExtension() : path + "/" + file.nameWithoutExtension();
            if (file.isDirectory()) {
                registerResources(file, namespace, newPath);
                continue;
            }

            Resource.Location location = new Resource.Location(namespace, newPath);
            Resource<?> resource = new Resource<>(location, file, false);

            RESOURCE_REGISTRY.register(location.toString(), resource);
        }
    }

    public List<Resource<?>> list(String namespace) {
        List<Resource<?>> resources = new ArrayList<>();
        for (Resource<?> resource : RESOURCE_REGISTRY.values()) {
            if (resource.location().namespace().equals(namespace)) {
                resources.add(resource);
            }
        }
        return resources;
    }

    public Optional<Resource<?>> get(String resourceLocation) {
        return RESOURCE_REGISTRY.get(resourceLocation);
    }
}
