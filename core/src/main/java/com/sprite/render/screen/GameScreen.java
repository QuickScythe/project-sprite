package com.sprite.render.screen;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.sprite.input.InputSystem;
import com.sprite.render.RenderPipeline;
import com.sprite.render.camera.GameCamera;
import com.sprite.render.ui.UI;
import com.sprite.resource.ui.UIType;

import java.util.HashMap;
import java.util.Map;

public abstract class GameScreen implements Screen {

    protected UIManager ui;
    Map<Integer, UI<? extends UIType>> uis = new HashMap<>();
    UIManager uiManager = new UIManager();
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
        // Increase default UI font scale for better legibility on high resolutions
        font.getData().setScale(2f);
        renderPipeline = new RenderPipeline() {

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

    @Override
    public void dispose() {
        if (sprite != null) sprite.dispose();
        if (debugRenderer != null) debugRenderer.dispose();
    }

    public abstract void create();

    @Override
    public void render(float delta) {
        // Update high-level input first so just-pressed works for this frame
        InputSystem.i().update(delta);

        ScreenUtils.clear(color);
        renderPipeline.background();
        renderPipeline.backgroundShaders();
        renderPipeline.foreground();
        renderPipeline.foregroundShader();
        renderPipeline.ui();
        renderPipeline.uiShader();

        ui().draw();

//        ui().actAndDraw(delta);
    }

    public UIManager ui() {
        return uiManager;
    }

    public class UIManager {

        public void draw() {
            for (int i = 1; i <= uis.size(); i++) {
                if (uis.get(i) != null) uis.get(i).draw(GameScreen.this);
            }
        }

        public void addFirst(UI<? extends UIType> ui) {
            final int firstSize = uis.size();
            System.out.println("Adding UI at index " + firstSize);
            for (int i = firstSize; i > 0; i--) {
                System.out.println("Moving UI at index " + i + " to index " + (i + 1));
                uis.put(i + 1, uis.get(i));
            }
            System.out.println("Adding UI at index 1");
            uis.put(1, ui);
        }

        public void add(UI<? extends UIType> ui) {
            uis.put(uis.size() + 1, ui);
        }

        public void remove(int index) {
            uis.remove(index);
        }

        public void clear() {
            uis.clear();
        }

        public int size() {
            return uis.size();
        }
    }
}
