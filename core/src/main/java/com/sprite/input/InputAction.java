package com.sprite.input;

/**
 * High-level input actions abstracted from concrete devices.
 * This enables supporting mouse/keyboard today and gamepads/controllers later
 * without changing UI or game logic.
 */
public enum InputAction {
    PRIMARY,        // e.g., left click, A/Cross
    SECONDARY,      // e.g., right click, B/Circle
    SUBMIT,         // e.g., Enter/Space, Start
    CANCEL,         // e.g., Escape, Back

    MOVE_UP,
    MOVE_DOWN,
    MOVE_LEFT,
    MOVE_RIGHT,
    JUMP,
    INVENTORY
}
