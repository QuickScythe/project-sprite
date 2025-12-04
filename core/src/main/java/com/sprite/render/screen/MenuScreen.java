package com.sprite.render.screen;

import com.sprite.data.utils.Utils;
import com.sprite.render.ui.UI;
import com.sprite.resource.texture.GameSprite;

public class MenuScreen extends GameScreen{


    GameSprite background;

    @Override
    public void create() {
        UI<?> menu = ui().build(Utils.resources().USER_INTERFACES.load("ui:main_menu"));
        background = Utils.resources().TEXTURES.load("textures:background");
        ui().open(menu);
        pipeline().background((screen, delta) -> {
            screen.sprite().draw(background.sprite(), 0, 0, camera().viewportWidth, camera().viewportHeight);
        });
        requestMusic();

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
