package com.sprite.resource.ui;

import com.badlogic.gdx.math.Vector2;
import com.sprite.render.screen.GameScreen;
import com.sprite.resource.texture.GameSprite;

public abstract class UIType {

    GameSprite texture = null;
    float textureWidth;
    float textureHeight;
    int rows;
    int columns;
    float gridWidthSize;
    float gridHeightSize;


    public abstract void draw(GameScreen screen);

    public abstract void create(UIDefinition uiDefinition);

    public Vector2 position(GameScreen screen, int index) {
        int x = index % columns;
        int y = index / columns;
        return position(screen, new Vector2(x, y));
    }

    public Vector2 position(GameScreen screen, Vector2 rawPosition) {
        float centerX = screen.camera().viewportWidth / 2f;
        float centerY = screen.camera().viewportHeight / 2f;
        return new Vector2(centerX - (textureWidth / 2) + (rawPosition.x * gridWidthSize), centerY - (textureHeight / 2) + (rawPosition.y * gridHeightSize));
    }

    public float gridWidthSize() { return gridWidthSize; }
    public float gridHeightSize() { return gridHeightSize; }
    public int rows() { return rows; }
    public int columns() { return columns; }
    public GameSprite texture() { return texture; }
    public float textureWidth() { return textureWidth; }
    public float textureHeight() { return textureHeight; }
}
