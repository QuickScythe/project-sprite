package com.sprite.resource.ui.dialog;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.sprite.data.annotations.Nullable;
import com.sprite.data.utils.Utils;
import com.sprite.data.utils.audio.AudioChannel;
import com.sprite.data.utils.audio.Sounds;
import com.sprite.input.InputAction;
import com.sprite.input.InputSystem;
import com.sprite.render.screen.GameScreen;
import com.sprite.resource.texture.GameSprite;
import com.sprite.resource.texture.NineSliceSprite;
import com.sprite.resource.ui.DialogUI;

import java.util.Locale;

public class Slider extends DialogElement {

    private final float minRange;
    private final float maxRange;
    private final float step;
    private final NineSliceSprite slider;
    private final Knob knob;
    @Nullable
    private final SliderLabel label;

    private float value;
    // Tracks the last value that was committed (on release) to avoid playing a sound on every change
    private float lastCommittedValue;
    // Optional settings key to persist value changes
    @Nullable
    private String settingsKey;


    public Slider(String name, String translationKey, float minRange, float maxRange, float defaultValue, float step, boolean label, Vector2 position, Vector2 size, DialogUI dialog) {
        super(name, translationKey, position, size, dialog);
        this.minRange = minRange;
        this.maxRange = maxRange;
        this.step = step;
        this.slider = new NineSliceSprite(Utils.resources().TEXTURES.load("textures:ui/slider"), 48, 48, 48, 48);
        this.knob = new Knob(name, translationKey, position, size, dialog);
        this.label = label ? new SliderLabel(name, translationKey, position, size, dialog) : null;
        this.value = defaultValue;
        this.lastCommittedValue = defaultValue;

    }

    /**
     * Assign a settings key so this slider loads/saves its value to preferences.
     */
    public void setSettingsKey(String key) {
        this.settingsKey = key;
    }

    @Override
    public void draw(GameScreen screen, float x, float y, float width, float height) {
        slider.draw(screen.sprite(), x, y, width, height);
        knob.draw(screen, x, y, width, height);
        if (label != null) {
            label.draw(screen, x, y, width, height);
        }
        if (isJustHovered()) {
            Sounds.play("sounds:hover", AudioChannel.UI);
        }

    }

    @Override
    public void debug(GameScreen screen, float x, float y, float w, float h) {
        super.debug(screen, x, y, w, h);
        knob.debug(screen, x, y, w, h);
    }


    private class Knob extends DialogElement {

        private final GameSprite sprite;
        private boolean dragging = false;

        public Knob(String name, String translationKey, Vector2 position, Vector2 size, DialogUI dialog) {
            super(name, translationKey, new Vector2((int) (position.x + (size.x * (value / maxRange))), position.y), new Vector2(0.5f, size.y), dialog);
            this.sprite = Utils.resources().TEXTURES.load("textures:ui/knob");
        }


        public Sprite texture() {
            return sprite.sprite();
        }

