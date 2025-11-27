package com.sprite.data.utils;

import com.badlogic.gdx.Gdx;
import com.sprite.render.screen.WorldScreen;
import org.json.JSONObject;

public class ActionUtils {

    public static void log(JSONObject data){
        Gdx.app.log(data.optString("tag", "UIAction"), data.getString("value"));
    }

    public static void lang(JSONObject data){
        Texts.lang(Texts.Lang.valueOf(data.getString("value")), data.optBoolean("flush", true));
    }

    public static void play(){
        Utils.game().setScreen(new WorldScreen());
    }

    public static void exit(){
        Gdx.app.log("Game", "Exiting...");
        Gdx.app.exit();
    }
}
