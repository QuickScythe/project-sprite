package com.sprite.render.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.sprite.data.TranslatableString;
import com.sprite.data.registries.Registries;
import com.sprite.data.registries.Registry;
import com.sprite.data.utils.Utils;
import com.sprite.data.utils.resources.Texts;
import com.sprite.game.Game;
import com.sprite.game.items.ItemStack;
import com.sprite.game.world.World;
import com.sprite.game.world.entities.Entity;
import com.sprite.input.InputAction;
import com.sprite.input.InputSystem;
import com.sprite.render.ui.UI;
import com.sprite.resource.animations.Animation;
import com.sprite.resource.entities.CollisionData;
import com.sprite.resource.entities.EntityType;
import com.sprite.resource.magic.elements.Element;
import com.sprite.resource.magic.elements.Elements;
import com.sprite.resource.magic.spells.Spell;
import com.sprite.resource.magic.spells.Spells;
import com.sprite.resource.texture.GameSprite;
import org.json.JSONObject;

import java.util.Objects;
import java.util.Random;

/**
 * First screen of the application. Displayed after the application is created.
 */
public class WorldScreen extends GameScreen {

    World world;
    GameSprite background;
    Game game;

    @Override
    public void create() {
        if(Gdx.files.internal("saves/test").exists()){
            FileHandle handle = Gdx.files.internal("saves/test/options.json");
            JSONObject data = new JSONObject(handle.readString());
            System.out.println(data.toString(2));
            world = new World(data);
        }
        else{
            world = new World(new World.Builder("test"));
        }
        game = new Game(world);
        game.setParticleSystem(pipeline().particleSystem());
        game.setLightingSystem(pipeline().lightingSystem());

        // Configure atmospheric lighting and bloom
        pipeline().lightingSystem().setTileSize(world.tileSize());
        pipeline().lightingSystem().setAmbientColor(0.04f, 0.04f, 0.08f, 0.45f);
        pipeline().shaderLayer().setBloomEnabled(true);
        pipeline().shaderLayer().setBloomIntensity(1.3f);
        pipeline().shaderLayer().setBloomThreshold(0.55f);
        pipeline().shaderLayer().setBloomColor(new com.badlogic.gdx.graphics.Color(0.25f, 0.8f, 1.0f, 1.0f));

        // Ambient light from above casting real dynamic shadows
        com.sprite.render.lighting.DirectionalLight overheadAmbientLight = new com.sprite.render.lighting.DirectionalLight(
            0f, -1f, new com.badlogic.gdx.graphics.Color(1.0f, 0.98f, 0.92f, 0.2f), 0.85f, true
        );
        overheadAmbientLight.setShadowLength(world.tileSize() * 8f);
        pipeline().lightingSystem().addLight(overheadAmbientLight);

        background = Utils.resources().TEXTURES.load("textures:background");
        pipeline().background((screen, delta) -> {
            screen.sprite().draw(background.sprite(), 0, 0, camera().viewportWidth, camera().viewportHeight);
        });
        pipeline().foreground((screen, delta) -> {
            // Step world and render
            world.render(screen);
        });
        pipeline().ui((screen, delta) -> {
            ui().draw();
            game.render(screen);
        });

        EntityType playerType = Utils.resources().ENTITIES.load("entities:player");
        Entity player = world.spawn(playerType, 120, 200);
        player.director.animation("step");
        game.initPlayer(player);

        camera().focus(player);

        requestMusic();
    }

    public World world(){
        return world;
    }

    public Game game() {
        return game;
    }

    public Game rpg() {
        return game;
    }

    @Override
    public void draw(float delta) {
        InputSystem i = InputSystem.i();
        game.update(delta);
        updateShadowCasters();
        if(camera().focus() != null && camera().focus().inventory().open()){
            handleFocusInput(i);
            return;
        }
        if(ui().isOpen()){
            handleUIInput(i);
            return;
        }
        game.handleInput(this);
        handleWorldInput(i);
        camera().update();
    }

