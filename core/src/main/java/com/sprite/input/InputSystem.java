package com.sprite.input;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;

/**
 * Centralized input facade. Holds an active InputProvider and exposes a stable API for the game/UI.
 * Defaults to mouse/keyboard provider but can be swapped for controller-based providers.
 */
public final class InputSystem {
    private static final InputSystem INSTANCE = new InputSystem();

    public static InputSystem i() { return INSTANCE; }

    private InputProvider provider;

    private InputSystem() {
        this.provider = new MouseKeyboardInputProvider();
    }

    public void setProvider(InputProvider provider) {
        if (provider != null) this.provider = provider;
    }

    public InputProvider getProvider() { return provider; }

    public void update(float delta) { provider.update(delta); }

    public VirtualCursor cursor() { return provider.getCursor(); }
    public float cursorX() { return provider.getCursor().getX(); }
    public float cursorY() { return provider.getCursor().getY(); }

    public boolean isActionPressed(InputAction action) { return provider.isPressed(action); }
    public boolean isActionJustPressed(InputAction action) { return provider.isJustPressed(action); }

    /** Convenience: returns world-space cursor using the given camera. */
    public Vector3 worldCursor(Camera camera) {
        Vector3 v = new Vector3(cursorX(), cursorY(), 0f);
        camera.unproject(v);
        return v;
    }
}
