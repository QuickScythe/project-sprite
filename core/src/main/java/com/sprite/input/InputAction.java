package com.sprite.input;

/**
 * High-level input actions abstracted from concrete devices.
 * This enables supporting mouse/keyboard today and gamepads/controllers later
 * without changing UI or game logic.
 */
public enum InputAction {
    Primary,        // e.g., left click, A/Cross
    Secondary,      // e.g., right click, B/Circle
    Submit,         // e.g., Enter/Space, Start
    Cancel,         // e.g., Escape, Back

    MoveUp,
    MoveDown,
    MoveLeft,
    MoveRight
}
