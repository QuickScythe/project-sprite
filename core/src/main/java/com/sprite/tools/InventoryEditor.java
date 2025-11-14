package com.sprite.tools;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.ScreenUtils;
import com.sprite.data.utils.Utils;
import com.sprite.render.screen.GameScreen;
import com.sprite.render.ui.UIDefinition;

public class InventoryEditor {

    private static InventoryEditor.Screen screen = null;

    public static InventoryEditor.Screen screen() {
        if (screen == null) InventoryEditor.screen = new InventoryEditor.Screen();
        return screen;
    }

    public static class Screen extends GameScreen {

        UIDefinition ui = null;

        @Override
        public void create() {
            buildUI();
        }

        private void buildUI() {
            ui = Utils.resources().USER_INTERFACES.load("ui:furnace");
        }


        @Override
        public void render(float delta) {
            ScreenUtils.clear(Color.BLACK);


            sprite().setProjectionMatrix(camera().combined);
            sprite().begin();
            ui.draw(this);
//            float x = 0;
//            for(EntityType type : Utils.resources().ENTITIES.all()){
//
//            sprite().draw(type.model.animations[1].frame(type.model), (camera().viewportWidth/2)-(type.width/2), (camera().viewportHeight/2)-(type.width/2), type.width, type.height);



            sprite().end();

//
//
//            }
//
//            sprite().end();
        }

        @Override
        public void pause() {

        }

        @Override
        public void resume() {

        }


    }
}
