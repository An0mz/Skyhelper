package me.anomz.skyhelper.gui

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.world.ClientWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.world.chunk.WorldChunk
import net.minecraft.world.chunk.Chunk
import java.util.function.Predicate
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import me.anomz.skyhelper.utils.BlockRenderHelper

/**
 * Base class for highlighting blocks that match [statePredicate].
 * Subclasses decide when it’s active via [shouldProcess].
 */
abstract class AbstractBlockHighlighter(
    private val statePredicate: Predicate<net.minecraft.block.BlockState>,
    private val color: FloatArray,    // RGB floats
    private val alpha: Float           // 0..1
) {
    private val highlights = ObjectOpenHashSet<BlockPos>()

    /** Call this once during client init. */
    fun init() {
        ClientChunkEvents.CHUNK_LOAD.register(this::onChunkLoad)
        ClientChunkEvents.CHUNK_UNLOAD.register(this::onChunkUnload)
        ClientPlayConnectionEvents.JOIN.register { _, _, _ -> highlights.clear() }
        WorldRenderEvents.AFTER_TRANSLUCENT.register(this::onRender)
    }

    /** Called for every new chunk that arrives. */
    private fun onChunkLoad(world: ClientWorld, chunk: WorldChunk) {
        if (!shouldProcess()) return
        chunk.forEachBlockMatchingPredicate(statePredicate) { pos, _ ->
            highlights.add(pos.toImmutable())
        }
    }

    /** Called when chunks unload. */
    private fun onChunkUnload(world: ClientWorld, chunk: WorldChunk) {
        if (!shouldProcess()) return
        val cpos = chunk.pos
        highlights.removeIf { it.x shr 4 == cpos.x && it.z shr 4 == cpos.z }
    }

    /** Every frame, draw a box around each stored pos. */
    private fun onRender(ctx: WorldRenderContext) {
        val client = MinecraftClient.getInstance()
        if (!shouldProcess() || client.world == null) return

        // once per frame
        val cam = ctx.camera()
        val cx  = cam.pos.x
        val cy  = cam.pos.y
        val cz  = cam.pos.z

        for (pos in highlights) {
            BlockRenderHelper.getBlockBoundingBox(client.world!!, pos)?.let { outline ->
                // move into camera space
                val relBox = outline.offset(-cx, -cy, -cz)

                // draw filled box (or switch to renderOutline if you prefer wireframe)
                BlockRenderHelper.renderFilled(
                    ctx,
                    relBox,
                    color,
                    alpha,
                    filled = true
                )
            }
        }
    }


    /** Should we be actively highlighting right now? */
    protected abstract fun shouldProcess(): Boolean
}
