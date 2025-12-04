package com.sprite.resource.ui.dialog;

import com.badlogic.gdx.math.Vector2;
import com.sprite.data.TranslatableString;
import com.sprite.data.utils.resources.Texts;
import com.sprite.render.screen.GameScreen;
import com.sprite.resource.ui.DialogUI;

/**
 * Base UI element used by DialogUI. Holds grid position/size and transient interaction state.
 */
public abstract class DialogElement  {

    final TranslatableString title;
    private final String name;
    private final Vector2 position;
    private final Vector2 size;
    private final DialogUI dialog;

    // Transient per-frame interaction state
    private boolean hovered;
    private boolean pressed;
    private boolean justClicked;
    // Edge-triggered flags computed from previous frame
    private boolean justHovered;
    private boolean justPressed;
    // Previous state trackers to compute edge events
    private boolean prevHovered;
    private boolean prevPressed;

    public DialogElement(String name, String translationKey, Vector2 position, Vector2 size, DialogUI dialog) {
        this.name = name;
        this.position = position;
        this.size = size;
        this.title = new TranslatableString(translationKey);
        this.dialog = dialog;
    }

    public DialogUI dialog() { return dialog; }

    /**
     * Called by rendering/input pass each frame to update interaction flags.
     * Also computes edge-triggered events like just-hovered and just-pressed, which
     * are true for exactly one frame on the transition from false to true.
     */
    public void setInteractionState(boolean hovered, boolean pressed, boolean justClicked) {
        // Compute edge events based on previous frame before overwriting current state
        this.justHovered = hovered && !this.prevHovered;
        this.justPressed = pressed && !this.prevPressed;

        // Store current state
        this.hovered = hovered;
        this.pressed = pressed;
        this.justClicked = justClicked;

        // Update previous trackers for next frame's edge computation
        this.prevHovered = hovered;
        this.prevPressed = pressed;
    }

    public boolean isHovered() { return hovered; }
    public boolean isPressed() { return pressed; }
    public boolean isJustClicked() { return justClicked; }
    /** True only on the frame the cursor first hovers this element. */
    public boolean isJustHovered() { return justHovered; }
    /** True only on the frame the primary press begins on this element. */
    public boolean isJustPressed() { return justPressed; }

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

    public abstract void draw(GameScreen screen, float x, float y, float width, float height);

    public void debug(GameScreen screen, float x, float y, float w, float h) {
        screen.shape().rect(x, y, w, h);
    }
}
