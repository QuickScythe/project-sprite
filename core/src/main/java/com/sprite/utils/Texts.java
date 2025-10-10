package com.sprite.utils;

import com.badlogic.gdx.Gdx;
import com.sprite.data.TranslatableString;
import com.sprite.data.registries.Registries;
import com.sprite.data.registries.Registry;
import com.sprite.resource.Resource;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Texts {

    private static String tag = Locale.getDefault().toLanguageTag().replace("-", "_").toLowerCase(Locale.ROOT);
    private static final Map<TranslatableString, String> map = new HashMap<>();

    public static void init(){
        Gdx.app.log("Language", "Initializing language: " + tag);
        map.clear();
        JSONObject data = raw();
        for (String key : data.keySet()) {
            String value = data.getString(key);
            TranslatableString text = new TranslatableString(key);
            map.put(text, value);
        }
    }

    public static Resource<?> file() {
        return Utils.resources().get("lang:" + tag).orElse(Utils.resources().get("lang:en_us").orElseThrow());
    }

    public static JSONObject raw(){
        Resource<?> langFile = file();
        String read = langFile.file().readString();
        return new JSONObject(read);
    }

    public static void lang(String languageTag){
        tag = languageTag.replace("-", "_").toLowerCase(Locale.ROOT);
        Gdx.app.log("Language", "Switched language to: " + tag);
        init();
    }


    public static String get(TranslatableString key) {
        if(map.containsKey(key)) return map.get(key);
        for(Map.Entry<TranslatableString, String> entry : map.entrySet()){
            if(entry.getKey().key.equals(key.key))
                return entry.getValue();
        }
        return key.key;
    }

    public enum Lang {
        EN_US("en_us"),
        EN_UK("en_uk"),
        ES_ES("es_es"),
        FR_FR("fr_fr"),
        FR_CA("fr_ca"),
        DE_DE("de_de"),
        IT_IT("it_it");

        public final String tag;

        Lang(String tag) {
            this.tag = tag;
        }
    }
}
