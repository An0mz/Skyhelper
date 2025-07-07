package me.anomz.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import me.anomz.skyhelper.features.tooltip.ScrollableTooltipState;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import org.joml.Vector2ic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(DrawContext.class)
public class DrawContextMixin {
    @Unique private int lastX, lastY, lastW, lastH;

    /**
     * Before vanilla draws any tooltip, record its screen bounds
     * (so our ScreenMixin can test “over the tooltip”), then
     * push our offset into the matrix stack.
     */
    @Inject(
            method = "drawTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;IILnet/minecraft/client/gui/tooltip/TooltipPositioner;Lnet/minecraft/util/Identifier;)V",
            at = @At("HEAD")
    )
    private void beforeTooltip(
            TextRenderer renderer,
            List<TooltipComponent> components,
            int x, int y,
            TooltipPositioner positioner,
            Identifier texture,
            CallbackInfo ci
    ) {
        // compute raw width & height
        int w = 0, h = 0;
        for (int i = 0; i < components.size(); i++) {
            TooltipComponent comp = components.get(i);
            w = Math.max(w, comp.getWidth(renderer));
            h += comp.getHeight(renderer);
            if (i == 0) h += 2; // vanilla spacing above second+ lines
        }
        h += 6; // vanilla padding
        w += 6;

        // figure out the actual on-screen x/y
        Vector2ic pos = positioner.getPosition(
                ((DrawContext)(Object)this).getScaledWindowWidth(),
                ((DrawContext)(Object)this).getScaledWindowHeight(),
                x, y, w, h
        );

        lastX = pos.x();
        lastY = pos.y();
        lastW = w;
        lastH = h;
        ScrollableTooltipState.lastTooltipBox = new Box(
                lastX, lastY, 0,
                lastX + lastW, lastY + lastH, 0
        );

        // now translate your offset into the matrix stack
        ((DrawContext)(Object)this).getMatrices().push();
        ((DrawContext)(Object)this).getMatrices().translate(0, ScrollableTooltipState.offset, 0);
    }

    /**
     * After the tooltip is drawn, pop back the matrix.
     */
    @Inject(
            method = "drawTooltip(Lnet/minecraft/client/font/TextRenderer;Ljava/util/List;IILnet/minecraft/client/gui/tooltip/TooltipPositioner;Lnet/minecraft/util/Identifier;)V",
            at = @At("RETURN")
    )
    private void afterTooltip(
            TextRenderer renderer,
            List<TooltipComponent> components,
            int x, int y,
            TooltipPositioner positioner,
            Identifier texture,
            CallbackInfo ci
    ) {
        ((DrawContext)(Object)this).getMatrices().pop();
    }
}
