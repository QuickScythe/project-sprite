package com.sprite.tools;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.sprite.data.utils.Utils;
import com.sprite.input.InputSystem;
import com.sprite.render.screen.GameScreen;
import com.sprite.render.ui.UI;

public class UIEditor {

    private static UIEditor.Screen screen = null;

    public static UIEditor.Screen screen() {
        if (screen == null) UIEditor.screen = new UIEditor.Screen();
        return screen;
    }

    public static class Screen extends GameScreen {

        @Override
        public void create() {
            UI<?> ui = ui().build(Utils.resources().USER_INTERFACES.load("ui:main_menu"));
            ui().open(ui);
        }


        @Override
        public void render(float delta) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.K)) {
                ui().open(ui().build(Utils.resources().USER_INTERFACES.load("ui:furnace")));
            }
            InputSystem.i().update(delta);
            ScreenUtils.clear(Color.BLACK);


            sprite().setProjectionMatrix(camera().combined);
            sprite().begin();
            ui().draw();
            screen.sprite().end();
            screen.shape().setProjectionMatrix(screen.camera().combined);
            screen.shape().begin(ShapeRenderer.ShapeType.Line);
            ui().debug();
            screen.shape().end();
//            float x = 0;
//            for(EntityType type : Utils.resources().ENTITIES.all()){
//
//            sprite().draw(type.model.animations[1].frame(type.model), (camera().viewportWidth/2)-(type.width/2), (camera().viewportHeight/2)-(type.width/2), type.width, type.height);


//
//
//            }
//
//            sprite().end();
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


    }
}
