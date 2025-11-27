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
import com.sprite.render.ui.inventory.Dialog;
import com.sprite.render.ui.inventory.Inventory;
import com.sprite.resource.ui.DialogUI;
import com.sprite.resource.ui.InventoryUI;
import com.sprite.resource.ui.UIDefinition;
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
        renderPipeline = new RenderPipeline();
        renderPipeline.ui((screen, delta) -> {
            if (uiManager.isOpen()) ui().draw();
        });

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
        camera.update();
        sprite().setProjectionMatrix(camera.combined);
        sprite().begin();
        renderPipeline.background().render(this, delta);
        renderPipeline.foreground().render(this, delta);
        renderPipeline.ui().render(this, delta);
        sprite().end();



//        ui().actAndDraw(delta);
    }

    public RenderPipeline pipeline() {
        return renderPipeline;
    }

    public UIManager ui() {
        return uiManager;
    }

    public class UIManager {

        private boolean open = false;

        public UI<? extends UIType> build(UIDefinition uiDefinition) {
            if (uiDefinition.type() instanceof InventoryUI type) {
                return new Inventory(type);
            }
            if (uiDefinition.type() instanceof DialogUI type) {
                return new Dialog(type);
            }
            throw new IllegalStateException("Unsupported UI type: " + uiDefinition.type().getClass().getSimpleName() + "");
        }

        public void draw() {
            for (int i = 1; i <= uis.size(); i++) {
                if (uis.get(i) != null) uis.get(i).draw(GameScreen.this);
            }
        }
        public void debug() {
            for (int i = 1; i <= uis.size(); i++) {
                if (uis.get(i) != null) uis.get(i).debug(GameScreen.this);
            }
        }

        public void addFirst(UI<? extends UIType> ui) {
            final int firstSize = uis.size();
            for (int i = firstSize; i > 0; i--) {
                uis.put(i + 1, uis.get(i));
            }
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

        public boolean isOpen() {
            return open;
        }

        public void open() {
            open = true;
        }

        public void close() {
            open = false;
        }



    }
}
