package com.sprite;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.sprite.data.TranslatableString;
import com.sprite.magic.elements.Element;
import com.sprite.magic.elements.Elements;
import com.sprite.magic.spells.Spell;
import com.sprite.magic.spells.Spells;
import com.sprite.screen.FirstScreen;
import com.sprite.screen.GameScreen;
import com.sprite.utils.Texts;
import com.sprite.utils.Utils;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends Game {

    public GameScreen screen;


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
    public GameScreen getScreen() {
        return screen;
    }

    @Override
    public void setScreen(Screen screen) {
        super.setScreen(screen);
        if(screen instanceof GameScreen gs) this.screen = gs;
    }
}
