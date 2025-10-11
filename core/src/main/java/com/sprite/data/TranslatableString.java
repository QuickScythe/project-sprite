package com.sprite.data;

import com.sprite.utils.Texts;

/**
 * Wrapper for i18n translation keys used with {@link Texts}.
 * Provides case-insensitive equality with both other TranslatableString instances and raw String keys.
 * The {@link #toString()} returns the key itself.
 */
public record TranslatableString(String key) {

    /**
     * Case-insensitive equality with TranslatableString and String.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TranslatableString other)
            return other.key.equalsIgnoreCase(this.key);
        if (obj instanceof String str)
            return str.equalsIgnoreCase(this.key);
        return false;
    }

    /**
     * Returns the key. A translation lookup is triggered to warm caches, but result is not used.
     * @return the raw key string
     */
    @Override
    public String toString() {
        Texts.get(this);
        return key;
    }
}
