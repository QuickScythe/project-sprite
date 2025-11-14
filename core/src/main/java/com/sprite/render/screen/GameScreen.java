package com.sprite.render.screen;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.sprite.render.RenderPipeline;
import com.sprite.render.camera.GameCamera;
import com.sprite.render.ui.UIManager;

public abstract class GameScreen implements Screen {

    protected UIManager ui;
    private GameCamera camera;
    private ShapeRenderer debugRenderer;
    private SpriteBatch sprite;
    private BitmapFont font;
    private Color color = new Color(0.1f, 0.1f, 0.12f, 1f);
    private RenderPipeline renderPipeline;

    @Override
    public void show() {
        camera = new GameCamera();
        sprite = new SpriteBatch();
        ui = new UIManager();
        font = new BitmapFont();
        renderPipeline = new RenderPipeline(){

            @Override
            public void background() {

            }

            @Override
            public void backgroundShaders() {

            }

            @Override
            public void foreground() {

            }

            @Override
            public void foregroundShader() {

            }

            @Override
            public void ui() {

            }

            @Override
            public void uiShader() {

            }
        };

        camera.setToOrtho(false, 1920, 1080);
        camera.update();

        debugRenderer = new ShapeRenderer();
        create();
    }

    public Color color() {
        return color;
    }

    public void color(Color color) {
        this.color = color;
    }

    public BitmapFont font() {
        return font;
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

    @Override
    public void render(float delta) {
        ScreenUtils.clear(color);
        renderPipeline.background();
        renderPipeline.backgroundShaders();
        renderPipeline.foreground();
        renderPipeline.foregroundShader();
        renderPipeline.ui();
        renderPipeline.uiShader();

        ui().actAndDraw(delta);
    }






}
