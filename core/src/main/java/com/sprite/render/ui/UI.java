package com.sprite.render.ui;

import com.sprite.render.screen.GameScreen;
import com.sprite.resource.ui.UIType;

public abstract class UI<T extends UIType> {

    T type;

    public UI(T type) {
        this.type = type;
    }

    public T type() {
        return type;
    }

    public abstract void draw(GameScreen screen);
}
