package com.sprite;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.sprite.data.TranslatableString;
import com.sprite.screen.FirstScreen;
import com.sprite.screen.GameScreen;
import com.sprite.utils.Texts;
import com.sprite.utils.Utils;

/**
 * Main LibGDX Game entry point shared by all platforms.
 * <p>
 * This class bootstraps the game by initializing core systems (resources, language, registries)
 * and setting the initial {@link com.badlogic.gdx.Screen}.
 */
public class Main extends Game {

    /** The currently active GameScreen instance, if the active screen is a GameScreen. */
    public GameScreen screen;

    /**
     * Initializes core systems and sets the first screen.
     * <ul>
     *   <li>Initializes Resources, Texts, Elements and Spells via {@link Utils#initialize()}.</li>
     *   <li>Demonstrates language switching and translation lookups.</li>
     *   <li>Sets the initial screen to {@link FirstScreen}.</li>
     * </ul>
     */
    @Override
    public void create() {
        Utils.initialize();

        System.out.println("Initialization Complete.");
        Texts.lang(Texts.Lang.EN_US.tag);
        TranslatableString test = new TranslatableString("entity.test.name");
        System.out.println(Texts.get(test));
        Texts.lang(Texts.Lang.FR_FR.tag);
        System.out.println(Texts.get(test));
        Texts.lang(Texts.Lang.ES_ES.tag);
        System.out.println(Texts.get(test));

        setScreen(new FirstScreen());
//        Element earth = Elements.get("magic:element/earth").orElseThrow();
//        Element fire = Elements.get("magic:element/fire").orElseThrow();
//        Element wind = Elements.get("magic:element/wind").orElseThrow();
//        Spell[] spells = Spells.generate(earth, wind, fire);
//
//        for (Spell spell : spells) {
//            System.out.println(spell.name());
//        }
    }

    /**
     * Returns the currently active GameScreen if set via {@link #setScreen(Screen)}.
     * @return the cached GameScreen or null if the active screen is not a GameScreen
     */
    public GameScreen getScreen() {
        return screen;
    }

    /**
     * Sets the current {@link Screen} and caches it if it is an instance of {@link GameScreen}.
     * @param screen the screen to activate
     */
    @Override
    public void setScreen(Screen screen) {
        super.setScreen(screen);
        if(screen instanceof GameScreen gs) this.screen = gs;
    }
}
