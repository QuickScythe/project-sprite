package com.sprite.render.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;

/**
 * Builds a minimal programmatic Skin to avoid bundling external uiskin.json assets.
 */
public final class UISkinFactory {
    private UISkinFactory() {}

    public static Skin createDefaultSkin() {
        Skin skin = new Skin();

        // Default font
        BitmapFont font = new BitmapFont();
        skin.add("default", font, BitmapFont.class);

        // Simple 1x1 white texture
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.WHITE);
        pm.fill();
        Texture whiteTex = new Texture(pm);
        pm.dispose();
        // Add the texture to the skin so it will be disposed with the skin
        skin.add("white", whiteTex);

        // Create 9-patches for button up/down/over and panel backgrounds
        NinePatch upPatch = new NinePatch(whiteTex, 0, 0, 0, 0);
        NinePatch downPatch = new NinePatch(whiteTex, 0, 0, 0, 0);
        NinePatch overPatch = new NinePatch(whiteTex, 0, 0, 0, 0);
        NinePatch panelPatch = new NinePatch(whiteTex, 0, 0, 0, 0);
        upPatch.setColor(new Color(0x2e7d32ff));     // green 600
        downPatch.setColor(new Color(0x1b5e20ff));   // green 900
        overPatch.setColor(new Color(0x388e3cff));   // green 700
        panelPatch.setColor(new Color(0x263238ff));  // blue grey 900

        NinePatchDrawable upDrawable = new NinePatchDrawable(upPatch);
        NinePatchDrawable downDrawable = new NinePatchDrawable(downPatch);
        NinePatchDrawable overDrawable = new NinePatchDrawable(overPatch);
        NinePatchDrawable panelDrawable = new NinePatchDrawable(panelPatch);

        // Register drawables by name for optional lookup elsewhere
        skin.add("button-normal", upDrawable);
        skin.add("button-normal-pressed", downDrawable);
        skin.add("button-normal-over", overDrawable);
        skin.add("panel", panelDrawable);

        // Label style
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = font;
        labelStyle.fontColor = Color.WHITE;
        skin.add("default", labelStyle);

        // Window style (needed by InventoryOverlay)
        Window.WindowStyle ws = new Window.WindowStyle();
        ws.titleFont = font;
        ws.titleFontColor = Color.WHITE;
        ws.background = panelDrawable;
        skin.add("default", ws);

        // TextButton style
        TextButton.TextButtonStyle tbs = new TextButton.TextButtonStyle();
        tbs.font = font;
        tbs.up = upDrawable;
        tbs.down = downDrawable;
        tbs.over = overDrawable;
        tbs.fontColor = Color.WHITE;
        skin.add("default", tbs);

        return skin;
    }
}
