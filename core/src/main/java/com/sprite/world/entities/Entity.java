package com.sprite.world.entities;

import com.sprite.data.TranslatableString;
import com.sprite.resource.animations.Animation;
import com.sprite.resource.animations.AnimationDirector;
import com.sprite.resource.entities.EntityType;
import com.sprite.resource.models.Model;
import com.sprite.screen.GameScreen;

public class Entity {

    public Model model;
    public int health, maxHealth;
    float speed;
    float x, y, width, height;
    TranslatableString name;

    public final AnimationDirector director = new AnimationDirector();

    public Entity(EntityType type, int x, int y) {
        this.model = type.model.clone();
        this.health = type.health;
        this.maxHealth = type.health;
        this.speed = type.speed;
        this.x = x;
        this.y = y;
        this.width = type.model.width;
        this.height = type.model.height;
        this.name = type.name;
        for(Animation animation : model.animations) {
            this.director.animations.register(animation.name, animation);
        }


    }

    public void render(GameScreen screen) {

        screen.sprite().setProjectionMatrix(screen.camera().combined);
        screen.sprite().begin();
        director.render(screen.sprite(), this, x, y, width, height);
        screen.sprite().end();




    }

    public TranslatableString name() {
        return name;
    }

    // Position and size accessors for physics/world
    public float x() { return x; }
    public float y() { return y; }
    public float width() { return width; }
    public float height() { return height; }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float speed() { return speed; }
}
