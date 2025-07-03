package me.anomz.skyhelper.features.foraging

import com.google.auto.service.AutoService
import me.anomz.skyhelper.api.ModuleInitializer
import me.anomz.skyhelper.config.SkyHelperConfig
import me.anomz.skyhelper.gui.AbstractBlockHighlighter
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.block.Blocks
import net.minecraft.block.BlockState

@Environment(EnvType.CLIENT)
@AutoService(ModuleInitializer::class)
class SeaLumiesHighlighter : AbstractBlockHighlighter(
    { state -> state.block == Blocks.SEA_PICKLE },
    floatArrayOf(), // Will override getColor()
    0f              // Will override getAlpha()
), ModuleInitializer {

    override fun initModule() {
        this.init()
    }

    override fun shouldProcess(): Boolean {
        return SkyHelperConfig.instance.seaLumies.enabled
    }

    override fun getColor(): FloatArray {
        val colorInt = SkyHelperConfig.instance.seaLumies.color
        val r = ((colorInt shr 16) and 0xFF) / 255f
        val g = ((colorInt shr 8) and 0xFF) / 255f
        val b = (colorInt and 0xFF) / 255f
        return floatArrayOf(r, g, b)
    }

    override fun getAlpha(): Float {
        return SkyHelperConfig.instance.seaLumies.alpha
    }
}