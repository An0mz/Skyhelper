package me.anomz.skyhelper.gui

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import me.anomz.skyhelper.api.ModuleInitializer
import me.anomz.skyhelper.utils.BlockRenderHelper
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.world.ClientWorld
import net.minecraft.block.BlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.world.chunk.WorldChunk
import java.util.function.Predicate

abstract class AbstractBlockHighlighter(
    protected val statePredicate: Predicate<BlockState>,
    private val color: FloatArray,
    private val alpha: Float
) : ModuleInitializer {

    private val highlights = ObjectOpenHashSet<BlockPos>()

    override fun initModule() = init()

    fun init() {
        ClientChunkEvents.CHUNK_LOAD.register(this::onChunkLoad)
        ClientChunkEvents.CHUNK_UNLOAD.register(this::onChunkUnload)
        ClientPlayConnectionEvents.DISCONNECT.register { _, _ -> highlights.clear() }
        WorldRenderEvents.AFTER_TRANSLUCENT.register { ctx ->
            onRender(ctx)
        }
    }

    private fun onChunkLoad(world: ClientWorld, chunk: WorldChunk) {
        chunk.forEachBlockMatchingPredicate(statePredicate) { pos, _ ->
            highlights.add(pos.toImmutable())
        }
    }

    private fun onChunkUnload(world: ClientWorld, chunk: WorldChunk) {
        val baseX = chunk.pos.x shl 4
        val baseZ = chunk.pos.z shl 4
        highlights.removeIf { pos ->
            pos.x in baseX until (baseX + 16) && pos.z in baseZ until (baseZ + 16)
        }
    }

    fun onBlockChanged(pos: BlockPos, newState: BlockState) {
        if (statePredicate.test(newState)) {
            highlights.add(pos.toImmutable())
        } else {
            highlights.remove(pos)
        }
    }

    private fun onRender(ctx: WorldRenderContext) {
        val client = MinecraftClient.getInstance()
        val world  = client.world ?: return
        if (!shouldProcess()) return

        val cam = ctx.camera()
        val cx  = cam.pos.x
        val cy  = cam.pos.y
        val cz  = cam.pos.z

        for (pos in highlights) {
            BlockRenderHelper.getBlockBoundingBox(world, pos)?.let { box ->
                val relBox = box.offset(-cx, -cy, -cz)
                BlockRenderHelper.renderFilled(
                    ctx, relBox,
                    getColor(), getAlpha(),
                    filled = true
                )
            }
        }
    }

    protected abstract fun shouldProcess(): Boolean
    protected open fun getColor(): FloatArray = color
    protected open fun getAlpha(): Float = alpha
}