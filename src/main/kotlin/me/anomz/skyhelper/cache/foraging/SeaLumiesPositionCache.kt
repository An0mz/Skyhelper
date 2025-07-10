package me.anomz.skyhelper.cache.foraging

import net.minecraft.block.Blocks
import net.minecraft.util.math.BlockPos
import me.anomz.skyhelper.config.SkyHelperConfig
import net.minecraft.client.MinecraftClient

/**
 * One‑time scan of your floating island for all SEA_PICKLE positions.
 */
object SeaLumiesPositionCache {
    // fill this once on first use:
    private var initialized = false
    val positions = mutableListOf<BlockPos>()

    fun ensureScanned() {
        if (initialized) return
        initialized = true

        val world = MinecraftClient.getInstance().world ?: return
        val cfg   = SkyHelperConfig.instance.seaLumies
        if (!cfg.enabled) return

        // replace these bounds with your island's true extents:
        val minX = -64
        val maxX = +64
        val minY = world.bottomY
        val maxY = world.bottomY + world.height - 1
        val minZ = -64
        val maxZ = +64

        for (x in minX..maxX) {
            for (z in minZ..maxZ) {
                for (y in minY..maxY) {
                    val pos = BlockPos(x, y, z)
                    if (world.getBlockState(pos).block == Blocks.SEA_PICKLE) {
                        positions += pos
                    }
                }
            }
        }
    }
}