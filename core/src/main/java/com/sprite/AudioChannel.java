package com.sprite;

public enum AudioChannel {
    MASTER("master", 50),
    MUSIC("music", 100),
    SFX("sfx", 100),
    UI("ui", 100);

    private final String key;
    private final int defaultVolume;

    AudioChannel(String key, int defaultVolume) {
        this.key = key;
        this.defaultVolume = defaultVolume;
    }

    public String key() {
        return key;
    }

    public int defaultVolume() {
        return defaultVolume;
    }


}
