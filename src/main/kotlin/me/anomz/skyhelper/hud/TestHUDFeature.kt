package me.anomz.skyhelper.hud

import me.anomz.skyhelper.utils.gui.AbstractWidget
import net.minecraft.client.MinecraftClient

class TestHUDFeature : HUDFeature {
    override val key = "test_hud"
    override fun createWidgets() = listOf(
        object : AbstractWidget.TextWidget(key, { "Hello, SkyHelper! Tick ${MinecraftClient.getInstance().world?.time}" }) {
            private var wasClicked = false

            override fun mouseClicked(mx: Double, my: Double, button: Int): Boolean {
                wasClicked = super.mouseClicked(mx, my, button)
                return wasClicked
            }

            override fun mouseReleased(mx: Double, my: Double, button: Int): Boolean {
                if (wasClicked) {
                    MinecraftClient.getInstance().player?.sendMessage(
                        net.minecraft.text.Text.literal("Widget clicked!"),
                        false
                    )
                }
                wasClicked = false
                return super.mouseReleased(mx, my, button)
            }
        }
    )
}