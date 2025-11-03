package com.sprite.lwjgl3;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.sprite.Main;

import java.util.HashMap;
import java.util.Map;

/** Launches the desktop (LWJGL3) application. */
public class Launcher {

    public static Map<String, String> LAUNCH_ARGS = new HashMap<>();

    public static void main(String[] args) {
        LAUNCH_ARGS.put("width", "640");
        LAUNCH_ARGS.put("height", "480");
        LAUNCH_ARGS.put("title", "project-sprite");
        LAUNCH_ARGS.put("vSync", "true");
        LAUNCH_ARGS.put("backgroundFPS", "0");
        LAUNCH_ARGS.put("resizable", "false");
        LAUNCH_ARGS.put("icon", "libgdx128.png");

        for(String arg : args){
            if(arg.startsWith("--") && arg.contains("=")){
                String[] split = arg.split("=");
                String label = split[0].substring(2);
                String value = split[1];
                LAUNCH_ARGS.put(label, value);
            }
        }
        if (StartupHelper.startNewJvmIfRequired()) return; // This handles macOS support and helps on Windows.
        createApplication(LAUNCH_ARGS);
    }

    private static Lwjgl3Application createApplication(Map<String, String> launchArgs) {
        Lwjgl3Application app = new Lwjgl3Application(new Main(launchArgs), getDefaultConfiguration());

        return app;
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle(LAUNCH_ARGS.getOrDefault("title", "project-sprite"));
        //// Vsync limits the frames per second to what your hardware can display, and helps eliminate
        //// screen tearing. This setting doesn't always work on Linux, so the line after is a safeguard.
        configuration.useVsync(Boolean.parseBoolean(LAUNCH_ARGS.getOrDefault("vSync", "true")));
        //// Limits FPS to the refresh rate of the currently active monitor, plus 1 to try to match fractional
        //// refresh rates. The Vsync setting above should limit the actual FPS to match the monitor.
        configuration.setForegroundFPS(Integer.parseInt(LAUNCH_ARGS.getOrDefault("foregroundFPS", (Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate + 1) + "")));
        //// If you remove the above line and set Vsync to false, you can get unlimited FPS, which can be
        //// useful for testing performance, but can also be very stressful to some hardware.
        //// You may also need to configure GPU drivers to fully disable Vsync; this can cause screen tearing.

        configuration.setWindowedMode(
            Integer.parseInt(LAUNCH_ARGS.getOrDefault("width", "640")),
            Integer.parseInt(LAUNCH_ARGS.getOrDefault("height", "480")));
        //// You can change these files; they are in lwjgl3/src/main/resources/ .
        //// They can also be loaded from the root of assets/ .
        configuration.setWindowIcon("libgdx128.png", "libgdx64.png", "libgdx32.png", "libgdx16.png");
        return configuration;
    }
}
