package me.anomz.skyhelper.features.foraging

import com.google.auto.service.AutoService
import me.anomz.skyhelper.api.ModuleInitializer
import me.anomz.skyhelper.config.SkyHelperConfig
import me.anomz.skyhelper.render.AbstractBlockHighlighter
import me.anomz.skyhelper.utils.IslandUtils
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.block.Blocks
import net.minecraft.block.SeaPickleBlock
import net.minecraft.client.MinecraftClient

@Environment(EnvType.CLIENT)
@AutoService(ModuleInitializer::class)
class SeaLumiesHighlighter : AbstractBlockHighlighter(
    { state ->
        state.block == Blocks.SEA_PICKLE
    },
    floatArrayOf(), // we override getColor()
    0f              // we override getAlpha()
), ModuleInitializer {

    override fun initModule() = init()

    override fun shouldProcess(): Boolean {
        if (!SkyHelperConfig.instance.seaLumies.enabled) return false
        if (MinecraftClient.getInstance().player?.isSubmergedInWater != true) return false

        return IslandUtils.isOnGalatea()
    }

    override fun getColor(): FloatArray {
        val colorInt = SkyHelperConfig.instance.seaLumies.color
        val r = ((colorInt shr 16) and 0xFF) / 255f
        val g = ((colorInt shr 8) and 0xFF) / 255f
        val b = (colorInt and 0xFF) / 255f
        return floatArrayOf(r, g, b)
    }

    override fun getAlpha(): Float =
        SkyHelperConfig.instance.seaLumies.alpha
}