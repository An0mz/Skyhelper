package me.anomz.skyhelper.gui

import me.anomz.skyhelper.config.SkyHelperConfig
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.block.Blocks
import net.minecraft.client.MinecraftClient
import net.minecraft.state.property.Properties
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.client.world.ClientWorld
import net.minecraft.world.chunk.WorldChunk
import net.minecraft.fluid.Fluids
import net.minecraft.registry.tag.FluidTags
import java.util.function.Predicate

@Environment(EnvType.CLIENT)
class SeaPickleHighlighter : AbstractBlockHighlighter(
    Predicate { it.isOf(Blocks.SEA_PICKLE) },
    floatArrayOf(0f,1f,1f),  /* cyan */
    0.3f                     /* alpha */
) {
    override fun shouldProcess(): Boolean {
        val client = MinecraftClient.getInstance()
        val world  = client.world ?: return false
        // only when submerged & enabled…
        return SkyHelperConfig.instance.seaLumies.enabled &&
                world.getFluidState(client.player!!.blockPos).fluid.isIn(FluidTags.WATER)
    }
}