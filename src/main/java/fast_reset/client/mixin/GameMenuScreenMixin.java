package fast_reset.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import fast_reset.client.FastReset;
import fast_reset.client.FastResetConfig;
import fast_reset.client.interfaces.FRMinecraftServer;
import me.contaria.speedrunapi.util.TextUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen {

    protected GameMenuScreenMixin(Text title) {
        super(title);
    }

    @ModifyExpressionValue(
            method = "initWidgets",
            at = @At(
                    value = "NEW",
                    target = "(IIIILnet/minecraft/text/Text;Lnet/minecraft/client/gui/widget/ButtonWidget$PressAction;)Lnet/minecraft/client/gui/widget/ButtonWidget;"
            ),
            slice = @Slice(
                    from = @At(
                            value = "CONSTANT",
                            args = "stringValue=menu.returnToMenu"
                    )
            )
    )
    private ButtonWidget createFastResetButton(ButtonWidget saveButton) {
        if (!MinecraftClient.getInstance().isInSingleplayer() || !this.shouldFastReset()) {
            return saveButton;
        }

        Text menuQuitWorld = TextUtil.translatable("fast_reset.menu.quitWorld");
        int height = 20;
        int width;
        int x;
        int y;
        switch (FastReset.config.buttonLocation) {
            case CENTER:
                width = saveButton.getWidth();
                x = saveButton.x;
                y = saveButton.y + 24;
                break;
            case BOTTOM_RIGHT:
            default:
                width = this.textRenderer.getWidth(menuQuitWorld) + 30;
                x = this.width - width - 4;
                y = this.height - height - 4;
        }

        ClickableWidget fastResetButton = this.addDrawableChild(new ButtonWidget(x, y, width, height, menuQuitWorld, button -> {
            if (MinecraftClient.getInstance().getServer() != null) {
                ((FRMinecraftServer) MinecraftClient.getInstance().getServer()).fastReset$fastReset();
            }
            saveButton.onPress();
        }));

        fastResetButton.visible = FastReset.config.buttonLocation != FastResetConfig.ButtonLocation.HIDE;

        return saveButton;
    }

    @Unique
    private boolean shouldFastReset() {
        if (FastReset.config.alwaysSaveAfter == 0) {
            return true;
        }
        return MinecraftClient.getInstance().getServer() != null && MinecraftClient.getInstance().getServer().getTicks() <= FastReset.config.alwaysSaveAfter * 20;
    }
}