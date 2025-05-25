package me.sophur.sofablock;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.azureaaron.hmapi.events.HypixelPacketEvents;
import net.azureaaron.hmapi.network.HypixelNetworking;
import net.azureaaron.hmapi.network.packet.s2c.ErrorS2CPacket;
import net.azureaaron.hmapi.network.packet.s2c.HypixelS2CPacket;
import net.azureaaron.hmapi.network.packet.v1.s2c.LocationUpdateS2CPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SofablockClient implements ClientModInitializer {
    public static final String MOD_ID = "sofablock";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        if (!Config.HANDLER.load()) throw new RuntimeException("Failed to load config");

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

    public static boolean inCrystalHollows() {
        return onSkyblock() && mode.equals("crystal_hollows");
    }
}
