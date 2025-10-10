package com.sprite.data;

/**
 * A simple type adapter for enums
 *
 * @param <E> The enum type
 */
public class TypeAdapter<E extends Enum<E>> {
    /**
     * The enum value associated with this adapter.
     */
    private final E enumValue;

    /**
     * Constructs a new TypeAdapter with the specified enum value.
     *
     * @param enumValue the enum value to associate with this adapter
     */
    public TypeAdapter(E enumValue) {
        this.enumValue = enumValue;
    }

    /**
     * Returns the enum value associated with this adapter.
     *
     * @return the enum value associated with this adapter
     */
    public E type() {
        return enumValue;
    }
}
