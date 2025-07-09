package me.sophur.sofablock;

import me.sophur.sofablock.mixin.PlayerListHudMixin;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.tooltip.TooltipBackgroundRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.Window;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

import static me.sophur.sofablock.SofablockClient.MOD_ID;

public class PowderDisplay implements IdentifiedLayer {
    private PowderDisplay() {

    }

    public final static PowderDisplay INSTANCE = new PowderDisplay();

    @Override
    public Identifier id() {
        return Identifier.of(MOD_ID, "powder_display");
    }

    @Override
    public void render(DrawContext context, RenderTickCounter tickCounter) {
        var client = MinecraftClient.getInstance();

        // only show without a screen or in chat
        boolean inScreen = client.currentScreen != null;
        if (!(client.currentScreen instanceof ChatScreen) && inScreen) return;
        if (client.getDebugHud().shouldShowDebugHud()) return; // don't show if F3 is open
        if (((PlayerListHudMixin) client.inGameHud.getPlayerListHud()).getVisible())
            return; // don't show if tab is open

        int x = 16, y = 16;
        int lineHeight = client.textRenderer.fontHeight;

        Window window = client.getWindow();
        double mouseX = client.mouse.getScaledX(window), mouseY = client.mouse.getScaledY(window);

        int maxWidth = 0;
        PowderType hover = null;
        ArrayList<Text> textLines = new ArrayList<>();
        int ly = y;
        for (PowderType powder : PowderType.values()) {
            Text text = powder.getText();
            if (text == null) continue;
            textLines.add(text);

            int width = client.textRenderer.getWidth(text);
            if (width > maxWidth) maxWidth = width;

            if (inScreen && mouseX >= x && mouseX < x + width && mouseY >= ly && mouseY <= ly + lineHeight)
                hover = powder;

            ly += lineHeight;
        }

        var textLineCount = textLines.size();
        if (textLineCount == 0) return;

        int height = lineHeight * textLineCount;
        TooltipBackgroundRenderer.render(context, x - 1, y - 1, maxWidth, height, 0, null);
        for (Text text : textLines) {
            context.drawTextWithShadow(client.textRenderer, text, x, y, 0xffffffff);
            y += lineHeight;
        }

        if (hover != null) {
            List<Text> hoverText = hover.getHoverText();
            if (hoverText != null)
                context.drawOrderedTooltip(client.textRenderer, hoverText.stream().map(Text::asOrderedText).toList(), (int) mouseX, (int) mouseY);
        }
    }
}
