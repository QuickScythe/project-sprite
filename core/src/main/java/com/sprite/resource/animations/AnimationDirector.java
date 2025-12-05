package com.sprite.resource.animations;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.sprite.data.registries.Registry;
import com.sprite.data.registries.RegistryKey;
import com.sprite.game.world.entities.Entity;

import java.util.Date;

public class AnimationDirector {

    public final Registry<Animation> animations = new Registry<>(RegistryKey.of("animations"));
    String current_animation = "idle";
    // Remember the last horizontal facing direction so idle doesn't snap to right
    private boolean lastFacingRight = true;


    public void render(SpriteBatch batch, Entity entity, float x, float y, float width, float height) {
        // Choose animation based on movement state
        float vx = entity.getVelX();
        float vy = entity.getVelY();
        final float EPS = 0.01f;
        boolean movingHoriz = Math.abs(vx) > EPS;
        boolean moving = movingHoriz || Math.abs(vy) > EPS;
        String desired = moving ? "step" : "idle";
        if (!desired.equals(current_animation)) {
            animation(desired);
        }

        Animation animation = animations.get(current_animation)
                .orElseThrow(() -> new IllegalArgumentException("Entity \"" + entity.name() + "\" does not have an animation with id " + current_animation));

        // Get current frame for this entity
        var region = animation.frame(entity);

        // Update and use facing: if moving horizontally, update last facing.
        if (movingHoriz) {
            lastFacingRight = vx > 0f;
        }

        // Mirror sprite when facing left (persist across idle)
        boolean faceLeft = !lastFacingRight;
        if (faceLeft) {
            // Create a flipped copy to avoid mutating shared textures
            var flipped = new com.badlogic.gdx.graphics.g2d.TextureRegion(region);
            flipped.flip(true, false);
            batch.draw(flipped, x, y, width, height);
        } else {
            batch.draw(region, x, y, width, height);
        }
    }

    public void animation(String animationName) {
        if (animations.get(animationName).isPresent()) {
            Animation animation = animations.get(animationName).get();
            if (!current_animation.equals(animationName)) {
                animation.frame = 0;
                animation.update = new Date().getTime();
                this.current_animation = animationName;

            }

        }
    }
}

