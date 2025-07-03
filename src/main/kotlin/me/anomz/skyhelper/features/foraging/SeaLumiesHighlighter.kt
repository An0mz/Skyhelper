package me.anomz.skyhelper.features.foraging

import com.google.auto.service.AutoService
import me.anomz.skyhelper.api.ModuleInitializer
import me.anomz.skyhelper.gui.AbstractBlockHighlighter
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.block.Blocks

@Environment(EnvType.CLIENT)
@AutoService(ModuleInitializer::class)
class SeaLumiesHighlighter
/** no‑arg ctor for Fabric entrypoint loader */()
    : AbstractBlockHighlighter(
    /* predicate: */ { state -> state.block == Blocks.SEA_PICKLE },
    /* color: */ floatArrayOf(0f, 1f, 1f),    // lime green
    /* alpha: */ 0.5f
),
    ModuleInitializer
{
    /** Fabric will call this on startup */
    override fun initModule() {
        // wires up chunk‑load & block‑update listeners in AbstractBlockHighlighter
        this.init()
    }

    /**
     * Return `true` when you want to highlight sea pickles.
     * You can tie this to a keybind or config value.
     */
    override fun shouldProcess(): Boolean {
        // for now, always on; later hook into a config or KeyBinding
        return true
    }
}
