package com.sprite.render.ui.inventory;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.sprite.input.InputAction;
import com.sprite.input.InputSystem;
import com.sprite.input.VirtualCursor;
import com.sprite.render.screen.GameScreen;
import com.sprite.render.ui.UI;
import com.sprite.resource.ui.DialogUI;
import com.sprite.resource.ui.dialog.Button;
import com.sprite.resource.ui.dialog.DialogElement;

public class Dialog extends UI<DialogUI> {

    public Dialog(DialogUI type) {
        super(type);
    }

    @Override
    public void draw(GameScreen screen) {
        screen.shape().setColor(Color.YELLOW);
        type().draw(screen);
        VirtualCursor cursor = InputSystem.i().cursor();
        cursor.draw(screen);
        float camX = (screen.camera().position.x - screen.camera().viewportWidth/2);
        float camY = (screen.camera().position.y - screen.camera().viewportHeight/2);
//        screen.sprite().end();
//        screen.shape().setProjectionMatrix(screen.camera().combined);
//        screen.shape().begin(ShapeRenderer.ShapeType.Line);
//        for (int x = 0; x < type().columns(); x++) {
//            for (int y = 0; y < type().rows(); y++) {
//                int index = y * type().columns() + x;
//                Vector2 pos = type().position(screen, index);
//
//                screen.shape().rect(pos.x, pos.y, type().gridWidthSize(), type().gridHeightSize());
//            }
//        }
//        screen.shape().end();
//        screen.sprite().begin();

        for (DialogElement element : type().elements().values()) {
            // Compute element world-space rect
            Vector2 pos = type().position(screen, element.position());
            float w = (element.size().x * type().gridWidthSize());
            float h = (element.size().y * type().gridHeightSize());

            // Hit-test using world-space cursor (UI is drawn in world space relative to camera)
            Vector3 world = InputSystem.i().worldCursor(screen.camera());
            float worldPosX = camX + pos.x;
            float worldPosY = camY + pos.y;
            boolean hovered = world.x >= worldPosX && world.x <= worldPosX + w && world.y >= worldPosY && world.y <= worldPosY + h;
            boolean pressed = hovered && InputSystem.i().isActionPressed(InputAction.Primary);
            boolean justClicked = hovered && InputSystem.i().isActionJustPressed(InputAction.Primary);
            element.setInteractionState(hovered, pressed, justClicked);
            element.draw(screen, camX + pos.x, camY + pos.y, w, h);
            // Debug draw background with hover/press tint
            if (element instanceof Button button) {

                String text = button.title();
                GlyphLayout layout = new GlyphLayout(screen.font(), text);
                float centerX = pos.x + w / 2f;
                float centerY = pos.y + h / 2f;
                // draw so that the text is centered on (centerX, centerY)
                screen.font().draw(screen.sprite(), text, camX+centerX - layout.width / 2f, camY+centerY + layout.height / 2f);
            }
        }

    }
}
