package me.sophur.sofablock.mixin;

import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(PlayerListHud.class)
public interface PlayerListHudMixin {
    @Accessor
    boolean getVisible();

    @Invoker
    List<PlayerListEntry> invokeCollectPlayerEntries();
}
