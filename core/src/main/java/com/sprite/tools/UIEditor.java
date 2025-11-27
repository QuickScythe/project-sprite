package com.sprite.tools;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.ScreenUtils;
import com.sprite.data.utils.Utils;
import com.sprite.game.ItemStack;
import com.sprite.input.InputAction;
import com.sprite.input.InputSystem;
import com.sprite.render.screen.GameScreen;
import com.sprite.render.ui.UI;
import com.sprite.render.ui.inventory.Dialog;
import com.sprite.render.ui.inventory.Inventory;
import com.sprite.resource.ui.DialogUI;
import com.sprite.resource.ui.InventoryUI;
import com.sprite.resource.ui.UIDefinition;
import com.sprite.resource.ui.UIType;

public class UIEditor {

    private static UIEditor.Screen screen = null;

    public static UIEditor.Screen screen() {
        if (screen == null) UIEditor.screen = new UIEditor.Screen();
        return screen;
    }

    public static class Screen extends GameScreen {

        @Override
        public void create() {
            ui().addFirst(ui().build(Utils.resources().USER_INTERFACES.load("ui:main_menu")));
        }




        @Override
        public void render(float delta) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.L)) {
                ui().addFirst(ui().build(Utils.resources().USER_INTERFACES.load("ui:furnace")));
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.K)) {
                ui().add(ui().build(Utils.resources().USER_INTERFACES.load("ui:furnace")));
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.J)) {
                ui().clear();
                ui().add(ui().build(Utils.resources().USER_INTERFACES.load("ui:player/inventory")));
//                buildUI(Utils.resources().USER_INTERFACES.load("ui:main_menu"));
            }
            if(Gdx.input.isKeyJustPressed(Input.Keys.I)){
                ui().remove(ui().size());
            }
            // Ensure input system is updated when using this editor screen
            InputSystem.i().update(delta);
            ScreenUtils.clear(Color.BLACK);


            sprite().setProjectionMatrix(camera().combined);
            sprite().begin();
            ui().draw();
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
