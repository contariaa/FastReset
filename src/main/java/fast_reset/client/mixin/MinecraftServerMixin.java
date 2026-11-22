package fast_reset.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import fast_reset.client.FastReset;
import fast_reset.client.interfaces.FRMinecraftServer;
import fast_reset.client.interfaces.FRThreadExecutor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if >=1.18.2 {
/*import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
*///? }

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements FRMinecraftServer {
    @Shadow
    private volatile boolean running;
    @Shadow
    private volatile boolean loading;

    @Unique
    private volatile boolean fastReset;

    @ModifyReturnValue(
            method = "shouldKeepTicking",
            at = @At("RETURN")
    )
    private boolean stopTicking(boolean shouldKeepTicking) {
        return shouldKeepTicking && this.shouldTick();
    }

    @Inject(
            method = "shutdown",
            at = @At("HEAD")
    )
    private void enableFastClose(CallbackInfo ci) {
        if (!this.fastReset$shouldSave()) {
            FastReset.enableFastClose();
        }
    }

    @WrapWithCondition(
            method = "shutdown",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/PlayerManager;saveAllPlayerData()V"
            )
    )
    private boolean disablePlayerSaving(PlayerManager playerManager) {
        return this.fastReset$shouldSave();
    }

    //? if >=1.18.2 {
    /*@ModifyExpressionValue(
            method = "shutdown",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/stream/Stream;anyMatch(Ljava/util/function/Predicate;)Z"
            )
    )
    private boolean doNotDelayShutdown(boolean shouldDelayShutdown) {
        return shouldDelayShutdown && !this.fastReset;
    }
    *///? }

    @WrapOperation(
            method = "shutdown",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/MinecraftServer;save(ZZZ)Z"
            )
    )
    private boolean disableSaving(MinecraftServer server, boolean bl, boolean bl2, boolean bl3, Operation<Boolean> original) {
        if (this.fastReset$shouldSave()) {
            return original.call(server, bl, bl2, bl3);
        }
        return false;
    }

    @Inject(
            method = "shutdown",
            at = @At("TAIL")
    )
    private void cancelRemainingTasks(CallbackInfo ci) {
        if (!this.fastReset$shouldSave()) {
            ((FRThreadExecutor) this).fastReset$cancelFutures();
        }
    }

    @Unique
    private boolean shouldTick() {
        return !(this.fastReset && !this.running && this.loading);
    }

    @Override
    public void fastReset$fastReset() {
        this.fastReset = true;
    }

    // MinecraftServer#loading actually means the complete opposite, more like "finishedLoading"
    // we check it to skip saving on WorldPreview resets
    @Override
    public boolean fastReset$shouldSave() {
        return !this.fastReset && this.loading;
    }
}