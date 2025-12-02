package com.sprite;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.sprite.data.utils.TaskScheduler;
import com.sprite.data.utils.Utils;
import com.sprite.render.screen.GameScreen;
import com.sprite.resource.Resource;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Sounds {

    private static final List<Music> music = new ArrayList<>();
    private static final Map<Long, Sound> sounds = new HashMap<>();
    private static final Map<AudioChannel, Integer> volumeMap = new EnumMap<>(AudioChannel.class);
    private static volatile boolean shuttingDown = false;

    public static void initialize() {
        for (AudioChannel channel : AudioChannel.values()) {
            volumeMap.put(channel, Utils.settings().getInteger("volume_" + channel.key()));
            System.out.println("Volume (" + channel.key() + "): " + volumeMap.get(channel));
        }
    }

    public static void play(String resourceLocation, AudioChannel channel){
        Resource.Location location = Resource.Location.fromString(resourceLocation);
        if (location.namespace().equalsIgnoreCase("sounds")) {
            sound(resourceLocation, channel);
        }
        if(location.namespace().equalsIgnoreCase("music")){
            background(resourceLocation, channel);
        }
    }

    public static void volume(AudioChannel channel, int volume) {
        volumeMap.put(channel, volume);
        for (Music music : Sounds.music) music.setVolume(mix(channel));
        for (Map.Entry<Long, Sound> entry : sounds.entrySet()) {
            entry.getValue().setVolume(entry.getKey(), volume / 100f);
        }
    }

    private static float mix(AudioChannel channel) {
        return volumeMap.get(channel) / 100f * (channel.equals(AudioChannel.MASTER) ? 1 : volumeMap.get(AudioChannel.MASTER) / 100f);
    }

    public static void background(String resourceLocation, AudioChannel channel) {
        Gdx.app.log("Sounds", "Now playing: " + resourceLocation);
        for (Music music : Sounds.music) {
            music.stop();
            music.dispose();
        }
        Music sound = Utils.resources().MUSIC.load(resourceLocation);
        sound.setVolume(mix(channel));
        sound.play();
        sound.setOnCompletionListener((finished) -> {
            music.remove(finished);
            if (Utils.game().getScreen() instanceof GameScreen screen)
                screen.requestMusic();
        });
        music.add(sound);
    }


    public static void music(String resourceLocation, AudioChannel channel) {

        Music sound = Utils.resources().MUSIC.load(resourceLocation);
        sound.setVolume(mix(channel));
        sound.play();
        sound.setOnCompletionListener(music::remove);
        music.add(sound);
    }

    public static void sound(String resourceLocation, AudioChannel channel) {
        Gdx.app.log("Sounds", "Now playing: " + resourceLocation);
        Sound sound = Utils.resources().SOUNDS.load(resourceLocation);
        long id = sound.play();
        sound.setVolume(id, mix(channel));
        sounds.put(id, sound);


        long durationMs = getWavDurationMs(Utils.resources().get(resourceLocation).orElseThrow().file()); // you implement this
        long safetySlackMs = 30; // small buffer for latency
        TaskScheduler.scheduleAsyncTask(() -> {
            // If you modify LibGDX objects or game state, do it on the render thread:
            Gdx.app.postRunnable(() -> {
                // Optional: stop by id in case it’s looping or still playing
                try {
                    sound.stop(id);
                } catch (Throwable ignored) {
                }
                sounds.remove(id);
            });
        }, durationMs + safetySlackMs, TimeUnit.MILLISECONDS);
    }

    static long getWavDurationMs(FileHandle fh) {
        try (DataInputStream in = new DataInputStream(fh.read())) {
            // Skip RIFF header to fmt and data chunks (production code should actually parse chunks)
            byte[] header = new byte[44];
            in.readFully(header);
            int sampleRate = ByteBuffer.wrap(header, 24, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
            int byteRate = ByteBuffer.wrap(header, 28, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
            int dataSize = ByteBuffer.wrap(header, 40, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
            float seconds = (float) dataSize / (float) byteRate;
            return (long) (seconds * 1000f);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Stops and disposes all active music and sound instances, clears internal collections,
     * and prevents further task rescheduling. Safe to call multiple times.
     */
    public static void dispose() {
        shuttingDown = true;
        // Stop music
        for (Music m : new ArrayList<>(music)) {
            try {
                m.stop();
            } catch (Throwable ignored) {
            }
            try {
                m.dispose();
            } catch (Throwable ignored) {
            }
        }
        music.clear();

        // Stop and dispose sounds
        for (Map.Entry<Long, Sound> e : new ArrayList<>(sounds.entrySet())) {
            Sound s = e.getValue();
            try {
                s.stop(e.getKey());
            } catch (Throwable ignored) {
            }
            try {
                s.dispose();
            } catch (Throwable ignored) {
            }
        }
        sounds.clear();
    }
}
