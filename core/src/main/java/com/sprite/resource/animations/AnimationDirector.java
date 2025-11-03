package com.sprite.resource.animations;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.sprite.data.registries.Registry;
import com.sprite.data.registries.RegistryKey;
import com.sprite.game.world.entities.Entity;

import java.util.Date;

public class AnimationDirector {

    public final Registry<Animation> animations = new Registry<>(RegistryKey.of("animations"));
    String current_animation = "idle";


    public void render(SpriteBatch batch, Entity entity, float x, float y, float width, float height) {
        Animation animation = animations.get(current_animation).orElseThrow(() -> new IllegalArgumentException("Entity \"" + entity.name() + "\" does not have an animation with id " + current_animation));
        batch.draw(animation.frame(entity.model), x, y, width, height);
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

