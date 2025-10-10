package com.sprite.screen;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public abstract class GameScreen implements Screen {

    boolean debug = true;
    float scale = 1 / 16f;
    float cameraScale = 0.5f;
    //    List<Entity> entities = new ArrayList<>();
    OrthographicCamera camera;
    ShapeRenderer debugRenderer;
    SpriteBatch sprite;

    public ShapeRenderer shape() {
        return debugRenderer;
    }

    public SpriteBatch sprite() {
        return sprite;
    }

    public OrthographicCamera camera() {
        return camera;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        sprite = new SpriteBatch();

        camera.setToOrtho(false, 300, 200);
        camera.update();

        debugRenderer = new ShapeRenderer();
    }

}
