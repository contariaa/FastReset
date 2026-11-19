package fast_reset.client.mixin.completefutures;

import fast_reset.client.completefutures.AsyncSupply;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

//? if <1.21 {
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
//? } else {
/*import net.minecraft.server.world.ServerChunkLoadingManager;
*///? }

//? if <1.19  {
import java.util.function.Supplier;
//? } else {
/*import java.util.function.Function;
*///? }

//? if <1.21 {
@Mixin(ThreadedAnvilChunkStorage.class)
 //? } else {
/*@Mixin(ServerChunkLoadingManager.class)
*///? }
public abstract class ThreadedAnvilChunkStorageMixin {

    //? if <1.19 {
    @Redirect(
            //? if <1.16 {
            /*method = "method_20619",
            *///? } else {
            method = "loadChunk",
            //? }
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/concurrent/CompletableFuture;supplyAsync(Ljava/util/function/Supplier;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"
            )
    )
    private <T> CompletableFuture<T> redirectCompletableFuture(Supplier<T> supplier, Executor executor) {
        return AsyncSupply.supplyAsync(supplier, executor);
    }
    //? } else {
    /*@Redirect(
            method = "loadChunk",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/concurrent/CompletableFuture;thenApplyAsync(Ljava/util/function/Function;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"
            )
    )
    private <T, U> CompletableFuture<U> redirectCompletableFuture(CompletableFuture<T> from, Function<T, U> function, Executor executor) {
        return AsyncSupply.thenApplyAsync(from, function, executor);
    }

    @Redirect(
            method = "loadChunk",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/concurrent/CompletableFuture;exceptionallyAsync(Ljava/util/function/Function;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"
            )
    )
    private <T> CompletableFuture<T> redirectCompletableFuture2(CompletableFuture<T> from, Function<Throwable, T> function, Executor executor) {
        return AsyncSupply.exceptionallyAsync(from, function, executor);
    }
    *///? }
}
