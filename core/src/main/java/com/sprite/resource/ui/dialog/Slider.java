package com.sprite.resource.ui.dialog;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.sprite.data.annotations.Nullable;
import com.sprite.data.utils.Utils;
import com.sprite.input.InputAction;
import com.sprite.input.InputSystem;
import com.sprite.render.screen.GameScreen;
import com.sprite.resource.texture.GameSprite;
import com.sprite.resource.texture.NineSliceSprite;
import com.sprite.resource.ui.UIType;

public class Slider extends DialogElement {

    private final float minRange;
    private final float maxRange;
    private final float step;
    private final NineSliceSprite slider;
    private final Knob knob;
    @Nullable
    private final Label label;

    private float value;


    public Slider(String name, String translationKey, float minRange, float maxRange, float defaultValue, float step, boolean label, Vector2 position, Vector2 size) {
        super(name, translationKey, position, size);
        this.minRange = minRange;
        this.maxRange = maxRange;
        this.step = step;
        this.slider = new NineSliceSprite(Utils.resources().TEXTURES.load("textures:ui/slider"), 48, 48, 48, 48);
        this.knob = new Knob(name, translationKey, position, size);
        this.label = label ? new Label(name, translationKey, position, size) : null;
        this.value = defaultValue;

    }

    @Override
    public void draw(GameScreen screen, float x, float y, float width, float height) {
        slider.draw(screen.sprite(), x, y, width, height);
        knob.draw(screen, x, y, width, height);
        if (label != null) {
            label.draw(screen, x, y, width, height);
        }
    }

    @Override
    public void debug(GameScreen screen, float x, float y, float w, float h, UIType type) {
        super.debug(screen, x, y, w, h, type);
        knob.debug(screen, x, y, w, h, type);
    }

    private class Knob extends DialogElement {

        private final GameSprite sprite;
        private boolean dragging = false;

        public Knob(String name, String translationKey, Vector2 position, Vector2 size) {
            super(name, translationKey, new Vector2((int)(position.x + (size.x*(value/maxRange))), position.y), new Vector2(0.5f, size.y));
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
            if (percent < 0f) percent = 0f; else if (percent > 1f) percent = 1f;

            // Draw knob at world-space position based on percent
            float knobWidth = 32f;
            float knobX = x - (knobWidth/2f) + (width * percent);
            screen.sprite().draw(texture(), knobX, y, knobWidth, height);

            // World-space cursor for hit-testing (UI is projected into world)
            Vector3 world = InputSystem.i().worldCursor(screen.camera());

            // Track bounds (full slider bar)
            float trackX = x;
            float trackY = y;
            float trackW = width;
            float trackH = height;

            // Knob bounds
            float knobY = y;
            float knobH = height;

            boolean overTrack = world.x >= trackX && world.x <= trackX + trackW && world.y >= trackY && world.y <= trackY + trackH;
            boolean overKnob = world.x >= knobX && world.x <= knobX + knobWidth && world.y >= knobY && world.y <= knobY + knobH;

            boolean justPressed = InputSystem.i().isActionJustPressed(InputAction.Primary);
            boolean pressed = InputSystem.i().isActionPressed(InputAction.Primary);

            // Start dragging if the user pressed on the knob or anywhere on the track
            if (justPressed && (overKnob || overTrack)) {
                dragging = true;
            }
            // Stop dragging on mouse/button release
            if (!pressed) {
                dragging = false;
            }

            // While dragging, update value according to cursor position along the track
            if (dragging && pressed) {
                float newPercent = (world.x - trackX) / trackW;
                if (newPercent < 0f) newPercent = 0f; else if (newPercent > 1f) newPercent = 1f;
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
                value = snapped;
            }

            // Update interaction flags for consumers using DialogElement API
            boolean hovered = overKnob; // consider hovered only when over knob
            setInteractionState(hovered, dragging && pressed, justPressed && (overKnob || overTrack));
        }

        @Override
        public void debug(GameScreen screen, float x, float y, float w1, float h1, UIType type) {
            screen.shape().setColor(Color.GREEN);
            float camX = (screen.camera().position.x - screen.camera().viewportWidth / 2);
            float camY = (screen.camera().position.y - screen.camera().viewportHeight / 2);
            Vector2 pos = type.position(screen, Knob.this.position());
            float w = (Knob.this.size().x * type.gridWidthSize());
            float h = (Knob.this.size().y * type.gridHeightSize());
            float range = Math.max(0.0001f, (maxRange - minRange));
            float percent = (value - minRange) / range;

            // Hit-test using world-space cursor (UI is drawn in world space relative to camera)
            Vector3 world = InputSystem.i().worldCursor(screen.camera());
            float worldPosX = camX + (pos.x - w/2 + (w1*percent));
            float worldPosY = camY + pos.y;
            boolean hovered = world.x >= worldPosX && world.x <= worldPosX + w && world.y >= worldPosY && world.y <= worldPosY + h;
            boolean pressed = hovered && InputSystem.i().isActionPressed(InputAction.Primary);
            boolean justClicked = hovered && InputSystem.i().isActionJustPressed(InputAction.Primary);
            Knob.this.setInteractionState(hovered, pressed, justClicked);

            super.debug(screen, worldPosX, worldPosY, w, h, type);
        }
    }
}
