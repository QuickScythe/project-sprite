package com.sprite.render.ui.inventory;

import com.badlogic.gdx.math.Vector2;
import com.sprite.game.ItemStack;
import com.sprite.input.InputSystem;
import com.sprite.input.VirtualCursor;
import com.sprite.render.screen.GameScreen;
import com.sprite.render.ui.UI;
import com.sprite.resource.ui.InventoryUI;

import java.util.HashMap;
import java.util.Map;

public class Inventory extends UI<InventoryUI> {

    Map<Integer, ItemStack> items = new HashMap<>();

    public Inventory(InventoryUI def) {
        super(def);
    }


    public void draw(GameScreen screen) {
        type().draw(screen);
        VirtualCursor cursor = InputSystem.i().cursor();
        cursor.draw(screen);
        int clickedIndex = type().getClickedIndex(screen);
        if (clickedIndex != -1) {
            if (items.containsKey(clickedIndex)) {
                ItemStack stack = items.get(clickedIndex);
                if (cursor.hold(stack)) {
                    items.remove(clickedIndex);
                } else if (stack.equals(cursor.holding())) {
                    ItemStack newStack = cursor.release();
                    newStack.amount(newStack.amount() + stack.amount());
                    items.put(clickedIndex, newStack);
                }

            } else {
                if (cursor.holding() != null) {
                    items.put(clickedIndex, cursor.release());
                }
            }
        }

        for (Map.Entry<Integer, ItemStack> entry : items.entrySet()) {
            ItemStack itemStack = entry.getValue();
            int index = entry.getKey();
            Vector2 pos = type().position(screen, index);
            Vector2 cam = type().cameraOrigin(screen);
            if (!(0 <= index && index < type().rows() * type().columns()))
                System.out.println("Inventory out of bounds: " + index);
            screen.sprite().draw(
                itemStack.type().texture().sprite(),
                pos.x + cam.x,
                pos.y + cam.y,
                type().size() * type().scale(),
                type().size() * type().scale());
            if (itemStack.amount() > 1) {
                screen.font().draw(screen.sprite(),
                    Integer.toString(itemStack.amount()),
                    pos.x + cam.x + type().size() * type().scale() / 2,
                    pos.y + cam.y + type().size() * type().scale() / 2
                );
            }
        }
    }

    @Override
    public void debug(GameScreen screen) {

    }


    public void put(int i, ItemStack itemStack) {
        if (type().hidden(i)) return;
        items.put(i, itemStack);
    }

    public void add(ItemStack itemStack) {
        for (int i = 0; i < items.size(); i++) {
            if (items.containsKey(i)) continue;
            put(i, itemStack);
            return;
        }
        put(items.size(), itemStack);
    }
}
