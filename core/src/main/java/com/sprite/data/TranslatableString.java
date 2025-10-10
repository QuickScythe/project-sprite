package com.sprite.data;

import com.sprite.utils.Texts;

public class TranslatableString {

    public final String key;


    public TranslatableString(String key) {
        this.key = key;
    }

    @Override
    public String toString() {
        Texts.get(this);

        return key;
    }
}
