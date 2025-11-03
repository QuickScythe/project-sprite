package com.sprite.render.screen;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.sprite.render.camera.GameCamera;
import com.sprite.render.ui.UIManager;

public abstract class GameScreen implements Screen {

    private final boolean debug = true;
    private final float scale = 1 / 16f;
    private final float cameraScale = 0.5f;
    protected UIManager ui;
    private GameCamera camera;
    private ShapeRenderer debugRenderer;
    private SpriteBatch sprite;

    @Override
    public void show() {
        camera = new GameCamera();
        sprite = new SpriteBatch();
        ui = new UIManager();

        camera.setToOrtho(false, 1920, 1080);
        camera.update();

        debugRenderer = new ShapeRenderer();
        create();
    }

    public ShapeRenderer shape() {
        return debugRenderer;
    }

    public SpriteBatch sprite() {
        return sprite;
    }

    public GameCamera camera() {
        return camera;
    }

    public UIManager ui() {
        return ui;
    }

    @Override
    public void resize(int width, int height) {
        if (ui != null) ui.resize(width, height);
    }

    @Override
    public void hide() {
        // Detach UI input when screen is no longer active to prevent input leakage between screens
        if (ui != null) {
            ui.detachFromInput();
            // Optional: clear actors to free references early
            if (ui.stage() != null) ui.stage().clear();
        }
    }

    @Override
    public void dispose() {
        if (sprite != null) sprite.dispose();
        if (debugRenderer != null) debugRenderer.dispose();
        if (ui != null) ui.dispose();
    }

    public abstract void create();






}
