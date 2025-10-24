package me.sophur.sofablock;

import me.sophur.sofablock.hud.AmountDisplay;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipBackgroundRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.Window;
import net.minecraft.text.Text;
import org.joml.Vector2d;
import org.joml.Vector2dc;
import org.lwjgl.glfw.GLFWMouseButtonCallback;

import java.util.List;
import java.util.function.Consumer;

public class SofablockHud {
    private SofablockHud() {
    }

    public record TextLine(Text text, List<Text> hoverText, Consumer<Vector2d> click) {
        // TODO: rework this so it can display items, and text in tables and custom layouts, instead of just a list
    }

    public interface TextDisplay {
        List<TextLine> GetTextLines();
    }

    private static final List<TextDisplay> text = List.of(
        new AmountDisplay()
    );

    private static boolean clicked = false;

    static {
        SofablockClient.mouseClicked.add(a -> {
            if (!SofablockClient.shouldDrawHUD()) return;
            if (a.button() == 0) clicked = true;
        });
    }

    public static void render(DrawContext context, RenderTickCounter tickCounter) {
        // only show without a screen or in chat
        if (!SofablockClient.shouldDrawHUD()) {
            clicked = false;
            return;
        }

        var client = MinecraftClient.getInstance();
        boolean inScreen = client.currentScreen != null;

        int lineHeight = client.textRenderer.fontHeight;

        Window window = client.getWindow();
        Vector2dc mouse = new Vector2d(client.mouse.getScaledX(window), client.mouse.getScaledY(window));

        TextLine hoverLine = null;
        Vector2dc hoverLinePos = null;

        for (TextDisplay textDisplay : text) {
            float scale = 1;
            // TODO: custom HUD positioning
            // float wWidth = window.getWidth() / scale, wHeight = window.getHeight() / scale;
            Vector2d pos = new Vector2d(16, 16);

            int maxWidth = 0;

            var textLines = textDisplay.GetTextLines();
            if (textLines == null || textLines.isEmpty()) continue;

            Vector2d linePos = new Vector2d(pos);
            for (TextLine line : textLines) {
                int width = client.textRenderer.getWidth(line.text);
                if (width > maxWidth) maxWidth = width;

                if (inScreen && mouse.x() / scale >= linePos.x && mouse.x() / scale < linePos.x + width && mouse.y() / scale >= linePos.y && mouse.y() / scale <= linePos.y + lineHeight) {
                    hoverLine = line;
                    hoverLinePos = linePos;
                }
                linePos.y += lineHeight;
            }

            context.getMatrices().pushMatrix();
            context.getMatrices().scale(scale);

            int height = lineHeight * textLines.size();
            TooltipBackgroundRenderer.render(context, (int) pos.x() - 1, (int) pos.y() - 1, maxWidth, height, null);
            for (TextLine line : textLines) {
                context.drawTextWithShadow(client.textRenderer, line.text, (int) pos.x(), (int) pos.y(), 0xffffffff);
                pos.y += lineHeight;
            }

            context.getMatrices().popMatrix();
        }

        if (hoverLine != null) {
            context.drawOrderedTooltip(client.textRenderer, hoverLine.hoverText
                .stream().map(Text::asOrderedText).toList(), (int) mouse.x(), (int) mouse.y());

            Vector2d relativePos = mouse.sub(hoverLinePos, new Vector2d());

            if (clicked) {
                hoverLine.click.accept(relativePos);
            }
        }

        clicked = false;
    }
}
