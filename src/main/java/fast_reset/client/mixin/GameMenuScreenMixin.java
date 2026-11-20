package fast_reset.client.mixin;

import fast_reset.client.FastReset;
import fast_reset.client.FastResetConfig;
import fast_reset.client.interfaces.FRMinecraftServer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

//? if <1.16 {
/*import net.minecraft.client.resource.language.I18n;
*///? } else {
import me.contaria.speedrunapi.util.TextUtil;
//? }

//? if <1.19.3 {
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Slice;
//? } else {
/*import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
*///? }

@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen {
    //? if >=1.19.3 {
    /*@Shadow
    private ButtonWidget exitButton;
    *///? }

    protected GameMenuScreenMixin(Text title) {
        super(title);
    }

    //? if <1.19.3 {
    @ModifyExpressionValue(
            method = "initWidgets",
            at = @At(
                    value = "INVOKE",
                    //? if <1.16.5 {
                    target = "Lnet/minecraft/client/gui/screen/GameMenuScreen;addButton(Lnet/minecraft/client/gui/widget/AbstractButtonWidget;)Lnet/minecraft/client/gui/widget/AbstractButtonWidget;"
                    //? } elif <1.17 {
                    /*target = "Lnet/minecraft/client/gui/screen/GameMenuScreen;addButton(Lnet/minecraft/client/gui/widget/ClickableWidget;)Lnet/minecraft/client/gui/widget/ClickableWidget;"
                    *///? } else {
                    /*target = "Lnet/minecraft/client/gui/screen/GameMenuScreen;addDrawableChild(Lnet/minecraft/client/gui/Element;)Lnet/minecraft/client/gui/Element;"
                    *///? }
            ),
            slice = @Slice(
                    from = @At(
                            value = "CONSTANT",
                            args = "stringValue=menu.returnToMenu"
                    )
            )
    )
    private @Coerce Object injectFastResetButton(@Coerce Object element) {
        this.createFastResetButton((ButtonWidget) element);
        return element;
    }
    //? } else {
    /*@Inject(
            method = "initWidgets",
            at = @At("TAIL")
    )
    private void injectFastResetButton(CallbackInfo ci) {
        this.createFastResetButton(this.exitButton);
    }
    *///? }

    @Unique
    private void createFastResetButton(ButtonWidget saveButton) {
        if (!MinecraftClient.getInstance().isInSingleplayer() || !this.shouldFastReset()) {
            return;
        }

        //? if <1.16 {
        /*String menuQuitWorld = I18n.translate("fast_reset.menu.quitWorld");
        *///? } else {
        Text menuQuitWorld = TextUtil.translatable("fast_reset.menu.quitWorld");
        //? }
        int height = 20;
        int width;
        int x;
        int y;
        switch (FastReset.config.buttonLocation) {
            case CENTER:
                width = saveButton.getWidth();
                //? if <1.19.3 {
                x = saveButton.x;
                y = saveButton.y + 24;
                //? } else {
                /*x = saveButton.getX();
                y = saveButton.getY() + 24;
                *///? }
                break;
            case BOTTOM_RIGHT:
            default:
                //? if <1.16 {
                /*width = MinecraftClient.getInstance().textRenderer.getStringWidth(menuQuitWorld) + 30;
                *///? } else {
                width = this.textRenderer.getWidth(menuQuitWorld) + 30;
                 //? }
                x = this.width - width - 4;
                y = this.height - height - 4;
        }

        //? if <1.19.3 {
        ButtonWidget fastResetButton = new ButtonWidget(x, y, width, height, menuQuitWorld, button -> this.fastReset(saveButton));
         //? } else {
        /*ButtonWidget fastResetButton = ButtonWidget.builder(menuQuitWorld, button -> this.fastReset(saveButton)).dimensions(x, y, width, height).build();
        *///? }

        fastResetButton.visible = FastReset.config.buttonLocation != FastResetConfig.ButtonLocation.HIDE;

        //? if <1.17 {
        this.addButton(fastResetButton);
         //? } else {
        /*this.addDrawableChild(fastResetButton);
        *///? }
    }

    @Unique
    private void fastReset(ButtonWidget saveButton) {
        MinecraftServer server = MinecraftClient.getInstance().getServer();
        if (server != null) {
            ((FRMinecraftServer) server).fastReset$fastReset();
        }
        saveButton.onPress();
    }

    @Unique
    private boolean shouldFastReset() {
        if (FastReset.config.alwaysSaveAfter == 0) {
            return true;
        }
        MinecraftServer server = MinecraftClient.getInstance().getServer();
        return server != null && server.getTicks() <= FastReset.config.alwaysSaveAfter * 20;
    }
}