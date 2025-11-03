package com.sprite;

import org.robovm.apple.foundation.NSAutoreleasePool;
import org.robovm.apple.uikit.UIApplication;

import com.badlogic.gdx.backends.iosrobovm.IOSApplication;
import com.badlogic.gdx.backends.iosrobovm.IOSApplicationConfiguration;
import com.sprite.Main;

import java.util.HashMap;
import java.util.Map;

/** Launches the iOS (RoboVM) application. */
public class IOSLauncher extends IOSApplication.Delegate {

    public static Map<String, String> LAUNCH_ARGS = new HashMap<>();


    @Override
    protected IOSApplication createApplication() {

        IOSApplicationConfiguration configuration = new IOSApplicationConfiguration();
        return new IOSApplication(new Main(LAUNCH_ARGS), configuration);
    }

    public static void main(String[] argv) {
        LAUNCH_ARGS.put("width", "640");
        LAUNCH_ARGS.put("height", "480");
        LAUNCH_ARGS.put("title", "project-sprite");
        LAUNCH_ARGS.put("vSync", "true");
        LAUNCH_ARGS.put("backgroundFPS", "0");
        LAUNCH_ARGS.put("resizable", "false");
        LAUNCH_ARGS.put("icon", "libgdx128.png");

        for(String arg : argv){
            if(arg.startsWith("--") && arg.contains("=")){
                String[] split = arg.split("=");
                String label = split[0].substring(2);
                String value = split[1];
                LAUNCH_ARGS.put(label, value);
            }
        }
        NSAutoreleasePool pool = new NSAutoreleasePool();
        UIApplication.main(argv, null, IOSLauncher.class);
        pool.close();
    }
}
