package me.sophur.sofablock;

import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipBackgroundRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.Window;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Position;

import java.util.ArrayList;
import java.util.List;

import static me.sophur.sofablock.SofablockClient.MOD_ID;

public class SofablockHUD implements IdentifiedLayer {
    private SofablockHUD() {

    }

    public final static SofablockHUD INSTANCE = new SofablockHUD();

    @Override
    public Identifier id() {
        return Identifier.of(MOD_ID, "hud");
    }

    public interface TextDisplay {
        List<Pair<Text, List<Text>>> GetTextLines();

        int GetX();

        int GetY();
    }

    private final List<TextDisplay> text = List.of(
        new me.sophur.sofablock.hud.AmountDisplay()
    );

    @Override
    public void render(DrawContext context, RenderTickCounter tickCounter) {
        // only show without a screen or in chat
        if (!SofablockClient.shouldDrawHUD()) return;

        var client = MinecraftClient.getInstance();
        boolean inScreen = client.currentScreen != null;

        int x = 16, y = 16;
        int lineHeight = client.textRenderer.fontHeight;

        Window window = client.getWindow();
        double mouseX = client.mouse.getScaledX(window), mouseY = client.mouse.getScaledY(window);


        List<Text> hoverText = null;

        for (TextDisplay textDisplay : text) {
            int maxWidth = 0;

            var textLines = textDisplay.GetTextLines();
            if (textLines.isEmpty()) continue;

            int ly = y;
            for (Pair<Text, List<Text>> line : textLines) {
                int width = client.textRenderer.getWidth(line.getLeft());
                if (width > maxWidth) maxWidth = width;

                if (inScreen && mouseX >= x && mouseX < x + width && mouseY >= ly && mouseY <= ly + lineHeight)
                    hoverText = line.getRight();
                ly += lineHeight;
            }

            int height = lineHeight * textLines.size();
            TooltipBackgroundRenderer.render(context, x - 1, y - 1, maxWidth, height, 0, null);
            for (Pair<Text, List<Text>> text : textLines) {
                context.drawTextWithShadow(client.textRenderer, text.getLeft(), x, y, 0xffffffff);
                y += lineHeight;
            }
        }

        if (hoverText != null)
            context.drawOrderedTooltip(client.textRenderer, hoverText.stream().map(Text::asOrderedText).toList(), (int) mouseX, (int) mouseY);
    }
}
