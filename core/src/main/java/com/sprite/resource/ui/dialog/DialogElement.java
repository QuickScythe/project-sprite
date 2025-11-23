package com.sprite.resource.ui.dialog;

import com.badlogic.gdx.math.Vector2;

public class DialogElement {

    private final String name;
    private final Vector2 position;
    private final Vector2 size;

    public DialogElement(String name, Vector2 position, Vector2 size) {
        this.name = name;
        this.position = position;
        this.size = size;
    }


    public String name() {
        return name;
    }

    public Vector2 position() {
        return position;
    }

    public Vector2 size() {
        return size;
    }

}