        @Override
        public void draw(GameScreen screen, float x, float y, float width, float height) {
            // Compute percentage of the slider (0..1)
            float range = Math.max(0.0001f, (maxRange - minRange));
            float percent = (value - minRange) / range;
            if (percent < 0f) percent = 0f;
            else if (percent > 1f) percent = 1f;

            // Draw knob at world-space position based on percent

            float knobWidth = size().x * dialog().gridWidthSize();

//            float worldPosX = cam.x + (pos.x - w/2 + (w1*percent)) + (dialog().gridWidthSize());

            float knobX = x - (knobWidth / 2f) + ((width - (dialog().gridWidthSize() * 2)) * percent) + (dialog().gridWidthSize());
            screen.sprite().draw(texture(), knobX, y, knobWidth, height);

            // World-space cursor for hit-testing (UI is projected into world)
            Vector3 world = InputSystem.i().worldCursor(screen.camera());

            // Track bounds (padded to match visual track: full width - gridWidth*2, centered)
            float pad = dialog().gridWidthSize();
            float trackX = x + pad;
            float trackY = y;
            float trackW = width - (pad * 2f);
            float trackH = height;

            // Knob bounds
            float knobY = y;
            float knobH = height;

            boolean overTrack = world.x >= trackX && world.x <= trackX + trackW && world.y >= trackY && world.y <= trackY + trackH;
            boolean overKnob = world.x >= knobX && world.x <= knobX + knobWidth && world.y >= knobY && world.y <= knobY + knobH;

            boolean justPressed = InputSystem.i().isActionJustPressed(InputAction.PRIMARY);
            boolean pressed = InputSystem.i().isActionPressed(InputAction.PRIMARY);

            // Start dragging if the user pressed on the knob or anywhere on the track
            if (justPressed && (overKnob || overTrack)) {
                dragging = true;

            }
            if (dragging) {
                if (settingsKey != null && !settingsKey.isEmpty()) {
                    if (settingsKey.toLowerCase(Locale.ENGLISH).startsWith("volume_")) {
                        int volume = Math.round(value);
                        Utils.settings().putInteger(settingsKey, volume);
                        String channel = settingsKey.replace("volume_", "");
                        Sounds.volume(AudioChannel.valueOf(channel.toUpperCase(Locale.ENGLISH)), volume);
                    }
                }

            }
            // Stop dragging on mouse/button release
            if (!pressed) {
                // If we were dragging and value changed since last commit, persist and play sound once
                if (dragging) {
                    try {
                        if (value != lastCommittedValue) {
                            Utils.settings().flush();
                            Sounds.play("sounds:click", AudioChannel.UI);
                            lastCommittedValue = value;
                        }
                    } catch (Throwable ignored) {
                    }
                }
                dragging = false;
            }

            // While dragging, update value according to cursor position along the track
            if (dragging && pressed) {
                float newPercent = (world.x - trackX) / trackW;
                if (newPercent < 0f) newPercent = 0f;
                else if (newPercent > 1f) newPercent = 1f;
                float rawValue = minRange + newPercent * (maxRange - minRange);
                // Snap to step
                float snapped;
                if (step > 0f) {
                    float steps = Math.round((rawValue - minRange) / step);
                    snapped = minRange + steps * step;
                } else {
                    snapped = rawValue;
                }
                // Clamp
                if (snapped < minRange) snapped = minRange;
                if (snapped > maxRange) snapped = maxRange;
                if (value != snapped) {
                    // Update the live value while dragging, but don't persist or play sound yet
                    value = snapped;
                }
            }

            // Update interaction flags for consumers using DialogElement API
            boolean hovered = overKnob; // consider hovered only when over knob
            setInteractionState(hovered, dragging && pressed, justPressed && (overKnob || overTrack));
        }

        @Override
        public void debug(GameScreen screen, float x, float y, float w1, float h1) {
            screen.shape().setColor(Color.GREEN);
            Vector2 cam = dialog().cameraOrigin(screen);
            Vector2 pos = dialog().position(screen, Knob.this.position());
            float w = (Knob.this.size().x * dialog().gridWidthSize());
            float h = (Knob.this.size().y * dialog().gridHeightSize());
            float range = Math.max(0.0001f, (maxRange - minRange));
            float percent = (value - minRange) / range;
            w1 = w1 - (dialog().gridWidthSize() * 2);

            // Hit-test using world-space cursor (UI is drawn in world space relative to camera)
            Vector3 world = InputSystem.i().worldCursor(screen.camera());
            float worldPosX = cam.x + (pos.x - w / 2 + (w1 * percent)) + (dialog().gridWidthSize());
            float worldPosY = cam.y + pos.y;
            boolean hovered = world.x >= worldPosX && world.x <= worldPosX + w && world.y >= worldPosY && world.y <= worldPosY + h;
            boolean pressed = hovered && InputSystem.i().isActionPressed(InputAction.PRIMARY);
            boolean justClicked = hovered && InputSystem.i().isActionJustPressed(InputAction.PRIMARY);
            Knob.this.setInteractionState(hovered, pressed, justClicked);

            super.debug(screen, worldPosX, worldPosY, w, h);
        }
    }

    public class SliderLabel extends Label {
        public SliderLabel(String name, String translationKey, Vector2 position, Vector2 size, DialogUI dialog) {
            super(name, translationKey, position, size, dialog);
        }

        @Override
        public String string() {
            return value + "";
        }
    }
}
