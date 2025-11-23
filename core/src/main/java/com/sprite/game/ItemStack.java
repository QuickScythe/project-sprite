package com.sprite.game;

import com.sprite.resource.items.ItemType;

public class ItemStack {

    private final ItemType type;
    int amount;

    public ItemStack(ItemType type) {
        this(type,1);
    }

    public ItemStack(ItemType type, int amount) {
        if(type == null) throw new IllegalArgumentException("ItemType cannot be null");
        this.type = type;
        this.amount = amount;
    }

    public ItemType type() {
        return type;
    }
}
