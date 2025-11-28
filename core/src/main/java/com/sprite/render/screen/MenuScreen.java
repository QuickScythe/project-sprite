package com.sprite.render.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.sprite.Main;
import com.sprite.Sounds;
import com.sprite.data.TranslatableString;
import com.sprite.data.utils.Texts;
import com.sprite.data.utils.Utils;
import com.sprite.render.ui.UI;
import com.sprite.resource.Resource;
import com.sprite.resource.texture.GameSprite;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MenuScreen extends GameScreen{


    GameSprite background;

    @Override
    public void create() {
        UI<?> menu = ui().build(Utils.resources().USER_INTERFACES.load("ui:main_menu"));
        background = Utils.resources().TEXTURES.load("textures:background");
        menu.open(true);
        ui().addFirst(menu);
        pipeline().background((screen, delta) -> {
            screen.sprite().draw(background.sprite(), 0, 0, camera().viewportWidth, camera().viewportHeight);
        });
        List<Resource> possible = new ArrayList<>();
        for(Resource resource : Utils.resources().list("music")){
            if(resource.location().path().startsWith("background")){
                possible.add(resource);
            }
        }
        Sounds.music(possible.get(new Random().nextInt(possible.size())).location().toString());

    }



    @Override
    public void resize(int width, int height) {
//        super.resize(width, height);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
//        super.hide();
    }

    @Override
    public void dispose() {
        super.dispose();
    }


}
