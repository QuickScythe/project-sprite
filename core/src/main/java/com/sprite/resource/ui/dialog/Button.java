package com.sprite.resource.ui.dialog;

import com.badlogic.gdx.math.Vector2;

public class Button extends DialogElement {

    private final String title;

    public Button(String name, String translationKey, Vector2 position, Vector2 size) {
        super(name, position, size);
        this.title = translationKey;
    }

    public String title() {
        return title;
    }
}
