package com.sprite.render.ui.dialog;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.sprite.input.InputAction;
import com.sprite.input.InputSystem;
import com.sprite.render.screen.GameScreen;
import com.sprite.render.ui.UI;
import com.sprite.resource.ui.DialogUI;
import com.sprite.resource.ui.dialog.DialogElement;

public class Dialog extends UI<DialogUI> {

    public Dialog(DialogUI type) {
        super(type);
    }

    @Override
    public void debug(GameScreen screen) {
        screen.shape().setColor(Color.YELLOW);
        Vector2 cam = type().cameraOrigin(screen);
        Vector3 cursor = InputSystem.i().worldCursor(screen.camera());

        for (int x = 0; x < type().columns(); x++) {
            for (int y = 0; y < type().rows(); y++) {
                int index = y * type().columns() + x;
                Vector2 pos = type().position(screen, index);

                screen.shape().rect(pos.x, pos.y, type().gridWidthSize(), type().gridHeightSize());
            }
        }
        screen.shape().setColor(Color.RED);
        for (DialogElement element : type().elements().values()) {
            // Compute element world-space rect
            Vector2 pos = type().position(screen, element.position());
            float w = (element.size().x * type().gridWidthSize());
            float h = (element.size().y * type().gridHeightSize());

            // Hit-test using world-space cursor (UI is drawn in world space relative to camera)

            element.setInteractionState(screen, cam, cursor);
            screen.shape().setColor(Color.RED);
            element.debug(screen, cam.x + pos.x, cam.y + pos.y, w, h);
        }
    }
    @Override
    public void draw(GameScreen screen) {
        screen.shape().setColor(Color.YELLOW);
        type().draw(screen);
        Vector2 cam = type().cameraOrigin(screen);
        Vector3 cursor = InputSystem.i().worldCursor(screen.camera());

        for (DialogElement element : type().elements().values()) {
            // Compute element world-space rect

            Vector2 pos = type().position(screen, element.position());
            float w = (element.size().x * type().gridWidthSize());
            float h = (element.size().y * type().gridHeightSize());

            element.setInteractionState(screen, cam, cursor);
            element.draw(screen, cam.x + pos.x, cam.y + pos.y, w, h);
            // Debug draw background with hover/press tint
        }
    }
}
