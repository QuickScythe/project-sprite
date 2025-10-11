package com.sprite.utils;

import com.badlogic.gdx.Gdx;
import com.sprite.magic.elements.Elements;
import com.sprite.magic.spells.Spells;

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
}
