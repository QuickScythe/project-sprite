package com.sprite.data;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents metadata stored in a JSON object.
 */
public class Metadata {

    private final JSONObject data;

    /**
     * Constructs a Metadata instance with the specified JSON data.
     * Recursively converts nested JSON objects to Metadata instances.
     *
     * @param data the JSON data
     */
    private Metadata(JSONObject data) {
        for (String key : data.keySet()) {
            if (data.get(key) instanceof JSONObject nestedData) {
                data.put(key, new Metadata(nestedData));
            }
        }
        this.data = data;
    }

    /**
     * Creates a Metadata instance from the specified JSON data.
     *
     * @param data the JSON data
     * @return a new Metadata instance
     */
    public static Metadata of(JSONObject data) {
        return new Metadata(data);
    }

    /**
     * Retrieves the Metadata associated with the specified key.
     *
     * @param key the key to look up
     * @return the Metadata associated with the key, or null if not found
     */
    public Metadata get(String key) {
        return data.opt(key) instanceof Metadata metadata ? metadata : null;
    }

    /**
     * Retrieves the value associated with the specified key and casts it to the specified class.
     *
     * @param key the key to look up
     * @param returnClass the class to cast the value to
     * @param <E> the type of the value
     * @return the value associated with the key
     * @throws IllegalArgumentException if the value cannot be cast to the specified class
     */
    public <E> E value(String key, Class<E> returnClass) {
        try {
            return returnClass.cast(data.get(key));
        } catch (ClassCastException | JSONException e) {
            throw new IllegalArgumentException("Could not find MetadataValue for " + key);
        }
    }

    /**
     * Returns a string representation of the Metadata.
     *
     * @return a string representation of the Metadata
     */
    @Override
    public String toString() {
        return this.toString(0);
    }

    /**
     * Returns a string representation of the Metadata with the specified indentation.
     *
     * @param indent the number of spaces to indent
     * @return a string representation of the Metadata with the specified indentation
     */
    public String toString(int indent) {
        return data.toString(indent);
    }
}
