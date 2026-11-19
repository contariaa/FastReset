package fast_reset.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import fast_reset.client.FastReset;
import fast_reset.client.interfaces.FRThreadExecutor;
import net.minecraft.server.world.ServerChunkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerChunkManager.class)
public abstract class ServerChunkManagerMixin {
    @Unique
    private FRThreadExecutor fastResetThreadExecutor;

    @ModifyExpressionValue(
            method = "<init>",
            at = @At(
                    value = "NEW",
                    //? if <1.17 {
                    target = "(Lnet/minecraft/server/world/ServerChunkManager;Lnet/minecraft/world/World;Lnet/minecraft/server/world/ServerChunkManager$1;)Lnet/minecraft/server/world/ServerChunkManager$MainThreadExecutor;"
                    //? } else {
                    /*target = "(Lnet/minecraft/server/world/ServerChunkManager;Lnet/minecraft/world/World;)Lnet/minecraft/server/world/ServerChunkManager$MainThreadExecutor;"
                    *///? }
            )
    )
    private @Coerce Object setFastResetThreadExecutor(@Coerce Object mainThreadExecutor) {
        return this.fastResetThreadExecutor = (FRThreadExecutor) mainThreadExecutor;
    }

    @WrapWithCondition(
            method = "close",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerChunkManager;save(Z)V"
            )
    )
    private boolean skipSaving(ServerChunkManager chunkManager, boolean flush) {
        return !FastReset.shouldFastClose();
    }

    @Inject(
            method = "close",
            at = @At("RETURN")
    )
    private void cancelRemainingTasks(CallbackInfo ci) {
        if (FastReset.shouldFastClose()) {
            this.fastResetThreadExecutor.fastReset$cancelFutures();
        }
    }
}
