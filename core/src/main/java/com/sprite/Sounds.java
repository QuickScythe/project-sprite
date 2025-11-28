package com.sprite;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.sprite.data.utils.TaskScheduler;
import com.sprite.data.utils.Utils;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Sounds {

    private static final List<Music> music = new ArrayList<>();
    private static final Map<Long, Sound> sounds = new HashMap<>();

    public static void initialize() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                for(Map.Entry<Long, Sound> entry : sounds.entrySet()){

                }
                TaskScheduler.scheduleAsyncTask(this, 1, TimeUnit.MILLISECONDS);
            }
        };
        TaskScheduler.scheduleAsyncTask(runnable, 1, TimeUnit.MILLISECONDS);
    }

    public static void volume(int volume) {
        System.out.println("Volume: " + volume);
        for(Music music : Sounds.music) music.setVolume(volume/100f);
        for(Map.Entry<Long, Sound> entry : sounds.entrySet()){
            entry.getValue().setVolume(entry.getKey(), volume/100f);
        }
    }


    public static void music(String resourceLocation) {
        Music sound = Utils.resources().MUSIC.load(resourceLocation);
        sound.setVolume(Utils.settings().getInteger("volume")/100f);
        sound.play();
        sound.setOnCompletionListener(music::remove);
        music.add(sound);
    }

    public static void sound(String resourceLocation){
        Sound sound = Utils.resources().SOUNDS.load(resourceLocation);
        long id = sound.play();
        sound.setVolume(id, Utils.settings().getInteger("volume") / 100f);
        sounds.put(id, sound);




        long durationMs = getWavDurationMs(Utils.resources().get(resourceLocation).orElseThrow().file()); // you implement this
        long safetySlackMs = 30; // small buffer for latency
        TaskScheduler.scheduleAsyncTask(() -> {
            // If you modify LibGDX objects or game state, do it on the render thread:
            Gdx.app.postRunnable(() -> {
                // Optional: stop by id in case it’s looping or still playing
                try { sound.stop(id); } catch (Throwable ignored) {}
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
            int byteRate   = ByteBuffer.wrap(header, 28, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
            int dataSize   = ByteBuffer.wrap(header, 40, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
            float seconds  = (float) dataSize / (float) byteRate;
            return (long) (seconds * 1000f);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