    private void updateShadowCasters() {
        if (world == null || pipeline() == null || pipeline().lightingSystem() == null) return;
        com.sprite.render.lighting.LightingSystem lighting = pipeline().lightingSystem();
        if (!lighting.isEnabled() || !lighting.isShadowsEnabled()) return;

        lighting.clearShadowCasters();
        if (lighting.getLights().isEmpty()) return;

        int ts = world.tileSize();
        boolean hasDirectionalShadowLight = false;

        for (com.sprite.render.lighting.Light light : lighting.getLights()) {
            if (!light.isActive() || !light.isCastsShadows()) continue;

            if (light instanceof com.sprite.render.lighting.DirectionalLight) {
                hasDirectionalShadowLight = true;
                continue;
            }

            float lx = light.getX();
            float ly = light.getY();
            float radius = light instanceof com.sprite.render.lighting.PointLight ? ((com.sprite.render.lighting.PointLight) light).getRadius() : 250f;

            int minTx = (int) Math.floor((lx - radius) / ts);
            int maxTx = (int) Math.ceil((lx + radius) / ts);
            int minTy = (int) Math.floor((ly - radius) / ts);
            int maxTy = (int) Math.ceil((ly + radius) / ts);

            for (int tx = minTx; tx <= maxTx; tx++) {
                for (int ty = minTy; ty <= maxTy; ty++) {
                    if (world.isSolid(world.getTileId(tx, ty))) {
                        lighting.addShadowCaster(new com.sprite.render.lighting.ShadowCaster(tx * ts, ty * ts, ts, ts));
                    }
                }
            }
        }

        if (hasDirectionalShadowLight && camera() != null) {
            float camLeft = camera().position.x - camera().viewportWidth / 2f;
            float camBottom = camera().position.y - camera().viewportHeight / 2f;
            int minTx = (int) Math.floor((camLeft - ts) / ts);
            int maxTx = (int) Math.ceil((camLeft + camera().viewportWidth + ts) / ts);
            int minTy = (int) Math.floor((camBottom - ts) / ts);
            int maxTy = (int) Math.ceil((camBottom + camera().viewportHeight + ts) / ts);

            for (int tx = minTx; tx <= maxTx; tx++) {
                for (int ty = minTy; ty <= maxTy; ty++) {
                    if (world.isSolid(world.getTileId(tx, ty))) {
                        float cx = tx * ts;
                        float cy = ty * ts;
                        boolean alreadyAdded = false;
                        for (com.sprite.render.lighting.ShadowCaster sc : lighting.getShadowCasters()) {
                            if (sc.getBounds().x == cx && sc.getBounds().y == cy) {
                                alreadyAdded = true;
                                break;
                            }
                        }
                        if (!alreadyAdded) {
                            lighting.addShadowCaster(new com.sprite.render.lighting.ShadowCaster(cx, cy, ts, ts));
                        }
                    }
                }
            }
        }
    }

    @Override
    public void debug(float delta) {
        sprite().end();

        for(Entity entity : world.entities){
            GlyphLayout layout = new GlyphLayout(font(), Texts.get(entity.name()));
            // draw so that the text is centered on (centerX, centerY)
            float layoutX = (entity.position().x + entity.width()/2) - (layout.width / 2f);
            float layoutY = (entity.position().y + entity.height()) + (layout.height / 2f);
            shape().end();
            shape().begin(ShapeRenderer.ShapeType.Filled);
            shape().setProjectionMatrix(camera().combined);
            shape().setColor(0,0,0,0.75f);

            shape().rect(layoutX-2.5f, layoutY-font().getLineHeight()+2, layout.width+5, font().getLineHeight());
            shape().end();
            sprite().begin();
            sprite().setProjectionMatrix(camera().combined);
            font().draw(sprite(), layout, layoutX, layoutY);
            sprite().end();
            shape().begin(ShapeRenderer.ShapeType.Line);
            shape().setProjectionMatrix(camera().combined);

            //Focused entity is red
            if(camera().focus() != null && camera().focus().uuid().equals(entity.uuid()))
                shape().setColor(1, 0, 0, 0.5f);
            else shape().setColor(1, 1, 1, 0.5f);
            float ex = entity.position().x;
            float ey = entity.position().y;
            Registry<Animation> animations = entity.director.animations;
            shape().rect(ex, ey , entity.width(), entity.height());
            for(Animation animation : animations.values()){
                Animation.FrameData frame = animation.frameArray[animation.frame()];
                for(String type : frame.collisions()){
                    CollisionData data = frame.collisions(type);
                    for(CollisionData.Part part : data.parts){
                        float scale = entity.width()/entity.model.width;
                        shape().setColor(0,1,0, 1);
                        float x = ex + part.from().x*scale;
                        float y = ey + part.from().y*scale;
                        float width = part.to().x*scale - part.from().x*scale;
                        float height = part.to().y*scale - part.from().y*scale;
                        shape().rect(x, y, width, height);
                    }

                }

//                animation.frameArray[animation.index].collisions
            }


        }
        sprite().begin();
    }

