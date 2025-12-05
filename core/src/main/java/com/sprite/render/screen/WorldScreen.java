package com.sprite.render.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.sprite.data.registries.Registries;
import com.sprite.data.utils.Utils;
import com.sprite.game.ItemStack;
import com.sprite.game.world.World;
import com.sprite.game.world.entities.Entity;
import com.sprite.input.InputAction;
import com.sprite.input.InputSystem;
import com.sprite.render.ui.UI;
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

    World world;
    GameSprite background;

    @Override
    public void create() {
        if(Gdx.files.internal("saves/test").exists()){
            FileHandle handle = Gdx.files.internal("saves/test/options.json");
            JSONObject data = new JSONObject(handle.readString());
            System.out.println(data.toString(2));
            world = new World(data);
        }
        else{
            world = new World(new World.Builder("test"));
        }
        background = Utils.resources().TEXTURES.load("textures:background");
        pipeline().background((screen, delta) -> {
            screen.sprite().draw(background.sprite(), 0, 0, camera().viewportWidth, camera().viewportHeight);
        });
        pipeline().foreground((screen, delta) -> {
            // Handle simple movement for the currently focused entity using the InputSystem
            // Default keyboard mapping uses WASD/Arrow keys via MouseKeyboardInputProvider


            // Step world and render
            world.render(screen);
        });
        pipeline().ui((screen, delta) -> {
            ui().draw();
        });

        EntityType playerType = Utils.resources().ENTITIES.load("entities:player");
        Entity player = world.spawn(playerType, new Random().nextInt((int) camera().viewportWidth), 200);
        player.health(10);
        player.director.animation("step");
        System.out.println(player.controller.extra().toString(2));

        player.inventory().add(new ItemStack("items:test", 1));
//                player.inventory().put(1, new ItemStack(Utils.resources().ITEMS.load("items:test"), 1));

        camera().focus(player);


        EntityType testType = Utils.resources().ENTITIES.load("entities:test");
        Entity test = world.spawn(testType, new Random().nextInt((int) camera().viewportWidth), 32);
        test.health(10);
        test.director.animation("step");

        requestMusic();
    }

    public World world(){
        return world;
    }

    @Override
    public void render(float delta) {
//        delta = 0;
        super.render(delta);
        InputSystem i = InputSystem.i();
        if(camera().focus() != null && camera().focus().inventory().open()){
            handleFocusInput(i);
            return;
        }
        if(ui().isOpen()){
            handleUIInput(i);
            return;
        }
        handleWorldInput(i);
        camera().update();
    }

    private void handleFocusInput(InputSystem i) {
        if(i.isActionJustPressed(InputAction.CANCEL)){
            camera().focus().inventory().open(false);
        }
    }

    private void handleWorldInput(InputSystem i) {
        if (i.isActionJustPressed(InputAction.INVENTORY)) {
            if (camera().focus() != null)
                camera().focus().inventory().open(true);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            UI<?> ui = ui().build(Utils.resources().USER_INTERFACES.load("ui:main_menu"));
            ui().open(ui);

        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            camera().zoom = camera().zoom + 0.1f;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            camera().zoom = camera().zoom - 0.1f;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.I)) {
            EntityType playerType = Utils.resources().ENTITIES.load("entities:player");
            Entity player = world.spawn(playerType, new Random().nextInt((int) camera().viewportWidth), 200);
            player.health(10);
            player.director.animation("step");
            System.out.println(player.controller.extra().toString(2));

            player.inventory().add(new ItemStack("items:test", 1));
//                player.inventory().put(1, new ItemStack(Utils.resources().ITEMS.load("items:test"), 1));

            camera().focus(player);


            EntityType testType = Utils.resources().ENTITIES.load("entities:test");
            Entity test = world.spawn(testType, new Random().nextInt((int) camera().viewportWidth), 32);
            test.health(10);
            test.director.animation("step");
            System.out.println(test.controller.extra().toString(2));

//                test.inventory().add(new ItemStack("items:test", 1));
//                test.inventory().put(1, new ItemStack(Utils.resources().ITEMS.load("item:test"), 1));

            JSONObject data = Registries.dump();
            System.out.println("Registries dump:");
            System.out.println(data.toString(2));


        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.H)) {
            EntityType playerType = Utils.resources().ENTITIES.load("entities:test");
            Entity test = world.spawn(playerType, new Random().nextInt((int) camera().viewportWidth), 200);
            test.health(10);
            test.director.animation("step");
            System.out.println(test.controller.extra().toString(2));


            camera().focus(test);


            EntityType testType = Utils.resources().ENTITIES.load("entities:test");
            Entity test1 = world.spawn(testType, new Random().nextInt((int) camera().viewportWidth), 32);
            test1.health(10);
            test1.director.animation("step");
            System.out.println(test1.controller.extra().toString(2));


        }
//        if (Gdx.input.isKeyJustPressed(Input.Keys.D)) {
//            for (Entity entity : world.entities) {
//                if (entity.type.equals(Utils.resources().ENTITIES.load("entities:player"))) entity.jump(500);
//            }
//        }
//        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
//            for (Entity entity : world.entities) {
//                if (entity.pathfinder() != null) entity.pathfinder().clear();
//                if (entity.type.equals(Utils.resources().ENTITIES.load("entities:player"))) entity.jump(500);
//            }
//        }
//        if (Gdx.input.isKeyJustPressed(Input.Keys.S)) {
//            for (Entity entity : world.entities) {
//                entity.setVelocity(-1000f, entity.getVelY());
//            }
//        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.A)) {
            int size = new Random().nextInt(6);
            Element[] elements = new Element[size];
            for (int j = 0; j < size; j++) {
                elements[j] = (Element) Elements.values().toArray()[new Random().nextInt(Elements.values().size())];
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
    }

    private void handleUIInput(InputSystem i) {
        if (i.isActionJustPressed(InputAction.CANCEL)) {
            ui().close();
        }

        if(Gdx.input.isKeyJustPressed(Input.Keys.J)){
            UI<?> ui = ui().build(Utils.resources().USER_INTERFACES.load("ui:furnace"));
            ui().open(ui);

        }

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
