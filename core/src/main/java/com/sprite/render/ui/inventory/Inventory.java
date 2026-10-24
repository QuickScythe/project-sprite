package com.sprite.render.ui.inventory;

import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Vector2;
import com.sprite.game.items.ItemStack;
import com.sprite.game.ui.UIGraphics;
import com.sprite.input.InputSystem;
import com.sprite.input.VirtualCursor;
import com.sprite.render.screen.GameScreen;
import com.sprite.render.ui.UI;
import com.sprite.resource.items.ItemType;
import com.sprite.resource.ui.InventoryUI;

import java.util.HashMap;
import java.util.Map;

public class Inventory extends UI<InventoryUI> {

    Map<Integer, ItemStack> items = new HashMap<>();
    private final GlyphLayout layout = new GlyphLayout();

    public Inventory(InventoryUI def) {
        super(def);
    }


    public void draw(GameScreen screen) {
        type().draw(screen);
        VirtualCursor cursor = InputSystem.i().cursor();
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

        Vector2 cam = type().cameraOrigin(screen);

        for (Map.Entry<Integer, ItemStack> entry : items.entrySet()) {
            ItemStack itemStack = entry.getValue();
            int index = entry.getKey();
            Vector2 pos = type().position(screen, index);
            if (!(0 <= index && index < type().rows() * type().columns()))
                System.out.println("Inventory out of bounds: " + index);

            float itemX = pos.x + cam.x;
            float itemY = pos.y + cam.y;
            float itemSize = type().size() * type().scale();

            if (itemStack.type() != null && itemStack.type().texture() != null) {
                screen.sprite().draw(
                    itemStack.type().texture().sprite(),
                    itemX,
                    itemY,
                    itemSize,
                    itemSize);
            } else if (UIGraphics.get().isAvailable()) {
                UIGraphics.get().drawItemIcon(
                    screen.sprite(),
                    itemStack,
                    itemX,
                    itemY,
                    itemSize,
                    itemSize);
            }

            if (itemStack.amount() > 1) {
                screen.font().getData().setScale(1.25f);
                screen.font().draw(screen.sprite(),
                    Integer.toString(itemStack.amount()),
                    itemX + itemSize / 1.3f,
                    itemY + itemSize / 4
                );
                screen.font().getData().setScale(2.0f);
            }
        }

        // Draw Tooltip for Hovered Item Slot
        int hoveredIndex = type().getHoveredVisibleIndex(screen);
        if (hoveredIndex != -1 && items.containsKey(hoveredIndex) && cursor.holding() == null) {
            ItemStack hoveredStack = items.get(hoveredIndex);
            Vector2 hoveredPos = type().position(screen, hoveredIndex);
            float targetX = hoveredPos.x + cam.x + type().size() * type().scale();
            float targetY = hoveredPos.y + cam.y + (type().size() * type().scale() / 2f);

            float minX = cam.x;
            float minY = cam.y;
            float maxX = cam.x + screen.camera().viewportWidth;
            float maxY = cam.y + screen.camera().viewportHeight;

            if (UIGraphics.get().isAvailable()) {
                UIGraphics.get().drawItemTooltip(
                    screen.sprite(),
                    screen.font(),
                    layout,
                    hoveredStack,
                    targetX,
                    targetY,
                    minX,
                    minY,
                    maxX,
                    maxY
                );
            }
        }
    }

    @Override
    public void debug(GameScreen screen) {

    }

    public int capacity() {
        if (type() != null) {
            return type().columns() * type().rows();
        }
        return 36;
    }

    public boolean isFull() {
        int cap = capacity();
        for (int i = 0; i < cap; i++) {
            if (type() != null && type().hidden(i)) continue;
            if (!items.containsKey(i)) return false;
        }
        return true;
    }

    public boolean canAdd(ItemStack itemStack) {
        if (itemStack == null || itemStack.amount() <= 0) return false;
        for (ItemStack existing : items.values()) {
            if (existing != null && existing.canStackWith(itemStack)) {
                return true;
            }
        }
        return !isFull();
    }

    public void put(int i, ItemStack itemStack) {
        if (type() != null && type().hidden(i)) return;
        if (i < 0 || i >= capacity()) return;
        if (itemStack == null) {
            items.remove(i);
        } else {
            items.put(i, itemStack);
        }
    }

    public boolean add(ItemStack itemStack) {
        if (itemStack == null || itemStack.amount() <= 0) return false;

        // 1. Try to stack with an existing item in inventory
        for (Map.Entry<Integer, ItemStack> entry : items.entrySet()) {
            ItemStack existing = entry.getValue();
            if (existing != null && existing.canStackWith(itemStack)) {
                existing.amount(existing.amount() + itemStack.amount());
                return true;
            }
        }

        // 2. Otherwise find the first available non-hidden slot
        int cap = capacity();
        for (int i = 0; i < cap; i++) {
            if (type() != null && type().hidden(i)) continue;
            if (!items.containsKey(i)) {
                put(i, itemStack);
                return true;
            }
        }

        // 3. Inventory is full and item cannot stack
        return false;
    }

    public ItemStack get(int slot) {
        return items.get(slot);
    }

    public ItemStack remove(int slot) {
        return items.remove(slot);
    }

    public Map<Integer, ItemStack> items() {
        return items;
    }

    public void clear() {
        items.clear();
    }

    public int size() {
        return items.size();
    }

    public boolean contains(int slot) {
        return items.containsKey(slot);
    }

    public int count(String itemId) {
        if (itemId == null) return 0;
        int total = 0;
        String search = itemId.toLowerCase().replace("items:", "");
        for (ItemStack stack : items.values()) {
            if (stack == null || stack.amount() <= 0) continue;
            String stackId = stack.id().toLowerCase().replace("items:", "");
            String stackName = stack.name() != null ? stack.name().key().toLowerCase() : "";
            if (stackId.equals(search) || stackName.equals(search) || stackId.contains(search) || stackName.contains(search)) {
                total += stack.amount();
            }
        }
        return total;
    }

//    public int count(Item item) {
//        if (item == null) return 0;
//        return count(item.id());
//    }
//
//    public int count(ItemType itemType) {
//        if (itemType == null) return 0;
//        return count(itemType.id());
//    }
}
