package com.sprite.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

import java.util.EnumMap;
import java.util.Map;

/**
 * Default input provider using LibGDX mouse and keyboard.
 * It exposes high-level actions and a virtual cursor that mirrors the OS mouse.
 */
public class MouseKeyboardInputProvider implements InputProvider {

    private final VirtualCursor cursor = new VirtualCursor();
    private final Map<InputAction, Boolean> current = new EnumMap<>(InputAction.class);
    private final Map<InputAction, Boolean> previous = new EnumMap<>(InputAction.class);

    public MouseKeyboardInputProvider() {
        for (InputAction a : InputAction.values()) {
            current.put(a, false);
            previous.put(a, false);
        }
    }

    @Override
    public void update(float delta) {
        // Update cursor position from actual mouse in screen space
        cursor.set(Gdx.input.getX(), Gdx.input.getY());

        // Copy current to previous
        for (InputAction a : InputAction.values()) previous.put(a, current.get(a));

        // Recompute current state from GDX
        current.put(InputAction.Primary, Gdx.input.isButtonPressed(Input.Buttons.LEFT));
        current.put(InputAction.Secondary, Gdx.input.isButtonPressed(Input.Buttons.RIGHT));

        boolean submit = Gdx.input.isKeyPressed(Input.Keys.ENTER) || Gdx.input.isKeyPressed(Input.Keys.SPACE);
        current.put(InputAction.Submit, submit);

        boolean cancel = Gdx.input.isKeyPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyPressed(Input.Keys.BACKSPACE);
        current.put(InputAction.Cancel, cancel);

        boolean up = Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP);
        boolean down = Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN);
        boolean left = Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT);
        boolean right = Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT);
        current.put(InputAction.MoveUp, up);
        current.put(InputAction.MoveDown, down);
        current.put(InputAction.MoveLeft, left);
        current.put(InputAction.MoveRight, right);
    }

    @Override
    public VirtualCursor getCursor() {
        return cursor;
    }

    @Override
    public boolean isPressed(InputAction action) {
        return Boolean.TRUE.equals(current.get(action));
    }

    @Override
    public boolean isJustPressed(InputAction action) {
        return !Boolean.TRUE.equals(previous.get(action)) && Boolean.TRUE.equals(current.get(action));
    }
}
