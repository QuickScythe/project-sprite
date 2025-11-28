package com.sprite.data.utils.placeholder;

import com.sprite.Viewer;
import com.sprite.data.registries.Registries;
import com.sprite.data.registries.Registry;
import com.sprite.resource.ui.dialog.Label;
import com.sprite.resource.ui.dialog.Slider;

import java.util.Map.Entry;
import java.util.Optional;


public class PlaceholderUtils {

    private static final Registry<Placeholder> registry = Registries.register("placeholders", ()->null);

    public static void registerPlaceholders() {
//        registerPlaceholder("name", player -> player.map(audience -> serialize(audience.get(Identity.DISPLAY_NAME).orElse(Component.text("Unknown")))).orElse("Unknown"));
//        registerPlaceholder("online", (player) -> Bukkit.getOnlinePlayers().size() + "");
        registerPlaceholder("value", viewer -> {
            if(viewer.isPresent()){
                if(viewer.get() instanceof Label label){
                    return label.string();
                }
            }
            return "0";
        });
        registerPlaceholder("valuePercent", viewer -> {
            if(viewer.isPresent()){
                if(viewer.get() instanceof Slider.SliderLabel label){
                    return label.string();
                }
            }
            return "0";
        });
    }

    public static void registerPlaceholder(String key, Placeholder worker) {
        registry.register("%" + key + "%", worker);
    }


    public static String replace(Viewer viewer, String string) {

        for (Entry<String, Placeholder> e : registry.toMap().entrySet()) {
            if (string.contains(e.getKey())) {
                string = string.replaceAll(e.getKey(), e.getValue().run(Optional.ofNullable(viewer)));
            }
        }

        string = emotify(string);
        return string;
    }

    public static String emotify(String string) {
        String tag = string;
        while (tag.contains("%symbol:")) {
            String icon = tag.split("ymbol:")[1].split("%")[0];
            if (Symbols.valueOf(icon.toUpperCase()) == null) {
                tag = tag.replaceAll("%symbol:" + icon + "%", Symbols.UNKNOWN.toString());
            } else {
                tag = tag.replaceAll("%symbol:" + icon + "%", Symbols.valueOf(icon.toUpperCase()).toString());
            }
        }
        return tag;
    }

    @FunctionalInterface
    public interface Placeholder {
        String run(Optional<Viewer> viewer);
    }
}
