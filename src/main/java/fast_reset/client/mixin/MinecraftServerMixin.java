package fast_reset.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import fast_reset.client.FastReset;
import fast_reset.client.interfaces.FRMinecraftServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.ServerTask;
import net.minecraft.util.thread.ReentrantThreadExecutor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin extends ReentrantThreadExecutor<ServerTask> implements FRMinecraftServer {
    @Shadow
    private volatile boolean loading;

    @Unique
    private volatile boolean fastReset;

    public MinecraftServerMixin(String string) {
        super(string);
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

    @ModifyExpressionValue(
            method = "shutdown",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/stream/Stream;anyMatch(Ljava/util/function/Predicate;)Z"
            )
    )
    private boolean disableShutdownDelay(boolean anyMatch) {
        return anyMatch && this.fastReset$shouldSave();
    }

    @WrapWithCondition(
            method = "shutdown",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/MinecraftServer;save(ZZZ)Z"
            )
    )
    private boolean disableSaving(MinecraftServer server, boolean bl, boolean bl2, boolean bl3) {
        return this.fastReset$shouldSave();
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