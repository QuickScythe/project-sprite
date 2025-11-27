package com.sprite.resource.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.sprite.data.utils.Utils;
import com.sprite.render.screen.GameScreen;
import com.sprite.resource.texture.GameSprite;
import com.sprite.input.InputAction;
import com.sprite.input.InputSystem;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class InventoryUI extends UIType {

    private final List<Integer> hiddenSlots = new ArrayList<>();
    private int size;
    private float uiWidth;
    private float uiHeight;

    public void create(UIDefinition uiDefinition) {
        texture = Utils.resources().TEXTURES.load(uiDefinition.texture().toString());
        textureWidth = texture.width() * scale();
        textureHeight = texture.height() * scale();
        JSONObject uiData = uiDefinition.data().getJSONObject("inventory");
        // width corresponds to number of columns; height corresponds to number of rows
        columns = uiData.getInt("width");
        rows = uiData.getInt("height");
        size = uiData.optInt("size", 16);
        gridWidthSize = size;
        gridHeightSize = size;
        // ui dimensions derive from columns (width) and rows (height)
        uiWidth = columns * size * scale();
        uiHeight = rows * size * scale();
        JSONArray hidden = uiData.optJSONArray("hidden", new JSONArray());
        for (int i = 0; i < hidden.length(); i++) {
            hiddenSlots.add(hidden.getInt(i));
        }
    }

    @Override
    public void draw(GameScreen screen) {
        float camX = (screen.camera().position.x - screen.camera().viewportWidth/2);
        float camY = (screen.camera().position.y - screen.camera().viewportHeight/2);
        screen.sprite().draw(texture.sprite(), camX + (screen.camera().viewportWidth / 2) - (textureWidth / 2), camY + (screen.camera().viewportHeight / 2) - (textureHeight / 2), textureWidth, textureHeight);
        screen.sprite().end();
        screen.shape().setProjectionMatrix(screen.camera().combined);
        screen.shape().begin(ShapeRenderer.ShapeType.Filled);


        // Iterate across visible grid: x in columns (width), y in rows (height)
        for (int x = 0; x < columns; x++) {
            for (int y = 0; y < rows; y++) {
                int index = y * columns + x;
                if (hiddenSlots.contains(index)) continue;
                if (getHoveredIndex(screen) == index) {
                    screen.shape().setColor(new Color(0.75f, 0.75f, 0.75f, 0.0125f));
                    Vector2 pos = position(screen, index);
                    screen.shape().rect(camX+pos.x, camY+pos.y, size * scale(), size * scale());

                }
            }
        }
        screen.shape().end();
        screen.sprite().begin();
    }

    /**
     * Returns the index under the mouse, or -1 if the mouse is outside the inventory grid area.
     * Note: hidden slots may still return their index; use getHoveredVisibleIndex to ignore hidden slots.
     */
    public int getHoveredIndex(GameScreen screen) {
        float cellSize = size * scale();
        float camX = (screen.camera().position.x - screen.camera().viewportWidth/2f);
        float camY = (screen.camera().position.y - screen.camera().viewportHeight/2f);
        float gridX = camX + (screen.camera().viewportWidth / 2f) - (uiWidth / 2f);
        float gridY = camY + (screen.camera().viewportHeight / 2f) - (uiHeight / 2f);

        Vector3 world = InputSystem.i().worldCursor(screen.camera());

        float localX = world.x - gridX;
        float localY = world.y - gridY;

        if (localX < 0 || localY < 0 || localX >= uiWidth || localY >= uiHeight) return -1;

        int x = (int) (localX / cellSize);
        int y = (int) (localY / cellSize);
        return y * columns + x;
    }

    /**
     * Returns hovered index but skips hidden slots; -1 if outside grid or on a hidden cell.
     */
    public int getHoveredVisibleIndex(GameScreen screen) {
        int idx = getHoveredIndex(screen);
        if (idx == -1) return -1;
        return hidden(idx) ? -1 : idx;
    }

    /**
     * True when the mouse cursor is over the inventory grid (not the whole texture), false otherwise.
     */
    public boolean isMouseOverGrid(GameScreen screen) {
        return getHoveredIndex(screen) != -1;
    }

    /**
     * True when the mouse cursor is over the inventory background texture area.
     * This is useful to block world/game input while the UI is hovered.
     */
    public boolean isMouseOverUITexture(GameScreen screen) {
        // Compute texture rect centered in the viewport, projected into world space w.r.t camera
        float camX = (screen.camera().position.x - screen.camera().viewportWidth/2f);
        float camY = (screen.camera().position.y - screen.camera().viewportHeight/2f);
        float left = camX + (screen.camera().viewportWidth / 2f) - (textureWidth / 2f);
        float bottom = camY + (screen.camera().viewportHeight / 2f) - (textureHeight / 2f);

        Vector3 world = InputSystem.i().worldCursor(screen.camera());

        return world.x >= left && world.x <= left + textureWidth &&
               world.y >= bottom && world.y <= bottom + textureHeight;
    }

    /**
     * Returns true if the specified mouse button was just pressed while the cursor is inside the grid.
     */
    public boolean isButtonJustPressedInsideGrid(GameScreen screen, int button) {
        if (!Gdx.input.isButtonJustPressed(button)) return false;
        return getHoveredIndex(screen) != -1;
    }

    /**
     * Convenience overload for left mouse button inside the grid.
     */
    public boolean isLeftClickJustPressedInsideGrid(GameScreen screen) {
        // Use high-level input action so controllers can map to Primary
        if (!InputSystem.i().isActionJustPressed(InputAction.Primary)) return false;
        return getHoveredIndex(screen) != -1;
    }

    /**
     * If the given mouse button was just pressed, returns the hovered visible index; otherwise returns -1.
     * Hidden slots are ignored (return -1) to simplify item picking logic.
     */
    public int getClickedIndex(GameScreen screen, int button) {
        if (!Gdx.input.isButtonJustPressed(button)) return -1;
        return getHoveredVisibleIndex(screen);
    }

    /**
     * Convenience overload for left mouse clicks.
     */
    public int getClickedIndex(GameScreen screen) {
        // Primary action (mouse left or controller confirm)
        if (!InputSystem.i().isActionJustPressed(InputAction.Primary)) return -1;
        return getHoveredVisibleIndex(screen);
    }

    public float uiWidth() {
        return uiWidth;
    }

    public float uiHeight() {
        return uiHeight;
    }

    public int size() {
        return size;
    }

    public int rows() {
        return rows;
    }

    public int columns() {
        return columns;
    }

    public void dispose() {
        texture.dispose();
    }

    public boolean hidden(int index) {
        return hiddenSlots.contains(index);
    }

    public Vector2 position(GameScreen screen, int index) {
        float centerX = screen.camera().viewportWidth / 2f;
        float centerY = screen.camera().viewportHeight / 2f;
        int x = index % columns();
        int y = index / columns();
        return new Vector2(centerX - (uiWidth() / 2) + (x * size() * scale()), centerY - (uiHeight() / 2) + (y * size() * scale()));
    }
    public float scale() {
        return 9f;
    }
}
