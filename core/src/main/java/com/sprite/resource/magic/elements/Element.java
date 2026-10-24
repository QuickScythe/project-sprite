package com.sprite.resource.magic.elements;

import com.badlogic.gdx.graphics.Color;

public interface Element {

    String name();

    int cost();

    int cooldown();

    default String id() {
        return name();
    }

    default Color color() {
        String n = name() != null ? name().toLowerCase() : "";
        if (n.contains("fire")) return new Color(1.0f, 0.35f, 0.1f, 1f);
        if (n.contains("earth")) return new Color(0.45f, 0.75f, 0.25f, 1f);
        if (n.contains("wind") || n.contains("air")) return new Color(0.35f, 0.85f, 0.95f, 1f);
        if (n.contains("water") || n.contains("frost") || n.contains("ice")) return new Color(0.2f, 0.6f, 1.0f, 1f);
        if (n.contains("light") || n.contains("holy")) return new Color(1.0f, 0.95f, 0.4f, 1f);
        if (n.contains("dark") || n.contains("shadow")) return new Color(0.6f, 0.2f, 0.8f, 1f);
        return new Color(0.8f, 0.6f, 1.0f, 1f);
    }
}
