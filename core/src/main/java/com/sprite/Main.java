package com.sprite;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.sprite.data.utils.Texts;
import com.sprite.render.screen.GameScreen;
import com.sprite.render.screen.MenuScreen;
import com.sprite.render.screen.WorldScreen;
import com.sprite.data.utils.Utils;
import com.sprite.tools.EntityEditor;
import com.sprite.tools.InventoryEditor;

import java.util.Map;

/**
 * Main LibGDX Game entry point shared by all platforms.
 * <p>
 * This class bootstraps the game by initializing core systems (resources, language, registries)
 * and setting the initial {@link com.badlogic.gdx.Screen}.
 */
public class Main extends Game {

    public final Map<String, String> LAUNCH_ARGS;


    /**
     * The currently active GameScreen instance, if the active screen is a GameScreen.
     */
    public GameScreen screen;

    public Main(Map<String, String> launchArgs) {
        this.LAUNCH_ARGS = launchArgs;
    }

    /**
     * Initializes core systems and sets the first screen.
     * <ul>
     *   <li>Initializes Resources, Texts, Elements and Spells via {@link Utils#initialize()}.</li>
     *   <li>Demonstrates language switching and translation lookups.</li>
     *   <li>Sets the initial screen to {@link WorldScreen}.</li>
     * </ul>
     */
    @Override
    public void create() {
        for (Map.Entry<String, String> entry : LAUNCH_ARGS.entrySet())
            Gdx.app.log("Launcher", "Argument: " + entry.getKey() + " = " + entry.getValue());
        Utils.initialize();


//        Texts.lang(Texts.Lang.EN_PS.tag);
        if (!LAUNCH_ARGS.containsKey("debugTool")){
            setScreen(new MenuScreen());
            return;
        }
        String toolName = LAUNCH_ARGS.get("debugTool");
        if (toolName == null)
            throw new IllegalArgumentException("Missing debug tool name.");
        Gdx.app.log("Launcher", "Launching debug tool: " + toolName);
        switch (toolName) {
            case "world":
                setScreen(new WorldScreen());
                break;
            case "menu":
                setScreen(new MenuScreen());
                break;
            case "entityEditor":
                setScreen(EntityEditor.screen());
                break;
            case "inventoryEditor":
                setScreen(InventoryEditor.screen());
                break;
            default:
                throw new IllegalArgumentException("Unknown debug tool: " + toolName);
        }
    }

    /**
     * Returns the currently active GameScreen if set via {@link #setScreen(Screen)}.
     *
     * @return the cached GameScreen or null if the active screen is not a GameScreen
     */
    public GameScreen getScreen() {
        return screen;
    }

    /**
     * Sets the current {@link Screen} and caches it if it is an instance of {@link GameScreen}.
     *
     * @param screen the screen to activate
     */
    @Override
    public void setScreen(Screen screen) {
        super.setScreen(screen);
        if (screen instanceof GameScreen gs) this.screen = gs;
    }
}
