package com.sprite.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Manages a list of objects
 *
 * @param <T> The type of object to manage
 * @param <E> The type of enum to manage
 */
public class NestedListManager<T extends TypeAdapter<E>, E extends Enum<E>> {

    private final List<T> objects = new ArrayList<>();

    /**
     * @param objects The objects to manage
     */
    @SafeVarargs
    public NestedListManager(T... objects) {
        this.objects.addAll(Arrays.asList(objects));
    }

    /**
     * Filters the list of objects by type
     *
     * @param type The type to filter by
     * @return A new ObjectManager with the filtered objects
     */
    public NestedListManager<T, E> ofType(E type) {
        return new NestedListManager<>(objects.stream().filter(object -> object.type().equals(type)).toArray(TypeAdapter[]::new));
    }

    /**
     * Gets the first object of the specified type
     *
     * @param type The type to get
     * @return The object of the specified type
     */
    public T get(E type) {
        return objects.stream().filter(object -> object.type().equals(type)).findFirst().orElse(null);
    }

    /**
     * Gets all objects
     *
     * @return All objects
     */
    public List<T> all() {
        return objects;
    }

    /**
     * Gets a random object
     *
     * @return A random object
     */
    public T random() {
        return objects.get((int) (Math.random() * objects.size()));
    }

}
