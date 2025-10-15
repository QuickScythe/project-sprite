package com.sprite.render.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.ScreenUtils;
import com.sprite.data.registries.Registries;
import com.sprite.magic.elements.Element;
import com.sprite.magic.elements.Elements;
import com.sprite.magic.spells.Spell;
import com.sprite.magic.spells.Spells;
import com.sprite.resource.entities.EntityType;
import com.sprite.ui.InventoryOverlay;
import com.sprite.utils.Utils;
import com.sprite.world.World;
import com.sprite.world.entities.Entity;
import org.json.JSONObject;

import java.util.Objects;
import java.util.Random;

/**
 * First screen of the application. Displayed after the application is created.
 */
public class WorldScreen extends GameScreen {

    World world = new World();
    private InventoryOverlay inventory;

    @Override
    public void show() {
        super.show();
        // Build a reusable overlay using the shared UI system
        inventory = new InventoryOverlay(ui().skin());
        inventory.setVisible(false);
        ui().stage().addActor(inventory);
        // Position near top-left
        inventory.setPosition(20, ui().stage().getViewport().getWorldHeight() - inventory.getHeight() - 20);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);
        // Toggle overlay
        if (Gdx.input.isKeyJustPressed(Input.Keys.I)) {
            inventory.setVisible(!inventory.isVisible());
            if (inventory.isVisible()) inventory.toFront();
        }

        // Gameplay interactions
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            EntityType playerType = Utils.resources().ENTITIES.load("entities:player");
            Entity player = world.spawn(playerType, new Random().nextInt((int) camera().viewportWidth), 200);
            player.health(10);
            player.director.animation("step");
            System.out.println(player.controller.extra().toString(2));

            JSONObject data = Registries.dump();
            System.out.println("Registries dump:");
            System.out.println(data.toString(2));
            camera().focus(player);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            for(Entity entity : world.entities) {
                world.applyForce(entity, 50, -100);
                entity.director.animation("idle");
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.A)) {
            int size = new Random().nextInt(6);
            Element[] elements = new Element[size];
            for (int i = 0; i < size; i++) {
                elements[i] = (Element) Elements.values().toArray()[new Random().nextInt(Elements.values().size())];
            }
            Spell[] spells = Spells.generate(elements);

            String msg = "";
            for (Spell spell : spells) {
                msg = Objects.equals(msg, "") ? spell.name() : msg + ", " + spell.name();
            }
            String elems = "";
            for (Element e : elements) {
                elems = Objects.equals(elems, "") ? e.name() : elems + ", " + e.name();
            }
            if (spells.length == 0) {
                System.out.println("No spell was cast. You used: " + elems);
                return;
            }
            System.out.println("You cast: " + msg + "! You used: " + elems);
        }

        camera().update();

        // Render world
        world.render(this);
        // Render UI
        ui().actAndDraw(delta);
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        if (width <= 0 || height <= 0) return;
        if (inventory != null) {
            inventory.setPosition(20, ui().stage().getViewport().getWorldHeight() - inventory.getHeight() - 20);
        }
    }

    @Override
    public void pause() { }

    @Override
    public void resume() { }

    @Override
    public void hide() { super.hide(); }

    @Override
    public void dispose() {
        super.dispose();
    }
}
