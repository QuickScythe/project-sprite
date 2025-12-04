package com.sprite.data.utils.resources;

import com.badlogic.gdx.Gdx;
import com.sprite.data.TranslatableString;
import com.sprite.data.utils.Utils;
import com.sprite.resource.Resource;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Simple localization helper loading language JSON files and serving translated strings.
 */
public class Texts {

    /** Current language tag (e.g., en_us). Defaults to system locale. */
    private static String tag = Locale.getDefault().toLanguageTag().replace("-", "_").toLowerCase(Locale.ROOT);
    /** In-memory cache mapping translatable keys to localized values. */
    private static final Map<TranslatableString, String> map = new HashMap<>();

    /**
     * Initializes the translation cache for the current language by reading the raw JSON file.
     */
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

    /**
     * Returns the Resource representing the current language JSON file.
     * Falls back to English (en_us) if the current language is not found.
     * @return language resource
     */
    public static Resource file() {
        return Utils.resources().get("lang:" + tag).orElse(Utils.resources().get("lang:en_us").orElseThrow());
    }

    /**
     * Reads the current language JSON into a JSONObject.
     * @return raw JSON object for the active language
     */
    public static JSONObject raw(){
        Resource langFile = file();
        String read = langFile.data.data().get().toString();
        return new JSONObject(read);
    }

    /**
     * Switches the active language and re-initializes the cache.
     * @param lang new language to switch to
     * @param flushSettings whether to save the new language to settings
     */
    public static void lang(Lang lang, boolean flushSettings){
        tag = lang.tag;
        Gdx.app.log("Language", "Switched language to: " + tag);
        init();
        if(flushSettings) {
            Utils.settings().putString("language", lang.tag);
            Utils.settings().flush();
        }

    }

    /**2228
     * Returns the localized value for the given key, if available. If not found, returns the key string itself.
     * @param key translation key wrapper
     * @return localized string or the key when missing
     */
    public static String get(TranslatableString key,String...args) {
        String raw = get(key);
        for(int i = 0; i < args.length; i++) raw = raw.replace("[" + i + "]", args[i]);
        return raw;
    }

    private static String get(TranslatableString key){
        if(map.containsKey(key)) return map.get(key);
        for(Map.Entry<TranslatableString, String> entry : map.entrySet()){
            if(entry.getKey().key().equals(key.key()))
                return entry.getValue();
        }
        return key.key();
    }

    /** Supported built-in language tags. */
    public enum Lang {
        EN_US("en_us"),
        EN_PS("en_ps"),
        EN_UK("en_uk"),
        ES_ES("es_es"),
        FR_FR("fr_fr"),
        FR_CA("fr_ca"),
        DE_DE("de_de"),
        IT_IT("it_it");

        /** IETF tag in lowercase with underscore. */
        public final String tag;

        Lang(String tag) {
            this.tag = tag;
        }

        public static Lang get(String language) {
            for(Lang lang : Lang.values()) {
                if(lang.tag.equals(language)) return lang;
            }
            return Lang.EN_US;
        }
    }
}
