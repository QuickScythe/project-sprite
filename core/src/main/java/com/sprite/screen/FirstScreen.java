package com.sprite.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.sprite.data.registries.Registries;
import com.sprite.magic.elements.Element;
import com.sprite.magic.elements.Elements;
import com.sprite.magic.spells.Spell;
import com.sprite.magic.spells.Spells;
import com.sprite.resource.entities.EntityType;
import com.sprite.utils.Utils;
import com.sprite.world.World;
import com.sprite.world.entities.Entity;
import org.json.JSONObject;

import java.util.Objects;
import java.util.Random;

/**
 * First screen of the application. Displayed after the application is created.
 */
public class FirstScreen extends GameScreen {

    World world = new World();


    @Override
    public void show() {
        super.show();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);
        // Draw your screen here. "delta" is the time since last render in seconds.
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {

            EntityType playerType = Utils.resources().ENTITIES.load("entities:player");
            Entity player = world.spawn(playerType, new Random().nextInt((int) camera.viewportWidth), 200);

            player.health = 10;
            player.director.animation("step");


            JSONObject data = Registries.dump();
            System.out.println("Registries dump:");
            System.out.println(data.toString(2));
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            for(Entity entity : world.entities) {
                world.applyForce(entity, 50, -50);
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

        world.render(this);

    }

    @Override
    public void resize(int width, int height) {
        // If the window is minimized on a desktop (LWJGL3) platform, width and height are 0, which causes problems.
        // In that case, we don't resize anything, and wait for the window to be a normal size before updating.
        if (width <= 0 || height <= 0) {
        }

        // Resize your screen here. The parameters represent the new window size.
    }

    @Override
    public void pause() {
        // Invoked when your application is paused.
    }

    @Override
    public void resume() {
        // Invoked when your application is resumed after pause.
    }

    @Override
    public void hide() {
        // This method is called when another screen replaces this one.
    }

    @Override
    public void dispose() {
        // Destroy screen's assets here.
    }
}
