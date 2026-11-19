package fast_reset.client.mixin.completefutures;

import fast_reset.client.FastReset;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.thread.MessageListener;
import net.minecraft.util.thread.TaskExecutor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

//? if >=1.16 {
import com.mojang.datafixers.util.Either;
//? }

@Mixin(TaskExecutor.class)
public abstract class TaskExecutorMixin<T> implements MessageListener<T> {
    @Unique
    private final boolean shouldRememberFutures = !MinecraftClient.getInstance().isOnThread();
    @Unique
    private Set<CompletableFuture<?>> futures;

    @Inject(
            method = "<init>",
            at = @At("TAIL")
    )
    private void initializeFuturesCapture(CallbackInfo ci) {
        if (this.shouldRememberFutures) {
            this.futures = Collections.synchronizedSet(new HashSet<>());
        }
    }

    @Inject(
            method = "close",
            at = @At("HEAD")
    )
    private synchronized void cancelFutures(CallbackInfo ci) {
        if (!this.shouldRememberFutures) {
            return;
        }
        if (this.futures == null) {
            FastReset.LOGGER.warn("FastReset | Could not cancel futures because future set is null!");
            return;
        }
        if (!FastReset.shouldFastClose()) {
            //FastReset.LOGGER.info("FastReset | Not fast-closing Task Executor: {}", this.name);
            this.futures.clear();
            this.futures = null;
            return;
        }
        for (CompletableFuture<?> future : new HashSet<>(this.futures)) {
            future.complete(null);
        }
        if (!this.futures.isEmpty()) {
            FastReset.LOGGER.warn("FastReset | Completed all futures but future set is still not empty!");
        }
        this.futures = null;
    }

    @Unique
    private synchronized <Source> CompletableFuture<Source> saveFuture(CompletableFuture<Source> future) {
        if (this.shouldRememberFutures && this.futures == null) {
            //FastReset.LOGGER.warn("FastReset | Submitted a future but future set has already been cleared!");
            future.complete(null);
            return future;
        }
        this.futures.add(future);
        future.whenComplete((r, t) -> this.removeFuture(future));
        return future;
    }

    @Unique
    private synchronized void removeFuture(CompletableFuture<?> future) {
        this.futures.remove(future);
    }

    @Override
    public <Source> CompletableFuture<Source> ask(Function<? super MessageListener<Source>, ? extends T> messageProvider) {
        return this.saveFuture(MessageListener.super.ask(messageProvider));
    }

    //? if >=1.16 {
    @Override
    //? if <1.17 {
    public <Source> CompletableFuture<Source> method_27918(Function<? super MessageListener<Either<Source, Exception>>, ? extends T> function) {
        return this.saveFuture(MessageListener.super.method_27918(function));
    }
    //? } else {
    /*public <Source> CompletableFuture<Source> askFallible(Function<? super MessageListener<Either<Source, Exception>>, ? extends T> function) {
        return this.saveFuture(MessageListener.super.askFallible(function));
    }
    *///? }
    //? }
}
