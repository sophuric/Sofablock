package me.sophur.sofablock.mixin;

import me.sophur.sofablock.SofablockClient;
import net.minecraft.client.gui.screen.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {
    @Inject(method = "mouseClicked(DDI)Z", at = @At("HEAD"))
    public void injectMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        SofablockClient.mouseClicked.invoke(new SofablockClient.ScreenClickedArgs(mouseX, mouseY, button));
    }

    /*
    @Inject(method = "mouseReleased(DDI)Z", at = @At("HEAD"))
    public void injectMouseReleased(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        SofablockClient.mouseReleased.invoke(new SofablockClient.ScreenClickedArgs(mouseX, mouseY, button));
    }
     */
}
