package com.sprite.data.utils;

import com.badlogic.gdx.Gdx;
import com.sprite.data.utils.resources.Texts;
import com.sprite.render.screen.GameScreen;
import com.sprite.render.screen.WorldScreen;
import com.sprite.render.ui.UI;
import com.sprite.resource.ui.UIDefinition;
import org.json.JSONObject;

public class ActionUtils {

    public static void log(JSONObject data) {
        if (Utils.game().getScreen() instanceof GameScreen screen) screen.requestMusic();
        Gdx.app.log(data.optString("tag", "UIAction"), data.getString("value"));
    }

    public static void lang(JSONObject data) {
        Texts.lang(Texts.Lang.valueOf(data.getString("value")), data.optBoolean("flush", true));
    }

    public static void open(JSONObject data) {
        GameScreen.UIManager uiManager = ((GameScreen) Utils.game().getScreen()).ui();
        UIDefinition uidef = Utils.resources().USER_INTERFACES.load(data.getString("value"));
        UI<?> ui = uiManager.build(uidef);
        uiManager.open(ui);
    }

    public static void close(){
        GameScreen.UIManager uiManager = ((GameScreen) Utils.game().getScreen()).ui();
        uiManager.close();
    }

    public static void play() {
        Utils.game().setScreen(new WorldScreen());
    }

    public static void exit() {
        Gdx.app.log("Game", "Exiting...");
        Gdx.app.exit();
    }


}
