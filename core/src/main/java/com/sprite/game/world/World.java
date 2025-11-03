package com.sprite.game.world;

import com.sprite.resource.entities.EntityType;
import com.sprite.render.screen.GameScreen;
import com.sprite.game.world.entities.Entity;

import java.util.ArrayList;
import java.util.List;

public class World {



    public final List<Entity> entities = new ArrayList<>();

    // Physics parameters
    private float gravity = -600f; // units per sec^2 (stronger gravity for snappier feel)
    private float linearDamping = 1; // air drag each step (per-frame factor)
    private float groundFriction = 0.85f; // applied to vx when on ground
    private float restitution = 0.0f; // bounce factor on ground
    private float worldFloorY = 0f; // simple ground plane at y=0

    public Entity spawn(EntityType type, int x, int y) {
        Entity entity = new Entity(type, x, y);
        entities.add(entity);
        return entity;
    }

    public void gravity(float g){ this.gravity = g; }
    public float gravity(){ return gravity; }
    public void floor(float y){ this.worldFloorY = y; }
    public float floor(){ return worldFloorY; }
    public void linearDamping(float d){ this.linearDamping = d; }
    public float linearDamping(){ return linearDamping; }
    public void groundFriction(float f){ this.groundFriction = f; }
    public float groundFriction(){ return groundFriction; }
    public void restitution(float r){ this.restitution = r; }
    public float restitution(){ return restitution; }



    private void step(float dt){
        if (dt <= 0) return;
        for (Entity e : entities){
            e.update(this, dt);

        }
    }

    public void render(GameScreen screen){
        // Step physics before rendering this frame
        float dt = com.badlogic.gdx.Gdx.graphics.getDeltaTime();
        step(dt);

        for (Entity entity : entities){
            entity.render(screen);
        }
    }
}
