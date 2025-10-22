package me.sophur.sofablock;

import me.sophur.sofablock.hud.AmountDisplay;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipBackgroundRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.Window;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;

import java.util.List;

public class SofablockHud {
    private SofablockHud() {
    }

    private final static SofablockHud INSTANCE = new SofablockHud();

    public interface TextDisplay {
        List<Pair<Text, List<Text>>> GetTextLines();

        float GetX(float width, float height);

        float GetY(float width, float height);

        float GetScale();
    }

    private static final List<TextDisplay> text = List.of(
        new AmountDisplay()
    );

    public static void render(DrawContext context, RenderTickCounter tickCounter) {
        // only show without a screen or in chat
        if (!SofablockClient.shouldDrawHUD()) return;

        var client = MinecraftClient.getInstance();
        boolean inScreen = client.currentScreen != null;

        int lineHeight = client.textRenderer.fontHeight;

        Window window = client.getWindow();
        double mouseX = client.mouse.getScaledX(window), mouseY = client.mouse.getScaledY(window);

        List<Text> hoverText = null;

        for (TextDisplay textDisplay : text) {
            float scale = textDisplay.GetScale();
            float wWidth = window.getWidth() / scale, wHeight = window.getHeight() / scale;
            float x = textDisplay.GetX(wWidth, wHeight), y = textDisplay.GetY(wWidth, wHeight);

            int maxWidth = 0;

            var textLines = textDisplay.GetTextLines();
            if (textLines == null || textLines.isEmpty()) continue;

            float ly = y;
            for (Pair<Text, List<Text>> line : textLines) {
                int width = client.textRenderer.getWidth(line.getLeft());
                if (width > maxWidth) maxWidth = width;

                if (inScreen && mouseX/scale >= x && mouseX/scale < x + width && mouseY/scale >= ly && mouseY/scale <= ly + lineHeight) {
                    hoverText = line.getRight();
                }
                ly += lineHeight;
            }

            context.getMatrices().pushMatrix();
            context.getMatrices().scale(scale);

            int height = lineHeight * textLines.size();
            TooltipBackgroundRenderer.render(context, (int) x - 1, (int) y - 1, maxWidth, height, null);
            for (Pair<Text, List<Text>> text : textLines) {
                context.drawTextWithShadow(client.textRenderer, text.getLeft(), (int) x, (int) y, 0xffffffff);
                y += lineHeight;
            }

            context.getMatrices().popMatrix();
        }

        if (hoverText != null) {
            context.drawOrderedTooltip(client.textRenderer, hoverText.stream().map(Text::asOrderedText).toList(), (int) mouseX, (int) mouseY);
        }
    }
}
