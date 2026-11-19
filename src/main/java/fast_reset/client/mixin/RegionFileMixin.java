//? if >=1.15 {
package fast_reset.client.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import fast_reset.client.FastReset;
import net.minecraft.world.storage.RegionFile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

//? if >=1.15.1 {
import java.nio.channels.FileChannel;
 //? }

@Mixin(RegionFile.class)
public abstract class RegionFileMixin {

    @WrapWithCondition(
            method = "close",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/storage/RegionFile;fillLastSector()V"
            )
    )
    private boolean skipFillingLastSector(RegionFile file) {
        return !FastReset.shouldFastClose();
    }

    //? if >=1.15.1 {
    @WrapWithCondition(
            method = "close",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/nio/channels/FileChannel;force(Z)V"
            )
    )
    private boolean skipForcingChannel(FileChannel channel, boolean metaData) {
        return !FastReset.shouldFastClose();
    }
    //? }
}
//? }
