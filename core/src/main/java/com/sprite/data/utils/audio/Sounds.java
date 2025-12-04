package com.sprite.data.utils.audio;

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
    // Track which channel each active instance belongs to
    private static final Map<Music, AudioChannel> musicChannels = new IdentityHashMap<>();
    private static final Map<Long, AudioChannel> soundChannels = new HashMap<>();
    private static final Map<AudioChannel, Integer> volumeMap = new EnumMap<>(AudioChannel.class);
    // Cache parsed audio durations to avoid repeated file I/O per play
    private static final Map<String, Long> durationCache = new HashMap<>();
    // Toggle verbose logging if needed during development
    private static final boolean DEBUG_LOG = false;

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
        // Precompute master scalar to avoid repeated map lookups and divisions
        float masterScalar = volumeMap.get(AudioChannel.MASTER) / 100f;

        // Update only affected instances: all when MASTER changes, otherwise only that channel
        for (Music m : Sounds.music) {
            AudioChannel ch = musicChannels.get(m);
            if (ch == null) continue; // safety
            if (channel == AudioChannel.MASTER || ch == channel) {
                float chScalar = (ch == AudioChannel.MASTER) ? 1f : (volumeMap.get(ch) / 100f);
                m.setVolume(chScalar * masterScalar);
            }
        }
        for (Map.Entry<Long, Sound> entry : sounds.entrySet()) {
            Long id = entry.getKey();
            Sound s = entry.getValue();
            AudioChannel ch = soundChannels.get(id);
            if (ch == null) continue;
            if (channel == AudioChannel.MASTER || ch == channel) {
                float chScalar = (ch == AudioChannel.MASTER) ? 1f : (volumeMap.get(ch) / 100f);
                s.setVolume(id, chScalar * masterScalar);
            }
        }
    }

    private static float mix(AudioChannel channel) {
        return volumeMap.get(channel) / 100f * (channel.equals(AudioChannel.MASTER) ? 1 : volumeMap.get(AudioChannel.MASTER) / 100f);
    }

    public static void background(String resourceLocation, AudioChannel channel) {
        if (DEBUG_LOG) Gdx.app.log("Sounds", "Now playing (bg): " + resourceLocation);
        for (Music music : Sounds.music) {
            music.stop();
            music.dispose();
            musicChannels.remove(music);
        }
        Music sound = Utils.resources().MUSIC.load(resourceLocation);
        sound.setVolume(mix(channel));
        sound.play();
        sound.setOnCompletionListener((finished) -> {
            music.remove(finished);
            musicChannels.remove(finished);
            if (Utils.game().getScreen() instanceof GameScreen screen)
                screen.requestMusic();
        });
        music.add(sound);
        musicChannels.put(sound, channel);
    }


    public static void music(String resourceLocation, AudioChannel channel) {

        Music sound = Utils.resources().MUSIC.load(resourceLocation);
        sound.setVolume(mix(channel));
        sound.play();
        sound.setOnCompletionListener((finished) -> {
            music.remove(finished);
            musicChannels.remove(finished);
        });
        music.add(sound);
        musicChannels.put(sound, channel);
    }

    public static void sound(String resourceLocation, AudioChannel channel) {
        if (DEBUG_LOG) Gdx.app.log("Sounds", "Now playing (sfx): " + resourceLocation);
        Sound sound = Utils.resources().SOUNDS.load(resourceLocation);
        long id = sound.play();
        sound.setVolume(id, mix(channel));
        sounds.put(id, sound);
        soundChannels.put(id, channel);


        long durationMs = getDurationMs(resourceLocation, Utils.resources().get(resourceLocation).orElseThrow().file());
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
                soundChannels.remove(id);
            });
        }, durationMs + safetySlackMs, TimeUnit.MILLISECONDS);
    }

    static long getWavDurationMs(FileHandle fh) {
        // Backwards-compat method; use uncached path
        return parseWavDurationSafe(fh, 1000L);
    }

    /**
     * Returns duration (ms) for the provided resource, using an in-memory cache to avoid
     * repeated file I/O. Supports WAV header parsing; for other formats or failures,
     * falls back to a sane default short duration to ensure scheduled cleanup still occurs.
     */
    static long getDurationMs(String resourceLocation, FileHandle fh) {
        Long cached = durationCache.get(resourceLocation);
        if (cached != null) return cached;

        long duration;
        String name = fh.name().toLowerCase(Locale.ROOT);
        if (name.endsWith(".wav")) {
            duration = parseWavDurationSafe(fh, 1000L);
        } else {
            // Unsupported precise parsing – choose a conservative default
            duration = 1000L; // 1 second default for short UI/SFX clicks
        }
        durationCache.put(resourceLocation, duration);
        return duration;
    }

    private static long parseWavDurationSafe(FileHandle fh, long fallbackMs) {
        try (DataInputStream in = new DataInputStream(fh.read())) {
            byte[] header = new byte[44];
            in.readFully(header);
            int byteRate = ByteBuffer.wrap(header, 28, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
            int dataSize = ByteBuffer.wrap(header, 40, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
            if (byteRate <= 0) return fallbackMs;
            float seconds = (float) dataSize / (float) byteRate;
            long ms = (long) (seconds * 1000f);
            // Guard against zero/absurd values
            return ms > 0 ? ms : fallbackMs;
        } catch (Throwable ignored) {
            return fallbackMs;
        }
    }

    /**
     * Stops and disposes all active music and sound instances, clears internal collections,
     * and prevents further task rescheduling. Safe to call multiple times.
     */
    public static void dispose() {
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
        musicChannels.clear();

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
        soundChannels.clear();
        durationCache.clear();
    }

    // Query helpers to inspect current routing
    public static Optional<AudioChannel> channelOf(Music m) {
        return Optional.ofNullable(musicChannels.get(m));
    }

    public static Optional<AudioChannel> channelOf(long soundId) {
        return Optional.ofNullable(soundChannels.get(soundId));
    }
}
