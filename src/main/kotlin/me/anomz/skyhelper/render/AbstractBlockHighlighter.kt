package me.anomz.skyhelper.gui

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import me.anomz.skyhelper.api.ModuleInitializer
import me.anomz.skyhelper.utils.BlockRenderHelper
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.client.world.ClientWorld
import net.minecraft.block.BlockState
import net.minecraft.util.ActionResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.chunk.WorldChunk
import java.util.function.Predicate

/**
 * Base class for highlighting blocks that match [statePredicate].
 * Subclasses decide activation via [shouldProcess].
 */
abstract class AbstractBlockHighlighter(
    protected val statePredicate: Predicate<BlockState>,
    private val color: FloatArray,
    private val alpha: Float
) : ModuleInitializer {

    // All tracked positions
    private val highlights = ObjectOpenHashSet<BlockPos>()

    override fun initModule() = init()

    fun init() {
        // 1) Seed from loaded chunks
        ClientChunkEvents.CHUNK_LOAD.register(this::onChunkLoad)
        ClientChunkEvents.CHUNK_UNLOAD.register(this::onChunkUnload)
        ClientPlayConnectionEvents.DISCONNECT.register { _, _ -> highlights.clear() }

        // 2) Immediate placement callback
        UseBlockCallback.EVENT.register { player, world, hand, hit ->
            if (!shouldProcess() || world !is ClientWorld) return@register ActionResult.PASS
            val placePos = hit.blockPos.offset(hit.side)
            val state = world.getBlockState(placePos)
            if (statePredicate.test(state)) {
                highlights.add(placePos.toImmutable())
            }
            ActionResult.PASS
        }

        // 3) Render each frame *after* entities
        WorldRenderEvents.AFTER_ENTITIES.register(this::onRender)
    }

    private fun onChunkLoad(world: ClientWorld, chunk: WorldChunk) {
        if (!shouldProcess()) return
        chunk.forEachBlockMatchingPredicate(statePredicate) { pos, _ ->
            highlights.add(pos.toImmutable())
        }
    }

    private fun onChunkUnload(world: ClientWorld, chunk: WorldChunk) {
        if (!shouldProcess()) return
        val baseX = chunk.pos.x shl 4
        val baseZ = chunk.pos.z shl 4
        highlights.removeIf { pos ->
            pos.x in baseX until (baseX + 16) && pos.z in baseZ until (baseZ + 16)
        }
    }

    /** Called from the ClientWorld mixin on every block state change. */
    fun onBlockChanged(pos: BlockPos, newState: BlockState) {
        if (!shouldProcess()) return
        if (statePredicate.test(newState)) {
            highlights.add(pos.toImmutable())
        } else {
            highlights.remove(pos)
        }
    }

    /** Renders all tracked positions each frame. */
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
                    color, alpha,
                    filled = true
                )
            }
        }
    }

    /** Should highlighting be active right now? */
    protected abstract fun shouldProcess(): Boolean
}
