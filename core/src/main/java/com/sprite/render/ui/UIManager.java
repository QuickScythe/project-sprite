package com.sprite.render.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * Lightweight UI system to be reused by any GameScreen.
 * Wraps a Scene2D Stage, ScreenViewport, and a minimal programmatic Skin.
 */
public class UIManager {

    private final Stage stage;
    private final Skin skin;

    public UIManager() {
        this.stage = new Stage(new ScreenViewport());
        this.skin = UISkinFactory.createDefaultSkin();

        attachToInput();
    }

    private void attachToInput() {
        // Ensure UI receives input; keep any existing input processors via a multiplexer.
        if (!(Gdx.input.getInputProcessor() instanceof InputMultiplexer)) {
            InputMultiplexer mux = new InputMultiplexer();
            // Stage first so UI consumes events before gameplay.
            mux.addProcessor(stage);
            if (Gdx.input.getInputProcessor() != null) mux.addProcessor(Gdx.input.getInputProcessor());
            Gdx.input.setInputProcessor(mux);
        } else {
            ((InputMultiplexer) Gdx.input.getInputProcessor()).addProcessor(0, stage);
        }
    }

    public void detachFromInput() {
        if (Gdx.input.getInputProcessor() instanceof InputMultiplexer mux) {
            mux.removeProcessor(stage);
            // If no processors remain, clear to avoid stray input behavior
            if (mux.size() == 0) {
                Gdx.input.setInputProcessor(null);
            }
        } else if (Gdx.input.getInputProcessor() == stage) {
            // Edge case: stage is directly set as input processor
            Gdx.input.setInputProcessor(null);
        }
    }

    public Stage stage() {
        return stage;
    }

    public Skin skin() {
        return skin;
    }

    public void actAndDraw(float delta) {
        stage.act(delta);
        stage.draw();
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public void dispose() {
        detachFromInput();
        stage.dispose();
        skin.dispose();
    }
}
