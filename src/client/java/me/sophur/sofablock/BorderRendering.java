package me.sophur.sofablock;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.util.TriState;
import net.minecraft.util.Util;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector3f;

import static net.minecraft.client.render.RenderPhase.*;

public class BorderRendering {
    public static final RenderLayer
            BORDER_ALL_MASK = getLayer(true),
            BORDER_COLOR_MASK = getLayer(false);

    private static RenderLayer getLayer(boolean mask) {
        return RenderLayer.of(
                "skyblock_border",
                VertexFormats.POSITION_TEXTURE,
                VertexFormat.DrawMode.QUADS,
                1536,
                false,
                false,
                RenderLayer.MultiPhaseParameters.builder()
                        .program(POSITION_TEXTURE_COLOR_PROGRAM)
                        .texture(new RenderPhase.Texture(WorldBorderRendering.FORCEFIELD, TriState.FALSE, true))
                        .transparency(OVERLAY_TRANSPARENCY)
                        .lightmap(ENABLE_LIGHTMAP)
                        .target(WEATHER_TARGET)
                        .writeMaskState(mask ? ALL_MASK : COLOR_MASK)
                        .layering(WORLD_BORDER_LAYERING)
                        .cull(DISABLE_CULLING)
                        .build(false)
        );
    }

    public static void buildVertices(BufferBuilder bufferBuilder, Vector3f cameraPos) {
        var config = Config.HANDLER.instance();
        if (config.crystalHollowsEnabled && SofablockClient.inCrystalHollows()) {
            buildRectangle(bufferBuilder, cameraPos, 512.5f, -500f, 562.0f, 512.5f, 500f, 924.0f);
            buildRectangle(bufferBuilder, cameraPos, 560.0f, -500f, 513.0f, 824.0f, 500f, 513.0f);
            buildRectangle(bufferBuilder, cameraPos, 513.0f, -500f, 460.0f, 513.0f, 500f, 202.0f);
            buildRectangle(bufferBuilder, cameraPos, 463.0f, -500f, 513.0f, 202.0f, 500f, 513.0f);
        }
    }

    private static void buildRectangle(BufferBuilder bufferBuilder, Vector3f cameraPos, float x1, float y1, float z1, float x2, float y2, float z2) {
        buildRectangle(bufferBuilder, cameraPos, new Vector3f(x1, y1, z1), new Vector3f(x2, y2, z2));
    }

    // Record to help with looping for creating planes
    private record AxisLoop(int axis1, int axis2, int axisFixed) {
        public Vector3f getVector(Vector3f vec1, Vector3f vec2, Vector3f vecFixed) {
            return getVector(vec1.get(axis1), vec2.get(axis2), vecFixed.get(axisFixed));
        }

        public Vector3f getVector(float coord1, float coord2, float coordFixed) {
            var vec = new Vector3f();
            vec.setComponent(axis1, coord1);
            vec.setComponent(axis2, coord2);
            vec.setComponent(axisFixed, coordFixed);
            return vec;
        }

        ;

        public AxisLoop {
            if (axis1 == axis2) throw new IllegalArgumentException("Axis 1 cannot be the same as axis 2");
            if (axis1 == axisFixed) throw new IllegalArgumentException("Axis 1 cannot be same as the fixed axis");
            if (axis2 == axisFixed) throw new IllegalArgumentException("Axis 2 cannot be same as the fixed axis");
        }
    }

    @FunctionalInterface
    private interface VectorComponentFunction {
        float func(float value, int component);
    }

    private static void buildRectangle(BufferBuilder bufferBuilder, Vector3f cameraPos, Vector3f v1, Vector3f v2) {
        var config = Config.HANDLER.instance();

        var viewDistance = MinecraftClient.getInstance().options.getClampedViewDistance() * 16;

        var loops = new AxisLoop[]{
                // fixed axis = axis that is perpendicular to the plane
                new AxisLoop(0, 2, 1), // X/Z floors/ceilings
                new AxisLoop(0, 1, 2), // X/Y walls
                new AxisLoop(2, 1, 0) // Z/Y walls
        };
        if (v1.x > v2.x) {
            var temp = v1.x;
            v1.x = v2.x;
            v2.x = temp;
        }
        if (v1.y > v2.y) {
            var temp = v1.y;
            v1.y = v2.y;
            v2.y = temp;
        }
        if (v1.z > v2.z) {
            var temp = v1.z;
            v1.z = v2.z;
            v2.z = temp;
        }

        final var multiple = 1;

        // subtract camera pos
        v1.sub(cameraPos);
        v2.sub(cameraPos);

        // clamped values so we don't render outside of render distance
        var v1clamp = new Vector3f(
                MathHelper.clamp(v1.x, -viewDistance, viewDistance), v1.y,
                MathHelper.clamp(v1.z, -viewDistance, viewDistance)
        );
        var v2clamp = new Vector3f(
                MathHelper.clamp(v2.x, -viewDistance, viewDistance), v2.y,
                MathHelper.clamp(v2.z, -viewDistance, viewDistance)
        );

        float cycle = (float) (Util.getMeasuringTimeMs() % 3000L) / 3000.0F;

        for (AxisLoop loop : loops) {
            // check that the plane is close enough on the axis perpendicular to the plane
            var distance = 0f;
            // euclidean distance = sqrt(x*x+y*y+z*z)
            for (int axis = 0; axis < 3; ++axis) {
                var val1 = v1.get(axis);
                var val2 = v2.get(axis);
                // skip this axis if we are between the bounds
                if (val1 < 0 && val2 > 0) continue;
                // the point considered for distance calculation is the endpoint of the wall
                var dist = Math.min(Math.abs(val1), Math.abs(val2));
                dist *= dist;
                distance += dist;
            }
            distance = MathHelper.sqrt(distance);
            if (distance > config.fadeEnd) continue;
            // calculate opacity value required
            float opacity = 1;
            if (distance > config.fadeStart)
                opacity = 1 - (distance - config.fadeStart) / (config.fadeEnd - config.fadeStart);
            var color = ColorHelper.withAlpha((int) (config.color.getAlpha() * opacity), config.color.getRGB());
            addVertex(bufferBuilder, loop, v1clamp, v1clamp, v1, cycle, cycle, cameraPos).color(color);
            addVertex(bufferBuilder, loop, v2clamp, v1clamp, v1, cycle, cycle, cameraPos).color(color);
            addVertex(bufferBuilder, loop, v2clamp, v2clamp, v1, cycle, cycle, cameraPos).color(color);
            addVertex(bufferBuilder, loop, v1clamp, v2clamp, v1, cycle, cycle, cameraPos).color(color);
        }
    }

    private static VertexConsumer addVertex(BufferBuilder bufferBuilder, AxisLoop loop, Vector3f vec1, Vector3f vec2, Vector3f vecFixed, float uv1, float uv2, Vector3f camera) {
        return bufferBuilder.vertex(loop.getVector(vec1, vec2, vecFixed))
                .texture(
                        uv1 - (vec1.get(loop.axis1) + camera.get(loop.axis1)) * 0.5f,
                        uv2 - (vec2.get(loop.axis2) + camera.get(loop.axis2)) * 0.5f
                );
    }

    private BorderRendering() {
    }
}
