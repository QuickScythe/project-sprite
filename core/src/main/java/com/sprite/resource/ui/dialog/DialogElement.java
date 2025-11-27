package com.sprite.resource.ui.dialog;

import com.badlogic.gdx.math.Vector2;
import com.sprite.data.TranslatableString;
import com.sprite.data.utils.Texts;
import com.sprite.render.screen.GameScreen;

/**
 * Base UI element used by DialogUI. Holds grid position/size and transient interaction state.
 */
public class DialogElement {

    private final TranslatableString title;
    private final String name;
    private final Vector2 position;
    private final Vector2 size;

    // Transient per-frame interaction state
    private boolean hovered;
    private boolean pressed;
    private boolean justClicked;

    public DialogElement(String name, String translationKey, Vector2 position, Vector2 size) {
        this.name = name;
        this.position = position;
        this.size = size;
        this.title = new TranslatableString(translationKey);
    }

    /** Called by rendering/input pass each frame to update interaction flags. */
    public void setInteractionState(boolean hovered, boolean pressed, boolean justClicked) {
        this.hovered = hovered;
        this.pressed = pressed;
        this.justClicked = justClicked;
    }

    public boolean isHovered() { return hovered; }
    public boolean isPressed() { return pressed; }
    public boolean isJustClicked() { return justClicked; }

    public String name() {
        return name;
    }

    public Vector2 position() {
        return position;
    }

    public Vector2 size() {
        return size;
    }

    public String title() {
        return Texts.get(title);
    }

    public void draw(GameScreen screen, float x, float y, float width, float height) {

    }
}
