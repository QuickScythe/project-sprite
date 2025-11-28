package com.sprite.data.utils;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.math.Vector3;
import com.sprite.data.utils.placeholder.PlaceholderUtils;
import com.sprite.resource.magic.elements.Elements;
import com.sprite.resource.magic.spells.Spells;
import org.json.JSONObject;

/**
 * Utility bootstrap class for initializing global systems and providing accessors.
 */
public class Utils {

    /** Global resource manager instance. */
    private static Resources resourceManager;
    private static Preferences settings;
    private static boolean shutdown = false;
    private static Game game;

    /**
     * Initializes core subsystems: resource registry, text localization, elements and spells.
     * Logs a message when initialization completes.
     */
    public static void initialize(Game game) {
        Utils.game = game;
        resourceManager = new Resources("resources");
        Texts.init();
        PlaceholderUtils.registerPlaceholders();
        settings = Gdx.app.getPreferences("player_settings");

        Texts.lang(Texts.Lang.get(settings.getString("language", Texts.Lang.EN_US.tag)), false);

        Elements.initialize();
        Spells.initialize();

        Gdx.app.log("Initialization", "Resources Initialized. Game ready to launch");
    }

    public static Game game(){
        return game;
    }

    public static Preferences settings(){
        return settings;
    }

    /**
     * Returns the global {@link Resources} manager.
     * @return the shared Resources instance
     */
    public static Resources resources() {
        return resourceManager;
    }

    /**
     * Shuts down global systems, disposing resources and clearing registries.
     * Safe to call multiple times.
     */
    public static void shutdown() {
        if (shutdown) return;
        shutdown = true;
        try {
            if (resourceManager != null) resourceManager.dispose();
        } catch (Throwable t) {
            Gdx.app.error("Shutdown", "Error disposing resources", t);
        } finally {
            resourceManager = null;
        }
    }

    public static int distance(Vector3 position, Vector3 position1) {
        return (int) Math.sqrt(Math.pow(position.x - position1.x, 2) + Math.pow(position.y - position1.y, 2));
    }


}
