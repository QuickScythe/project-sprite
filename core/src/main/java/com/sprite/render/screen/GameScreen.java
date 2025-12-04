package com.sprite.render.screen;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.sprite.data.utils.Utils;
import com.sprite.data.utils.audio.AudioChannel;
import com.sprite.data.utils.audio.Sounds;
import com.sprite.input.InputSystem;
import com.sprite.input.VirtualCursor;
import com.sprite.render.RenderPipeline;
import com.sprite.render.camera.GameCamera;
import com.sprite.render.ui.UI;
import com.sprite.render.ui.dialog.Dialog;
import com.sprite.render.ui.inventory.Inventory;
import com.sprite.resource.Resource;
import com.sprite.resource.ui.DialogUI;
import com.sprite.resource.ui.InventoryUI;
import com.sprite.resource.ui.UIDefinition;
import com.sprite.resource.ui.UIType;

import java.util.*;

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
        VirtualCursor cursor = InputSystem.i().cursor();
        cursor.draw(this);
        sprite().end();
    }

    public RenderPipeline pipeline() {
        return renderPipeline;
    }

    public UIManager ui() {
        return uiManager;
    }

    public void requestMusic() {
        List<Resource> possible = new ArrayList<>();
        for (Resource resource : Utils.resources().list("music")) {
            if (resource.location().path().startsWith("background")) {
                possible.add(resource);
            }
        }
        Sounds.play(possible.get(new Random().nextInt(possible.size())).location().toString(), AudioChannel.MUSIC);
    }

    public class UIManager {


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
                if (uis.get(i) != null && uis.get(i).open()) uis.get(i).draw(GameScreen.this);
            }
            if (camera().focus() != null && camera().focus().inventory().open())
                camera().focus().inventory().draw(GameScreen.this);
        }

        public void debug() {
            for (int i = 1; i <= uis.size(); i++) {
                if (uis.get(i) != null) uis.get(i).debug(GameScreen.this);
            }
        }

        private void add(UI<? extends UIType> ui) {
            uis.put(uis.size() + 1, ui);
        }

        public void open(UI<? extends UIType> ui) {
            if (uis.containsKey(uis.size())) uis.get(uis.size()).open(false);
            add(ui);
            ui.open(true);
        }

        public void remove(int index) {
            uis.remove(index);
        }

        public void remove(UI<? extends UIType> ui) {
            uis.values().removeIf(existingUI -> existingUI == ui);
        }

        public void clear() {
            uis.clear();
        }

        public int size() {
            return uis.size();
        }

        public boolean isOpen() {
            for (UI<? extends UIType> open : uis.values())
                if (open.open()) return true;
            return false;
        }

        public void close() {
            uis.get(uis.size() ).open(false);
            if (uis.containsKey(uis.size() - 1)) uis.get(uis.size() - 1).open(true);
            uis.remove(uis.size());

        }
    }
}
