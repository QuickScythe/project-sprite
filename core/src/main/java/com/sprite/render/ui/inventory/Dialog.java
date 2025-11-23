package com.sprite.render.ui.inventory;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.sprite.data.TranslatableString;
import com.sprite.data.utils.Texts;
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
        screen.sprite().end();

        for (DialogElement element : type().buttons().values()) {
            screen.shape().setProjectionMatrix(screen.camera().combined);
            screen.shape().begin(ShapeRenderer.ShapeType.Filled);
            Vector2 pos = type().position(screen, element.position());
            screen.shape().setColor(Color.RED);
            screen.shape().rect(pos.x, pos.y, (element.size().x * type().gridWidthSize()), (element.size().y * type().gridHeightSize()));
            screen.shape().end();
            if (element instanceof Button button) {
                screen.sprite().begin();
                String text = Texts.get(new TranslatableString(button.title()));
                GlyphLayout layout = new GlyphLayout(screen.font(), text);
                float centerX = pos.x + element.size().x * (type().gridWidthSize() / 2f);
                float centerY = pos.y + element.size().y * (type().gridHeightSize() / 2f);
                // draw so that the text is centered on (centerX, centerY)
                screen.font().draw(screen.sprite(), text, centerX - layout.width / 2f, centerY + layout.height / 2f);
                screen.sprite().end();
            }

        }

        screen.sprite().begin();
        screen.sprite().setProjectionMatrix(screen.camera().combined);


    }
}