    private void handleFocusInput(InputSystem i) {
        if(i.isActionJustPressed(InputAction.CANCEL)){
            camera().focus().inventory().open(false);
        }
    }

    private void handleWorldInput(InputSystem i) {
        if (i.isActionJustPressed(InputAction.INVENTORY)) {
            if (camera().focus() != null)
                camera().focus().inventory().open(true);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            UI<?> ui = ui().build(Utils.resources().USER_INTERFACES.load("ui:main_menu"));
            ui().open(ui);

        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            camera().zoom = camera().zoom + 0.1f;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            camera().zoom = camera().zoom - 0.1f;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.I)) {
            EntityType playerType = Utils.resources().ENTITIES.load("entities:player");
            Entity player = world.spawn(playerType, new Random().nextInt((int) camera().viewportWidth), 200);
            player.health(10);
            player.director.animation("step");
            player.name(new TranslatableString("Player" + new Random().nextInt(999)));
            System.out.println(player.controller.extra().toString(2));

            player.inventory().add(new ItemStack("items:test", 1));
//                player.inventory().put(1, new ItemStack(Utils.resources().ITEMS.load("items:test"), 1));

            camera().focus(player);


            EntityType testType = Utils.resources().ENTITIES.load("entities:test");
            Entity test = world.spawn(testType, new Random().nextInt((int) camera().viewportWidth), 32);
            test.health(10);
            test.director.animation("step");
            System.out.println(test.controller.extra().toString(2));

//                test.inventory().add(new ItemStack("items:test", 1));
//                test.inventory().put(1, new ItemStack(Utils.resources().ITEMS.load("item:test"), 1));

            JSONObject data = Registries.dump();
            System.out.println("Registries dump:");
            System.out.println(data.toString(2));


        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.H)) {
            EntityType playerType = Utils.resources().ENTITIES.load("entities:test");
            Entity test = world.spawn(playerType, new Random().nextInt((int) camera().viewportWidth), 200);
            test.health(10);
            test.director.animation("step");
            System.out.println(test.controller.extra().toString(2));


            camera().focus(test);


            EntityType testType = Utils.resources().ENTITIES.load("entities:test");
            Entity test1 = world.spawn(testType, new Random().nextInt((int) camera().viewportWidth), 32);
            test1.health(10);
            test1.director.animation("step");
            System.out.println(test1.controller.extra().toString(2));


        }
//        if (Gdx.input.isKeyJustPressed(Input.Keys.D)) {
//            for (Entity entity : world.entities) {
//                if (entity.type.equals(Utils.resources().ENTITIES.load("entities:player"))) entity.jump(500);
//            }
//        }
//        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
//            for (Entity entity : world.entities) {
//                if (entity.pathfinder() != null) entity.pathfinder().clear();
//                if (entity.type.equals(Utils.resources().ENTITIES.load("entities:player"))) entity.jump(500);
//            }
//        }
//        if (Gdx.input.isKeyJustPressed(Input.Keys.S)) {
//            for (Entity entity : world.entities) {
//                entity.setVelocity(-1000f, entity.getVelY());
//            }
//        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.A)) {
            int size = new Random().nextInt(6);
            Element[] elements = new Element[size];
            for (int j = 0; j < size; j++) {
                elements[j] = (Element) Elements.values().toArray()[new Random().nextInt(Elements.values().size())];
            }
            Spell[] spells = Spells.generate(elements);

            String msg = "";
            for (Spell spell : spells) {
                msg = Objects.equals(msg, "") ? spell.name() : msg + ", " + spell.name();
            }
            String elems = "";
            for (Element e : elements) {
                elems = Objects.equals(elems, "") ? e.name() : elems + ", " + e.name();
            }
            if (spells.length == 0) {
                System.out.println("No spell was cast. You used: " + elems);
                return;
            }
            System.out.println("You cast: " + msg + "! You used: " + elems);
        }
    }

    private void handleUIInput(InputSystem i) {
        if (i.isActionJustPressed(InputAction.CANCEL)) {
            ui().close();
        }

        if(Gdx.input.isKeyJustPressed(Input.Keys.J)){
            UI<?> ui = ui().build(Utils.resources().USER_INTERFACES.load("ui:furnace"));
            ui().open(ui);

        }

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

    @Override
    public void dispose() {
        super.dispose();
    }


}
