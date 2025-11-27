package com.sprite.render.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.ScreenUtils;
import com.sprite.data.registries.Registries;
import com.sprite.data.utils.Utils;
import com.sprite.game.world.World;
import com.sprite.game.world.entities.Entity;
import com.sprite.resource.entities.EntityType;
import com.sprite.resource.magic.elements.Element;
import com.sprite.resource.magic.elements.Elements;
import com.sprite.resource.magic.spells.Spell;
import com.sprite.resource.magic.spells.Spells;
import com.sprite.resource.texture.GameSprite;
import org.json.JSONObject;

import java.util.Objects;
import java.util.Random;

/**
 * First screen of the application. Displayed after the application is created.
 */
public class WorldScreen extends GameScreen {

    World world = new World();
    GameSprite background;

    @Override
    public void create() {
        background = Utils.resources().TEXTURES.load("textures:background");
        pipeline().background((screen, delta) -> {
            screen.sprite().draw(background.sprite(), 0, 0, camera().viewportWidth, camera().viewportHeight);
        });
        pipeline().foreground((screen, delta) -> {
            world.render(screen);
        });
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        if (!ui().isOpen()) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                if (ui().size() == 0)
                    ui().addFirst(ui().build(Utils.resources().USER_INTERFACES.load("ui:player/inventory")));
                ui().open();
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                ui().addFirst(ui().build(Utils.resources().USER_INTERFACES.load("ui:main_menu")));
                ui().open();
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.I)) {
                EntityType playerType = Utils.resources().ENTITIES.load("entities:player");
                Entity player = world.spawn(playerType, new Random().nextInt((int) camera().viewportWidth), 200);
                player.health(10);
                player.director.animation("step");
                System.out.println(player.controller.extra().toString(2));


                camera().focus(player);

                EntityType testType = Utils.resources().ENTITIES.load("entities:test");
                Entity test = world.spawn(testType, new Random().nextInt((int) camera().viewportWidth), 32);
                test.health(10);
                test.director.animation("step");
                System.out.println(test.controller.extra().toString(2));

                JSONObject data = Registries.dump();
                System.out.println("Registries dump:");
                System.out.println(data.toString(2));


            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                world.entities.clear();
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.D)) {
                for (Entity entity : world.entities) {
                    if (entity.type.equals(Utils.resources().ENTITIES.load("entities:player"))) entity.jump(500);
                }
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                for (Entity entity : world.entities) {
                    if (entity.pathfinder() != null) entity.pathfinder().clear();
                    if (entity.type.equals(Utils.resources().ENTITIES.load("entities:player"))) entity.jump(500);
                }
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.S)) {
                for (Entity entity : world.entities) {
                    entity.setVelocity(-1000f, entity.getVelY());
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
        } else {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                ui().close();
            }

        }
//        if (Gdx.input.isKeyJustPressed(Input.Keys.K)) {
//            ui().add(ui().build(Utils.resources().USER_INTERFACES.load("ui:furnace")));
//        }
//        if (Gdx.input.isKeyJustPressed(Input.Keys.J)) {
//            ui().clear();
//            ui().add(ui().build(Utils.resources().USER_INTERFACES.load("ui:player/inventory")));
////                buildUI(Utils.resources().USER_INTERFACES.load("ui:main_menu"));
//        }
//        if (Gdx.input.isKeyJustPressed(Input.Keys.I)) {
//            ui().remove(ui().size());
//        }
        // Toggle overlay
//        if (Gdx.input.isKeyJustPressed(Input.Keys.I)) {
//            inventory.setVisible(!inventory.isVisible());
//            if (inventory.isVisible()) inventory.toFront();
//        }

        // Gameplay interactions


        camera().update();


        // Render world

    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        super.dispose();
    }


}
