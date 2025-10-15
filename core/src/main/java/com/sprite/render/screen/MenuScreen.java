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
import com.sprite.data.TranslatableString;
import com.sprite.utils.Texts;

public class MenuScreen extends GameScreen{

    private boolean built;

    @Override
    public void show() {
        super.show();
        System.out.println("Building UI for menu screen");
        buildUI();
    }

    private void buildUI() {
        if (built) return;
        Skin skin = ui().skin();
        Table root = new Table(skin);
        root.setFillParent(true);
        root.defaults().pad(8f).width(240).height(48);

        Label title = new Label(Texts.get(new TranslatableString("game.title")), skin);
        title.setColor(Color.WHITE);
        title.setFontScale(1f);

        TextButton play = new TextButton("Play", skin);
        TextButton options = new TextButton("Options", skin);
        TextButton exit = new TextButton("Exit", skin);

        play.addListener(new ClickListener(){
            @Override public void clicked(InputEvent event, float x, float y) {
                Main main = (Main) Gdx.app.getApplicationListener();
                main.setScreen(new WorldScreen());
            }
        });
        options.addListener(new ClickListener(){
            @Override public void clicked(InputEvent event, float x, float y) {
                // Placeholder: could push an options window here
                Texts.lang(Texts.Lang.EN_US.tag);
                Main main = (Main) Gdx.app.getApplicationListener();
                main.setScreen(new MenuScreen());
            }
        });
        exit.addListener(new ClickListener(){
            @Override public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });

        root.add(title).padBottom(24).width(300).height(60).row();
        root.add(play).row();
        root.add(options).row();
        root.add(exit).row();

        ui().stage().addActor(root);
        built = true;
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.1f, 0.1f, 0.12f, 1f);
        ui().actAndDraw(delta);
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
        super.hide();
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
