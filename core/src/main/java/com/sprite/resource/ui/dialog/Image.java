package com.sprite.resource.ui.dialog;

import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Vector2;
import com.sprite.data.utils.Utils;
import com.sprite.data.utils.placeholder.PlaceholderUtils;
import com.sprite.render.screen.GameScreen;
import com.sprite.resource.texture.GameSprite;
import com.sprite.resource.ui.DialogUI;

public class Image extends DialogElement{

    GameSprite sprite;

    public Image(String name, String textureLocation, Vector2 position, Vector2 size, DialogUI dialog) {
        super(name, name, position, size, dialog);
        sprite = Utils.resources().TEXTURES.load(textureLocation);
    }

    @Override
    public void draw(GameScreen screen, float x, float y, float width, float height) {
        float centerX = x + (width / 2);
        float centerY = y + (height / 2);
        screen.sprite().draw(sprite.sprite(), centerX - (width / 2), centerY - (height / 2), width, height);
        // draw so that the t//ext is centered on (centerX, centerY)
//        screen.font().draw(screen.sprite(), layout, centerX - layout.width / 2f, centerY + layout.height / 2f);
    }
}
