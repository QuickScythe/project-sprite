package com.sprite.render.ui.inventory;

import com.badlogic.gdx.math.Vector2;
import com.sprite.game.ItemStack;
import com.sprite.input.InputSystem;
import com.sprite.input.VirtualCursor;
import com.sprite.render.screen.GameScreen;
import com.sprite.render.ui.UI;
import com.sprite.resource.ui.InventoryUI;

import java.awt.*;
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
        if(clickedIndex != -1){
            if(items.containsKey(clickedIndex)){
                ItemStack stack = items.get(clickedIndex);
                if(cursor.hold(stack)){
                    items.remove(clickedIndex);
                }

            } else {
                if(cursor.holding() != null){
                    items.put(clickedIndex, cursor.release());
                }
            }
        }

        for (Map.Entry<Integer, ItemStack> entry : items.entrySet()) {
            ItemStack itemStack = entry.getValue();
            int index = entry.getKey();
            Vector2 pos = type().position(screen, index);
            if (!(0 <= index && index < type().rows() * type().columns()))
                System.out.println("Inventory out of bounds: " + index);
            screen.sprite().draw(
                itemStack.type().texture().sprite(),
                pos.x + screen.camera().position.x,
                pos.y + screen.camera().position.y,
                type().size() * type().scale(),
                type().size() * type().scale());
        }
    }

    @Override
    public void debug(GameScreen screen) {

    }


    public void put(int i, ItemStack itemStack) {
        if (type().hidden(i)) return;
        items.put(i, itemStack);
    }
}
