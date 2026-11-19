package fast_reset.client.mixin.completefutures;

import fast_reset.client.completefutures.AsyncSupply;
import fast_reset.client.interfaces.FRThreadExecutor;
import net.minecraft.util.thread.ThreadExecutor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

@Mixin(ThreadExecutor.class)
public abstract class ThreadExecutorMixin implements FRThreadExecutor {
    @Shadow
    @Final
    private Queue<Runnable> tasks;

    @Unique
    private volatile boolean fastClosed;

    @Shadow
    protected abstract void cancelTasks();

    @Redirect(
            method = {
                    "submit(Ljava/util/function/Supplier;)Ljava/util/concurrent/CompletableFuture;",
                    "submitAsync"
            },
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/concurrent/CompletableFuture;supplyAsync(Ljava/util/function/Supplier;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"
            )
    )
    private synchronized <U> CompletableFuture<U> cancelableAsyncSupply(Supplier<U> supplier, Executor executor) {
        return AsyncSupply.supplyAsync(supplier, executor);
    }

    @Inject(
            method = {
                    "submit(Ljava/util/function/Supplier;)Ljava/util/concurrent/CompletableFuture;",
                    "submitAsync",
                    //? if <1.15 {
                    /*"method_20493",
                    *///? } else {
                    "submit(Ljava/lang/Runnable;)Ljava/util/concurrent/CompletableFuture;",
                     //? }
            },
            at = @At("HEAD"),
            cancellable = true
    )
    private synchronized void doNotCompleteTasks(CallbackInfoReturnable<CompletableFuture<?>> cir) {
        if (this.fastClosed) {
            cir.setReturnValue(CompletableFuture.completedFuture(null));
        }
    }

    @Override
    public synchronized void fastReset$cancelFutures() {
        this.fastClosed = true;
        for (Runnable task : this.tasks) {
            if (task instanceof AsyncSupply) {
                ((AsyncSupply<?>) task).cancel();
            }
        }
        this.cancelTasks();
    }
}
