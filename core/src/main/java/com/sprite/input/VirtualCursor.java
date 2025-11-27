package com.sprite.input;

import com.badlogic.gdx.math.Vector3;
import com.sprite.game.ItemStack;
import com.sprite.render.screen.GameScreen;

/**
 * Represents a virtual cursor shared by all input providers.
 * For mouse/keyboard it mirrors the OS mouse; for controllers it can be moved by sticks/dpad.
 */
public class VirtualCursor {

    private ItemStack holding = null;
    private float x; // screen-space X (pixels)
    private float y; // screen-space Y (pixels)
    private boolean visible = true;

    public float getX() { return x; }
    public float getY() { return y; }
    public void set(float x, float y) { this.x = x; this.y = y; }

    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }

    public boolean hold(ItemStack stack) {
        if(holding == null){
            holding = stack;
            return true;
        }
        return false;
    }

    public ItemStack holding() {
        return holding;
    }

    public ItemStack release() {
        ItemStack stack = holding;
        holding = null;
        return stack;
    }


    /**
     * Draws the virtual cursor. SpriteBatch must have already been began.
     * @param screen    The screen to draw to
     */
    public void draw(GameScreen screen) {
        if(holding != null){
            Vector3 v = new Vector3(x, y, 0f);
            Vector3 pos = screen.camera().unproject(v);
            int size = 150;


            screen.sprite().draw(holding.type().texture().sprite(), pos.x-(size/2f), pos.y-(size/2f), size, size);
        }
    }
}
