package me.anomz.skyhelper.utils

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.debug.DebugRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.client.world.ClientWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box

object BlockRenderHelper {
    fun getBlockBoundingBox(world: ClientWorld, pos: BlockPos): Box? {
        val state = world.getBlockState(pos)
        val shape = state.getOutlineShape(world, pos)
        return shape.boundingBoxes.firstOrNull()?.let { bb ->
            Box(
                bb.minX + pos.x, bb.minY + pos.y, bb.minZ + pos.z,
                bb.maxX + pos.x, bb.maxY + pos.y, bb.maxZ + pos.z
            )
        }
    }

    fun renderFilled(
        ctx: WorldRenderContext,
        box: Box,
        color: FloatArray,
        alpha: Float,
        filled: Boolean
    ) {
        val ms = ctx.matrixStack() as? MatrixStack ?: return
        val consumers = ctx.consumers() as? VertexConsumerProvider.Immediate ?: return
        val (r, g, b) = color
        if (filled) {
            DebugRenderer.drawBox(
                ms,
                consumers,
                box.minX, box.minY, box.minZ,
                box.maxX, box.maxY, box.maxZ,
                r, g, b, alpha
            )
        }
    }

    fun renderOutline(
        ctx: WorldRenderContext,
        box: Box,
        color: FloatArray,
        alpha: Float
    ) {
        val ms        = ctx.matrixStack() as? MatrixStack ?: return
        val consumers = ctx.consumers()   as? VertexConsumerProvider.Immediate ?: return
        val (r, g, b) = color

        // draw just the wireframe
        DebugRenderer.drawBox(
            ms,
            consumers,
            box.minX, box.minY, box.minZ,
            box.maxX, box.maxY, box.maxZ,
            r, g, b, alpha
        )
    }

}