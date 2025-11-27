package com.sprite.resource.texture;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Lightweight wrapper around LibGDX NinePatch for 9-slice rendering.
 *
 * Usage:
 *  NineSliceSprite nine = new NineSliceSprite(gameSprite, 6, 6, 6, 6);
 *  nine.draw(batch, x, y, width, height);
 */
public class NineSliceSprite {

    private final NinePatch patch;
    private final int left, right, top, bottom;

    /**
     * Creates a 9-slice from a GameSprite using explicit split sizes (in pixels).
     * The non-stretchable corner sizes are: left, right, top, bottom.
     */
    public NineSliceSprite(GameSprite sprite, int left, int right, int top, int bottom) {
        this(new TextureRegion(sprite.sprite()), left, right, top, bottom);
    }

    /**
     * Creates a 9-slice directly from a Texture using explicit split sizes (in pixels).
     */
    public NineSliceSprite(Texture texture, int left, int right, int top, int bottom) {
        this(new TextureRegion(texture), left, right, top, bottom);
    }

    /**
     * Creates a 9-slice from a TextureRegion using explicit split sizes (in pixels).
     */
    public NineSliceSprite(TextureRegion region, int left, int right, int top, int bottom) {
        this.left = Math.max(0, left);
        this.right = Math.max(0, right);
        this.top = Math.max(0, top);
        this.bottom = Math.max(0, bottom);
        this.patch = new NinePatch(region, this.left, this.right, this.top, this.bottom);
    }

    /** Draws the 9-slice at the given destination rectangle. */
    public void draw(SpriteBatch batch, float x, float y, float width, float height) {
        if (batch == null) return;
        if (width <= 0 || height <= 0) return;
        patch.draw(batch, x, y, width, height);
    }

    public NinePatch patch() { return patch; }

    public int left() { return left; }
    public int right() { return right; }
    public int top() { return top; }
    public int bottom() { return bottom; }
}
