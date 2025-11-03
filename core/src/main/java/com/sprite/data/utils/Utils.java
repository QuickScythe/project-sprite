package com.sprite.data.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.sprite.game.magic.elements.Elements;
import com.sprite.game.magic.spells.Spells;

/**
 * Utility bootstrap class for initializing global systems and providing accessors.
 */
public class Utils {

    /** Global resource manager instance. */
    private static Resources resourceManager;

    /**
     * Initializes core subsystems: resource registry, text localization, elements and spells.
     * Logs a message when initialization completes.
     */
    public static void initialize() {
        resourceManager = new Resources("resources");
        Texts.init();

        Elements.initialize();
        Spells.initialize();

        Gdx.app.log("Initialization", "Resources Initialized. Game ready to launch");
    }

    /**
     * Returns the global {@link Resources} manager.
     * @return the shared Resources instance
     */
    public static Resources resources() {
        return resourceManager;
    }

    public static int distance(Vector3 position, Vector3 position1) {
        return (int) Math.sqrt(Math.pow(position.x - position1.x, 2) + Math.pow(position.y - position1.y, 2));
    }
}
