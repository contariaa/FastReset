//? if <1.21 {
package fast_reset.client.mixin.completefutures;

import net.minecraft.server.world.ChunkHolder;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.BiFunction;

//? if <1.20.5 {
import com.mojang.datafixers.util.Either;
 //? } else {
/*import net.minecraft.server.world.OptionalChunk;
*///? }

@Mixin(ChunkHolder.class)
public abstract class ChunkHolderMixin {

    @ModifyArg(
            //? if <1.16.2 {
            method = "updateFuture",
            //? } elif <1.18 {
            /*method = "combineSavingFuture",
            *///? } else {
            /*method = "combineSavingFuture(Ljava/util/concurrent/CompletableFuture;Ljava/lang/String;)V",
            *///? }
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/concurrent/CompletableFuture;thenCombine(Ljava/util/concurrent/CompletionStage;Ljava/util/function/BiFunction;)Ljava/util/concurrent/CompletableFuture;"
            ),
            index = 1
    )
    private BiFunction<?, ?, ?> handleCancelledFuture(BiFunction<?, ?, ?> fn) {
        //? if <1.20.5 {
        return (BiFunction<Chunk, ? extends Either<? extends Chunk, ChunkHolder.Unloaded>, Chunk>) (chunk, either) -> either != null ? either.map(chunkx -> chunkx, unloaded -> chunk) : chunk;
         //? } else {
        /*return (BiFunction<Chunk, ? extends OptionalChunk<? extends Chunk>, Chunk>) (chunk, otherChunk) -> otherChunk != null ? OptionalChunk.orElse(otherChunk, chunk) : chunk;
        *///? }
    }
}
//? }
