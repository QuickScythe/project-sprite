package com.sprite.resource.ui.dialog;

import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Vector2;
import com.sprite.render.screen.GameScreen;
import com.sprite.resource.ui.DialogUI;

public class Label extends DialogElement {


    public Label(String name, String translationKey, Vector2 position, Vector2 size, DialogUI dialog) {
        super(name, translationKey, position, size, dialog);
    }

    @Override
    public void draw(GameScreen screen, float x, float y, float width, float height) {
        float centerX = x + (width / 2);
        float centerY = y + (height / 2);
        GlyphLayout layout = new GlyphLayout(screen.font(), title());
        // draw so that the text is centered on (centerX, centerY)
        screen.font().draw(screen.sprite(), layout, centerX - layout.width / 2f, centerY + layout.height / 2f);
    }
}
