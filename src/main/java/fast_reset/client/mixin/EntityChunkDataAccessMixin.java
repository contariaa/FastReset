//? if >=1.17 <=1.20.6 {
/*package fast_reset.client.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import fast_reset.client.FastReset;
import net.minecraft.world.storage.EntityChunkDataAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;

@Mixin(EntityChunkDataAccess.class)
public abstract class EntityChunkDataAccessMixin {

    @WrapWithCondition(
            method = "method_31730",
            at = @At(
                    value = "INVOKE",
                    //? if <1.18.2 {
                    target = "Lorg/apache/logging/log4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V"
                    //? } else {
                    /^target = "Lorg/slf4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V"
                    ^///? }
            )
    )
    private static boolean suppressErrorsWhenFastClosed(@Coerce Object logger, String s, Object o, Object object) {
        return !FastReset.shouldFastClose();
    }
}
*///? }
