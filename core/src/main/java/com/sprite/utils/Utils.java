package com.sprite.utils;

import com.sprite.magic.elements.Elements;
import com.sprite.magic.spells.Spells;

public class Utils {


    private static Resources resourceManager;

    public static void initialize() {
        resourceManager = new Resources("resources");
        Texts.init();

//        resourceManager.RESOURCE_REGISTRY.forEach((key, resource) -> {
//            System.out.println(key + " -> " + resource.file().toString());
//        });

        Elements.initialize();
        Spells.initialize();



    }

    public static Resources resources() {
        return resourceManager;
    }


}
