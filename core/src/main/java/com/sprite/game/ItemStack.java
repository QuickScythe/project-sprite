package com.sprite.game;

import com.sprite.data.utils.Utils;
import com.sprite.resource.items.ItemType;

public class ItemStack {

    private final ItemType type;
    private int amount;

    public ItemStack(String type) {
        this(Utils.resources().ITEMS.load(type), 1);
    }

    public ItemStack(String type, int amount) {
        this(Utils.resources().ITEMS.load(type), amount);
    }

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

    public int amount() {
        return amount;
    }

    public void amount(int amount) {
        this.amount = amount;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ItemStack itemStack) return itemStack.type == type;
        return false;
    }
}
