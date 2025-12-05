package com.sprite.input;

/**
 * @param keyboard Keyboard if true, Mouse if false
 */
public record Keybind(InputAction action, boolean keyboard, Integer key) {

}
