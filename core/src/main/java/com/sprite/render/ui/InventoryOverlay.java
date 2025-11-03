package com.sprite.render.ui;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Window;

/**
 * Example reusable overlay UI that can be shown/hidden during gameplay.
 */
public class InventoryOverlay extends Window {

    public InventoryOverlay(Skin skin) {
        super("Inventory", skin);
        pad(10);

        Table content = new Table(skin);
        content.defaults().pad(4);
        content.add(new Label("- Potion x3", skin)).left().row();
        content.add(new Label("- Sword", skin)).left().row();
        content.add(new Label("- Shield", skin)).left().row();

        add(content).left().row();
        pack();
        setSize(getWidth(), getHeight());
        setMovable(true);
    }
}
