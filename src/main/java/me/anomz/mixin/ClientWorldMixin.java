package me.anomz.mixin;

import me.anomz.skyhelper.api.ModuleInitializer;
import me.anomz.skyhelper.render.AbstractBlockHighlighter;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ServiceLoader;

@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin {
    @Inject(
            method = "setBlockState",
            at = @At("RETURN")
    )
    private void onSetBlockState(
            BlockPos pos,
            BlockState newState,
            int flags,
            int updateFlags,
            CallbackInfoReturnable<BlockState> cir
    ) {
        // Forward to every AbstractBlockHighlighter
        ServiceLoader
                .load(ModuleInitializer.class)
                .stream()
                .filter(m -> m instanceof AbstractBlockHighlighter)
                .map(m -> (AbstractBlockHighlighter)m)
                .forEach(h -> h.onBlockChanged(pos, newState));
    }
}
