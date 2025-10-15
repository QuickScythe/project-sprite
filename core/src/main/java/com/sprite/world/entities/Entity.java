package com.sprite.world.entities;

import com.badlogic.gdx.math.Vector3;
import com.sprite.data.TranslatableString;
import com.sprite.render.screen.GameScreen;
import com.sprite.resource.animations.Animation;
import com.sprite.resource.animations.AnimationDirector;
import com.sprite.resource.controllers.Controller;
import com.sprite.resource.entities.EntityType;
import com.sprite.resource.models.Model;
import org.json.JSONObject;

public class Entity {

    public final Model model;
    public final int maxHealth;
    public final AnimationDirector director = new AnimationDirector();
    private final float width, height;
    private int health;
    private float speed;
    private TranslatableString name;
    private Vector3 position;
    public final Controller controller;

    public Entity(EntityType type, float x, float y) {
        this.model = type.model.clone();
        this.health = type.health;
        this.maxHealth = type.health;
        this.speed = type.speed;
        this.position = new Vector3(x, y, 0);
        this.width = type.width;
        this.height = type.height;
        this.name = type.name;
        this.controller = type.controller.clone();
        for (Animation animation : model.animations) {
            this.director.animations.register(animation.name, animation);
        }
    }

    public void render(GameScreen screen) {
        screen.sprite().setProjectionMatrix(screen.camera().combined);
        screen.sprite().begin();
        director.render(screen.sprite(), this, position.x, position.y, width, height);
        screen.sprite().end();
    }

    public void update(float delta) {

        if(controller.input().id().equalsIgnoreCase("input:pathfinder")){
            JSONObject pathfinderData = controller.extra().getJSONObject("pathfinder");
            return;
        }
    }

    public int health() {
        return health;
    }

    public void health(int health) {
        this.health = Math.min(health, maxHealth);
    }

    public TranslatableString name() {
        return name;
    }

    public float width() {
        return width;
    }

    public float height() {
        return height;
    }

    public Vector3 position() {
        return position;
    }

    public float speed() {
        return speed;
    }

    public static class Position {
        private float x, y;

        public Position(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public float x() {
            return x;
        }

        public float y() {
            return y;
        }

        public void set(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public void x(float x) {
            this.x = x;
        }

        public void y(float y) {
            this.y = y;
        }
    }
}
