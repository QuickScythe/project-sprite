package com.sprite.data.utils.audio;

public class Audio<T extends Audio.Type> {


    public static abstract class Type {

        public static final Type MUSIC = new Type() {};
        public static final Type SOUND = new Type() {};

    }
}
