package com.sprite.resource.ui.dialog;

import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Vector2;
import com.sprite.data.TranslatableString;
import com.sprite.data.utils.Texts;
import com.sprite.data.utils.Utils;
import com.sprite.render.screen.GameScreen;
import com.sprite.resource.texture.NineSliceSprite;
import com.sprite.resource.ui.DialogUI;
import com.sprite.resource.ui.UIAction;

public class Button extends DialogElement {

    private final NineSliceSprite up;
    private final NineSliceSprite down;
    private final NineSliceSprite hover;
    private final UIAction action;
    private final Label label;

    public Button(String name, String translationKey, Vector2 position, Vector2 size, UIAction action, DialogUI dialog) {
        super(name, translationKey, position, size, dialog);
        this.action = action;

        this.up = new NineSliceSprite(Utils.resources().TEXTURES.load("textures:ui/button"), 48, 48, 48, 48);
        this.down = new NineSliceSprite(Utils.resources().TEXTURES.load("textures:ui/button_pressed"), 48, 48, 48, 48);
        this.hover = new NineSliceSprite(Utils.resources().TEXTURES.load("textures:ui/button_hover"), 48, 48, 48, 48);

        label = new Label(name, translationKey, position, size, dialog);
    }



    public UIAction action() {
        return action;
    }

    public void click() {
        if (action == null) return;
        action.invoke();
    }

    public NineSliceSprite up() {
        return up;
    }

    public NineSliceSprite down() {
        return down;
    }

    public NineSliceSprite hover() {
        return hover;
    }

    @Override
    public void draw(GameScreen screen, float x, float y, float width, float height) {
        if (isPressed()) {
            down().draw(screen.sprite(), x, y, width, height);
        } else if (isHovered()) {
            hover().draw(screen.sprite(), x, y, width, height);
        } else {
            up().draw(screen.sprite(), x, y, width, height);
        }
        if (isJustClicked()) {
            click();
        }
        label.draw(screen, x, y, width, height);
    }
}
