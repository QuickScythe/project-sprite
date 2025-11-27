package com.sprite.render;

import com.sprite.render.screen.GameScreen;

public class RenderPipeline {

    private RenderPhase background = (screen, delta) -> {
    };
    private RenderPhase foreground = (screen, delta) -> {
    };
    private RenderPhase ui = (screen, delta) -> {
    };

    public RenderPhase background() {
        return background;
    }

    public RenderPhase foreground() {
        return foreground;
    }

    public RenderPhase ui() {
        return ui;
    }

    public void background(RenderPhase background) {
        this.background = background;
    }

    public void foreground(RenderPhase foreground) {
        this.foreground = foreground;
    }

    public void ui(RenderPhase ui) {
        this.ui = ui;
    }


    @FunctionalInterface
    public interface RenderPhase {

        void render(GameScreen screen, float delta);

    }

}
