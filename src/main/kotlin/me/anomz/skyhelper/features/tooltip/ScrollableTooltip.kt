package me.anomz.skyhelper.features.tooltip

import com.google.auto.service.AutoService
import me.anomz.skyhelper.api.ModuleInitializer
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents
import net.minecraft.client.util.InputUtil
import net.minecraft.util.math.Box
import org.lwjgl.glfw.GLFW
import net.minecraft.util.math.MathHelper
import kotlin.math.abs

@AutoService(ModuleInitializer::class)
class ScrollableTooltip : ModuleInitializer {
    override fun initModule() {
        ScreenEvents.BEFORE_INIT.register { client, screen, _, _ ->
            var prevBox: Box? = null
            ScreenMouseEvents.beforeMouseScroll(screen).register { _, _, _, _, vertical ->
                val handle = client.window.handle
                if (
                    InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_LEFT_SHIFT) ||
                    InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_RIGHT_SHIFT)
                ) {
                    ScrollableTooltipState.offset = MathHelper.clamp(
                        ScrollableTooltipState.offset + (-vertical * 10).toInt(),
                        -200, 200
                    )
                    true
                } else {
                    false
                }
            }
            ScreenEvents.afterTick(screen).register {
                val box = ScrollableTooltipState.lastTooltipBox
                val threshold = 10.0
                val movedSignificantly = prevBox != null && box != null &&
                        (abs(box.minX - prevBox!!.minX) > threshold ||
                                abs(box.minY - prevBox!!.minY) > threshold ||
                                abs(box.maxX - prevBox!!.maxX) > threshold ||
                                abs(box.maxY - prevBox!!.maxY) > threshold ||
                                abs((box.maxX - box.minX) - (prevBox!!.maxX - prevBox!!.minX)) > threshold ||
                                abs((box.maxY - box.minY) - (prevBox!!.maxY - prevBox!!.minY)) > threshold)
                if ((box == null && ScrollableTooltipState.offset != 0) || movedSignificantly) {
                    ScrollableTooltipState.offset = 0
                }
                prevBox = box
            }
        }
    }
}