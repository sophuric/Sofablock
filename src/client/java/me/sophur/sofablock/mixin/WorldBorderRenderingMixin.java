package me.sophur.sofablock.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import me.sophur.sofablock.BorderRendering;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldBorderRendering.class)
public class WorldBorderRenderingMixin {
    @Inject(method = "render", at = @At(value = "HEAD"))
    public void render(WorldBorder border, Vec3d vec3d, double d, double e, CallbackInfo ci) {
        var client = MinecraftClient.getInstance();
        var camera = client.gameRenderer.getCamera();

        // render to world border layer
        var renderLayer = MinecraftClient.isFabulousGraphicsOrBetter() ?
                BorderRendering.BORDER_ALL_MASK :
                BorderRendering.BORDER_COLOR_MASK;

        renderLayer.startDrawing();

        BufferBuilder bufferBuilder = Tessellator.getInstance()
                .begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

        var cameraPos = camera.getPos().toVector3f();
        BorderRendering.buildVertices(bufferBuilder, cameraPos);

        try (var buffer = bufferBuilder.endNullable()) {
            if (buffer != null) {
                renderLayer.draw(buffer);
            }
        }

        renderLayer.endDrawing();

        // restore state
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }
}
