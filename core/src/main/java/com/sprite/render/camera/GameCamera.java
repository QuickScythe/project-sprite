package com.sprite.render.camera;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.sprite.data.annotations.Nullable;
import com.sprite.data.utils.Utils;
import com.sprite.game.world.entities.Entity;

public class GameCamera extends OrthographicCamera {

    @Nullable
    Entity focus;

    Vector2 velocity = new Vector2(0,0);

    public GameCamera(){
        super();
        this.focus = null;
    }

    public GameCamera(@Nullable Entity focus) {
        super();
        this.focus = focus;
    }

    public void focus(@Nullable Entity focus) {
        this.focus = focus;
    }

    public Entity focus() {
        return focus;
    }

    @Override
    public void update() {
        super.update();
        if(focus != null) {
            Vector3 target = new Vector3(focus.position().x + (focus.width()/2), focus.position().y + (focus.height()/2), 0);
            if (Utils.distance(this.position, target) > 5) {
                velocity.lerp(new Vector2((target.x) - this.position.x, target.y - this.position.y).scl(0.1f), 0.1f);
            } else {
                velocity.scl(0.8f);
            }
            this.translate(velocity);
        }
    }
}
