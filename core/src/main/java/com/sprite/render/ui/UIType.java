package com.sprite.render.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.sprite.render.screen.GameScreen;

import java.rmi.server.UID;

public abstract class UIType {

    public abstract void draw(GameScreen screen, UIDefinition uiDefinition);
}
