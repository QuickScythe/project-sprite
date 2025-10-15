package com.sprite.world;

import com.sprite.resource.entities.EntityType;
import com.sprite.render.screen.GameScreen;
import com.sprite.world.entities.Entity;

import java.util.ArrayList;
import java.util.List;

public class World {

    private static class Body {
        float x, y;
        float vx, vy;
        float ax, ay;
        boolean onGround;
        float width, height;
        Body(float x, float y, float width, float height){
            this.x = x; this.y = y;
            this.width = width; this.height = height;
        }
    }

    public final List<Entity> entities = new ArrayList<>();
    private final java.util.Map<Entity, Body> bodies = new java.util.HashMap<>();

    // Physics parameters
    private float gravity = -400f; // units per sec^2 (tweak for feel)
    private float linearDamping = 0.98f; // simple air drag each step
    private float groundFriction = 0.85f; // applied to vx when on ground
    private float restitution = 0.0f; // bounce factor on ground
    private float worldFloorY = 0f; // simple ground plane at y=0

    public Entity spawn(EntityType type, int x, int y) {
        Entity entity = new Entity(type, x, y);
        entities.add(entity);
        Body body = new Body(entity.position().x, entity.position().y, entity.width(), entity.height());
        bodies.put(entity, body);
        return entity;
    }

    public void setGravity(float g){ this.gravity = g; }
    public float gravity(){ return gravity; }
    public void setFloorY(float y){ this.worldFloorY = y; }

    public void applyForce(Entity e, float fx, float fy){
        Body b = bodies.get(e);
        if (b == null) return;
        b.ax += fx; b.ay += fy;
    }

    public void setVelocity(Entity e, float vx, float vy){
        Body b = bodies.get(e);
        if (b == null) return;
        b.vx = vx; b.vy = vy;
    }

    public float getVelX(Entity e){ Body b = bodies.get(e); return b==null?0:b.vx; }
    public float getVelY(Entity e){ Body b = bodies.get(e); return b==null?0:b.vy; }
    public boolean onGround(Entity e){ Body b = bodies.get(e); return b!=null && b.onGround; }

    private void step(float dt){
        if (dt <= 0) return;
        for (Entity e : entities){
            Body b = bodies.get(e);
            if (b == null) continue;

            // Apply gravity
            b.ay += gravity;

            // Integrate velocity
            b.vx += b.ax * dt;
            b.vy += b.ay * dt;

            // Damping (air drag)
            b.vx *= linearDamping;
            b.vy *= linearDamping;

            // Integrate position
            b.x += b.vx * dt;
            b.y += b.vy * dt;

            // Ground collision with simple plane at y = worldFloorY
            b.onGround = false;
            if (b.y < worldFloorY){
                b.y = worldFloorY;
                if (b.vy < 0){
                    b.vy = -b.vy * restitution;
                }
                // When on ground, apply additional friction and zero tiny vertical velocity
                if (Math.abs(b.vy) < 0.01f) b.vy = 0f;
                b.onGround = true;
                b.vx *= groundFriction;
            }

            // Reset per-step acceleration
            b.ax = 0; b.ay = 0;

            // Sync entity position
            e.position().set(b.x, b.y, 0);
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
