package me.sophur.sofablock;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import me.sophur.sofablock.mixin.PlayerListHudMixin;
import me.sophur.sofablock.tracker.*;
import net.azureaaron.hmapi.events.HypixelPacketEvents;
import net.azureaaron.hmapi.network.HypixelNetworking;
import net.azureaaron.hmapi.network.packet.s2c.ErrorS2CPacket;
import net.azureaaron.hmapi.network.packet.s2c.HypixelS2CPacket;
import net.azureaaron.hmapi.network.packet.v1.s2c.LocationUpdateS2CPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

public class SofablockClient implements ClientModInitializer {
    public static final String MOD_ID = "sofablock";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        resetLocation();
        try {
            ItemStorage.load();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        HypixelPacketEvents.LOCATION_UPDATE.register(SofablockClient::handlePacket);
        HypixelNetworking.registerToEvents(Util.make(new Object2IntOpenHashMap<>(), map -> {
            map.put(LocationUpdateS2CPacket.ID, 1);
        }));
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            resetLocation();
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            resetLocation();
        });
        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, Identifier.of(MOD_ID, "hud"), SofablockHud::render);
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            try {
                ItemStorage.save();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
        ClientTickEvents.END_CLIENT_TICK.register(HotmGuiParser::handleTick);
        ClientTickEvents.END_CLIENT_TICK.register(TabParser::handleTick);
        ClientTickEvents.END_CLIENT_TICK.register(SackGuiParser::handleTick);
        ClientTickEvents.END_CLIENT_TICK.register(InventoryItemCounter::handleTick);
        ClientReceiveMessageEvents.ALLOW_GAME.register(SackPickupChatParser::handleGame);
    }
    
    public static Path getModDirectory() throws RuntimeException {
        var directory = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID);
        //noinspection ResultOfMethodCallIgnored
        directory.toFile().mkdirs();
        return directory;
    }

    private static String serverName;
    private static String serverType;
    private static String lobbyName;
    private static String mode;
    private static String map;

    private static void resetLocation() {
        serverName = serverType = lobbyName = mode = map = "";
    }

    private static void handlePacket(HypixelS2CPacket packet) {
        switch (packet) {
            case LocationUpdateS2CPacket(var serverName_, var serverType_, var lobbyName_, var mode_, var map_) -> {
                serverName = serverName_;
                serverType = serverType_.orElse("");
                lobbyName = lobbyName_.orElse("");
                mode = mode_.orElse("");
                map = map_.orElse("");
            }
            case ErrorS2CPacket(var id, var errorReason) -> {
                LOGGER.error("Hypixel packet error {}: {}", id, errorReason);
            }
            default -> {
            }
        }
    }

    public static String getServerName() {
        return serverName;
    }

    public static String getServerType() {
        return serverType;
    }

    public static String getLobbyName() {
        return lobbyName;
    }

    public static String getMode() {
        return mode;
    }

    public static String getMap() {
        return map;
    }

    public static boolean onSkyblock() {
        return serverType.equals("SKYBLOCK");
    }

    public static boolean shouldDrawHUD() {
        var client = MinecraftClient.getInstance();
        if (!client.isFinishedLoading()) return false;
        if (client.world == null) return false;
        if (client.options.hudHidden) return false; // don't show in F1 mode
        if (client.currentScreen != null && !(client.currentScreen instanceof ChatScreen))
            return false; // don't show on other GUIs
        if (client.getDebugHud().shouldShowDebugHud()) return false; // don't show if F3 is open
        if (((PlayerListHudMixin) client.inGameHud.getPlayerListHud()).getVisible())
            return false; // don't show if tab is open
        return true;
    }
}
