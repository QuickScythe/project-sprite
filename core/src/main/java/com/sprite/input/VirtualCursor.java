package com.sprite.input;

import com.badlogic.gdx.math.Vector3;
import com.sprite.data.utils.Utils;
import com.sprite.game.ItemStack;
import com.sprite.render.screen.GameScreen;
import com.sprite.resource.texture.GameSprite;

/**
 * Represents a virtual cursor shared by all input providers.
 * For mouse/keyboard it mirrors the OS mouse; for controllers it can be moved by sticks/dpad.
 */
public class VirtualCursor {

    private ItemStack holding = null;
    private float x; // screen-space X (pixels)
    private float y; // screen-space Y (pixels)
    private boolean visible = true;
    private final GameSprite cursor;
    private final float size = 75f;

    public VirtualCursor() {
        this.cursor = Utils.resources().TEXTURES.load("textures:cursor");
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void set(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean hold(ItemStack stack) {
        if (holding == null) {
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
     *
     * @param screen The screen to draw to
     */
    public void draw(GameScreen screen) {
        Vector3 mouse = screen.camera().unproject(new Vector3(x, y, 0f));
        screen.sprite().draw(cursor.sprite(), mouse.x - size/2f, mouse.y - size/2f, size, size);
        if (screen.ui().isOpen()) {
            if (holding != null) {
                Vector3 v = new Vector3(x, y, 0f);
                Vector3 pos = screen.camera().unproject(v);


                screen.sprite().draw(holding.type().texture().sprite(), pos.x - size, pos.y - size, size*2, size*2);
                if (holding.amount() > 1)
                    screen.font().draw(screen.sprite(), holding.amount() + "", pos.x + (size / 3f), pos.y - (size / 3f));
            }
        }
    }

    public float width(){
        return size;
    }

    public float height(){
        return size;
    }
}
