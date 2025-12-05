package com.sprite.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;

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
    private final Map<InputAction, Keybind> keybinds = new EnumMap<>(InputAction.class);

    public MouseKeyboardInputProvider() {
        Preferences prefs = Gdx.app.getPreferences("keybinds");
        keybinds.put(InputAction.MOVE_UP, new Keybind(InputAction.MOVE_UP, true, prefs.getInteger("move_up", Input.Keys.W)));
        keybinds.put(InputAction.MOVE_DOWN, new Keybind(InputAction.MOVE_DOWN, true, prefs.getInteger("move_down", Input.Keys.S)));
        keybinds.put(InputAction.MOVE_LEFT, new Keybind(InputAction.MOVE_LEFT, true, prefs.getInteger("move_left", Input.Keys.A)));
        keybinds.put(InputAction.MOVE_RIGHT, new Keybind(InputAction.MOVE_RIGHT, true, prefs.getInteger("move_right", Input.Keys.D)));
        keybinds.put(InputAction.JUMP, new Keybind(InputAction.JUMP, true, prefs.getInteger("jump", Input.Keys.SPACE)));
        keybinds.put(InputAction.INVENTORY, new Keybind(InputAction.INVENTORY, true, prefs.getInteger("inventory", Input.Keys.E)));
        keybinds.put(InputAction.CANCEL, new Keybind(InputAction.CANCEL, true, prefs.getInteger("cancel", Input.Keys.ESCAPE)));
        keybinds.put(InputAction.SUBMIT, new Keybind(InputAction.SUBMIT, true, prefs.getInteger("submit", Input.Keys.ENTER)));
        keybinds.put(InputAction.PRIMARY, new Keybind(InputAction.PRIMARY, false, prefs.getInteger("primary", Input.Buttons.LEFT)));
        keybinds.put(InputAction.SECONDARY, new Keybind(InputAction.SECONDARY, false, prefs.getInteger("secondary", Input.Buttons.RIGHT)));
        prefs.flush();


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

        for (Keybind k : keybinds.values()) {
            if (k.keyboard()) {
                current.put(k.action(), Gdx.input.isKeyPressed(k.key()));
            } else {
                current.put(k.action(), Gdx.input.isButtonPressed(k.key()));
            }
        }
//
//        // Recompute current state from GDX
//        current.put(InputAction.PRIMARY, Gdx.input.isButtonPressed(Input.Buttons.LEFT));
//        current.put(InputAction.SECONDARY, Gdx.input.isButtonPressed(Input.Buttons.RIGHT));
//
//        boolean submit = Gdx.input.isKeyPressed(Input.Keys.ENTER) || Gdx.input.isKeyPressed(Input.Keys.SPACE);
//        current.put(InputAction.SUBMIT, submit);
//
//        boolean cancel = Gdx.input.isKeyPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyPressed(Input.Keys.BACKSPACE);
//        current.put(InputAction.CANCEL, cancel);
//
//        boolean up = Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP);
//        boolean down = Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN);
//        boolean left = Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT);
//        boolean right = Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT);
//        current.put(InputAction.MOVE_UP, up);
//        current.put(InputAction.MOVE_DOWN, down);
//        current.put(InputAction.MOVE_LEFT, left);
//        current.put(InputAction.MOVE_RIGHT, right);
//
//        current.put(InputAction.JUMP, Gdx.input.isKeyPressed(Input.Keys.SPACE));
//
//        current.put(InputAction.INVENTORY, Gdx.input.isKeyPressed(Input.Keys.E));
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
