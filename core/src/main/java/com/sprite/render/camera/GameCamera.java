package com.sprite.render.camera;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.sprite.data.annotations.Nullable;
import com.sprite.data.utils.Utils;
import com.sprite.game.world.entities.Entity;
import com.sprite.render.screen.GameScreen;

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

    public void focus(GameScreen screen, @Nullable Entity focus) {
        if(focus != null){
            screen.ui().remove(focus.inventory());
        }
        this.focus = focus;
        if(focus == null) return;
        screen.ui().add(focus.inventory());

    }

    public Entity focus() {
        return focus;
    }

    @Override
    public void update() {
        super.update();
        if(focus != null) {
            if (Utils.distance(this.position, focus.position()) > 5) {
                velocity.lerp(new Vector2(focus.position().x - this.position.x, focus.position().y - this.position.y).scl(0.1f), 0.1f);
            } else {
                velocity.scl(0.8f);
            }
//            System.out.println("Focus\t\tX: " + focus.position().x() + "\tFocus\t\tY: " + focus.position().y());
//            System.out.println("Camera\t\tX: " + this.position.x + "\tCamera\t\tY: " + this.position.y);
//            this.position.set(focus.position().x() + focus.width() / 2f, focus.position().y() + focus.height() / 2f, 0);
            this.translate(velocity);
//            this.update();
        }
    }
}
