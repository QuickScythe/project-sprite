package com.sprite.input;

/**
 * Pluggable input provider interface. Implementations may use mouse/keyboard,
 * controllers, touch, or any combination to drive a common set of actions and a virtual cursor.
 */
public interface InputProvider {
    /** Update provider state each frame. */
    void update(float delta);

    /** Shared virtual cursor in screen space (pixels). */
    VirtualCursor getCursor();

    /** Returns true while the action is held down. */
    boolean isPressed(InputAction action);

    /** Returns true only on the frame the action becomes pressed. */
    boolean isJustPressed(InputAction action);
}
