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
import java.util.function.Predicate

@Environment(EnvType.CLIENT)
class SeaPickleHighlighter : AbstractBlockHighlighter(
    /* predicate: */ Predicate { state: net.minecraft.block.BlockState -> state.isOf(Blocks.SEA_PICKLE) },
    /* color: */ floatArrayOf(0f, 1f, 1f),     // cyan
    /* alpha: */ 0.2f
) {
    override fun shouldProcess(): Boolean {
        val client = MinecraftClient.getInstance()
        val world  = client.world ?: return false

        // 1) config must be enabled
        if (!SkyHelperConfig.instance.seaLumies.enabled) return false

        // 2) only when player is submerged
        val eyePos = client.player?.eyePos ?: return false
        val fluid = world.getFluidState(BlockPos(eyePos.x.toInt(), eyePos.y.toInt(), eyePos.z.toInt()))
        return fluid.fluid == Fluids.WATER || fluid.fluid == Fluids.FLOWING_WATER
    }
}