package me.anomz.skyhelper.features.tooltip

import com.google.auto.service.AutoService
import me.anomz.skyhelper.api.ModuleInitializer
import me.anomz.skyhelper.config.SkyHelperConfig
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents
import net.minecraft.client.util.InputUtil
import org.lwjgl.glfw.GLFW
import net.minecraft.util.math.MathHelper

@AutoService(ModuleInitializer::class)
class ScrollableTooltip : ModuleInitializer {
    override fun initModule() {
        ScreenEvents.BEFORE_INIT.register { client, screen, _, _ ->
            ScreenMouseEvents.beforeMouseScroll(screen).register { _, _, _, _, vertical ->
                if (!SkyHelperConfig.instance.scrollableTooltip.enabled) return@register
                val handle = client.window.handle
                if (
                    InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_LEFT_SHIFT) ||
                    InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_RIGHT_SHIFT)
                ) {
                    ScrollableTooltipState.offset = MathHelper.clamp(
                        ScrollableTooltipState.offset + (-vertical * 10).toInt(),
                        -200, 200
                    )
                }
            }
            ScreenKeyboardEvents.afterKeyRelease(screen).register { screen, keyCode, scanCode, modifiers ->
                if (!SkyHelperConfig.instance.scrollableTooltip.enabled) return@register
                if (keyCode == GLFW.GLFW_KEY_LEFT_SHIFT || keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) {
                    ScrollableTooltipState.offset = 0
                }
            }
            ScreenEvents.afterTick(screen).register {
                if (!SkyHelperConfig.instance.scrollableTooltip.enabled) {
                    ScrollableTooltipState.offset = 0
                    ScrollableTooltipState.lastTooltipBox = null
                }
            }
        }
    }
}