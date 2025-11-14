package com.sprite.render.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.sprite.data.utils.Utils;
import com.sprite.render.screen.GameScreen;
import com.sprite.resource.texture.GameSprite;

public class InventoryUI extends UIType {

    GameSprite texture = null;

    @Override
    public void draw(GameScreen screen, UIDefinition uiDefinition) {
        if(texture == null){
            texture = Utils.resources().TEXTURES.load(uiDefinition.texture.toString());
        }
        float textureWidth = texture.width()*10;
        float textureHeight = texture.height()*10;
        screen.sprite().draw(texture.sprite(), (screen.camera().viewportWidth/2)-(textureWidth /2), (screen.camera().viewportHeight/2)-(textureHeight /2), textureWidth, textureHeight);

    }
}
