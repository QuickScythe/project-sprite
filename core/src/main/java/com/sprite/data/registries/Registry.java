package com.sprite.data.registries;


import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * A generic registry for managing objects of type T.
 *
 * @param <T> the type of objects to be managed by this registry
 */
public class Registry<T> {

    /**
     * The internal map storing the registry entries.
     */
    private final Map<String, T> registry = new LinkedHashMap<>();


    /**
     * The key associated with this registry.
     */
    private final RegistryKey key;

    /**
     * Constructs a new Registry for the specified type.
     */
    public Registry(RegistryKey key) {
        this.key = key;
//        this.type = type;
    }

    public RegistryKey key() {
        return key;
    }


    /**
     * Clears all entries from the registry.
     */
    public void clear() {
        registry.clear();
    }

    /**
     * Reloads the registry by clearing all entries.
     */
    public void reload() {
        clear();
    }

    /**
     * Returns the internal map of the registry.
     *
     * @return the internal map of the registry
     */
    public Map<String, T> toMap() {
        return registry;
    }

    /**
     * Retrieves an entry from the registry by its key, or returns a default value if the key is not found.
     *
     * @param key          the key of the entry to retrieve
     * @param defaultValue the default value to return if the key is not found
     * @return the entry associated with the key, or the default value if the key is not found
     */
    public T getOrDefault(String key, T defaultValue) {
        return registry.getOrDefault(key, defaultValue);
    }

    /**
     * Registers a new entry in the registry with the given ID.
     *
     * @param id the ID of the entry to register
     * @param t  the entry to register
     */
    public void register(String id, T t) {
        registry.put(id, t);
    }

    /**
     * Applies the specified consumer to each entry in the registry.
     *
     * @param consumer the consumer to apply to each entry
     */
    public void forEach(BiConsumer<String, T> consumer) {
        registry.forEach(consumer);
    }


    /**
     * Retrieves an entry from the registry by its key.
     *
     * @param key the key of the entry to retrieve
     * @return an Optional containing the entry if found, or an empty Optional if not found
     */
    public Optional<T> get(String key) {
        return Optional.ofNullable(getOrDefault(key, null));
    }

    /**
     * Returns the number of entries in the registry.
     *
     * @return the number of entries in the registry
     */
    public int size() {
        return registry.size();
    }

    public Collection<T> values() {
        return registry.values();
    }

    public boolean has(String animation) {
        return get(animation).isPresent();
    }
}
