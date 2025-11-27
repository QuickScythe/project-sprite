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

    /**
     * Returns the UI-relative position (top-left origin of the centered UI texture) for a given grid index.
     * This does NOT include the camera world origin; for world coordinates use {@link #worldPosition(GameScreen, Vector2)}.
     */
    public Vector2 position(GameScreen screen, int index) {
        int x = index % columns;
        int y = index / columns;
        return position(screen, new Vector2(x, y));
    }

    /**
     * Returns the UI-relative position of a grid cell measured from the UI texture's top-left when centered on screen.
     * This is the same coordinate space used by existing UI rendering; add {@link #cameraOrigin(GameScreen)} to get world space.
     */
    public Vector2 position(GameScreen screen, Vector2 rawPosition) {
        float centerX = screen.camera().viewportWidth / 2f;
        float centerY = screen.camera().viewportHeight / 2f;
        return new Vector2(centerX - (textureWidth / 2) + (rawPosition.x * gridWidthSize), centerY - (textureHeight / 2) + (rawPosition.y * gridHeightSize));
    }

    /** Lower-left world-space origin (camera position minus half viewport). */
    public Vector2 cameraOrigin(GameScreen screen) {
        float camX = (screen.camera().position.x - screen.camera().viewportWidth / 2f);
        float camY = (screen.camera().position.y - screen.camera().viewportHeight / 2f);
        return new Vector2(camX, camY);
    }

    /** Top-left world-space position where the UI texture should be drawn to be centered on screen. */
    public Vector2 uiTopLeft(GameScreen screen) {
        Vector2 cam = cameraOrigin(screen);
        float x = cam.x + (screen.camera().viewportWidth / 2f) - (textureWidth / 2f);
        float y = cam.y + (screen.camera().viewportHeight / 2f) - (textureHeight / 2f);
        return new Vector2(x, y);
    }

    /** World-space position for a grid cell given its UI grid coordinates. */
    public Vector2 worldPosition(GameScreen screen, Vector2 gridPos) {
        Vector2 cam = cameraOrigin(screen);
        Vector2 uiPos = position(screen, gridPos);
        return new Vector2(cam.x + uiPos.x, cam.y + uiPos.y);
    }

    public float gridWidthSize() { return gridWidthSize; }
    public float gridHeightSize() { return gridHeightSize; }
    public int rows() { return rows; }
    public int columns() { return columns; }
    public GameSprite texture() { return texture; }
    public float textureWidth() { return textureWidth; }
    public float textureHeight() { return textureHeight; }
}
