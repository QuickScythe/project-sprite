package com.sprite.tools;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.ScreenUtils;
import com.sprite.data.utils.Utils;
import com.sprite.render.screen.GameScreen;
import com.sprite.resource.Resource;
import com.sprite.resource.entities.EntityType;

public class EntityEditor {

    private static EntityType type = null;
    private static EntityEditor.Screen screen = null;

    public static EntityEditor.Screen screen() {
        if (screen == null) EntityEditor.screen = new EntityEditor.Screen();
        return screen;
    }

    public static EntityType type() {
        return type;
    }

    public static void type(EntityType type) {
        EntityEditor.type = type;
    }

    public static class Screen extends GameScreen {

        private boolean built = false;
        private EntityType type = null;

        @Override
        public void create() {

            for (Resource resource : Utils.resources().list("entities")) {
                Utils.resources().ENTITIES.load("entities:" + resource.location().path());
            }
            buildUI();
        }

        private void type(EntityType type) {
            this.type = type;
        }

        private void buildUI() {
            if (built) return;
//            Skin skin = ui().skin();
//            Table root = new Table(skin);
//            root.setFillParent(true);
//            root.defaults().pad(8f).width(240).height(48);
//
//
//            for (EntityType type : Utils.resources().ENTITIES.all()) {
//
//                 Image + Label
//                TextureRegion region = new TextureRegion(type.model.animations[0].frame(type.model));
//                Image image = new Image(new TextureRegionDrawable(region));
//                Label label = new Label(Texts.get(type.name), skin);
//
//                 Compose them in a Table and make it clickable
//                Table row = new Table(skin);
//                row.setTouchable(Touchable.enabled);
//                 Optional: use a background drawable from your skin (e.g., button background)
//                 Use the drawables defined in uiskin.atlas/uiskin.json: button-normal, button-normal-over, button-normal-pressed
//                if (skin.has("button-normal", Drawable.class)) {
//                    row.setBackground(skin.getDrawable("button-normal"));
//                }
//                row.add(image).size(32).pad(8).left();
//                row.add(label).pad(8).left();
//                row.pack();
//
//                row.addListener(new ClickListener() {
//                    @Override
//                    public void clicked(InputEvent event, float x, float y) {
//                        EntityEditor.Screen.this.type = type;
//                    }
//                });
//
//                root.add(row);
//            }

//            ui().stage().addActor(root);
            built = true;
        }


        @Override
        public void render(float delta) {
            ScreenUtils.clear(Color.BLACK);
            if (type == null) {
                ui().draw();
                return;
            }

            sprite().setProjectionMatrix(camera().combined);
            sprite().begin();
//            float x = 0;
//            for(EntityType type : Utils.resources().ENTITIES.all()){
//
            sprite().draw(type.model.animations[1].frame(type.model), (camera().viewportWidth/2)-(type.width/2), (camera().viewportHeight/2)-(type.width/2), type.width, type.height);
//                x = x + type.width;

            font().setColor(Color.WHITE);

            font().draw(sprite(), "This is a test!zxdgfadsgdfsgdsfgsdfgsdfgf", 50,102);


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
