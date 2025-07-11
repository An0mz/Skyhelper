package me.anomz.skyhelper.hud

import me.anomz.skyhelper.utils.gui.AbstractWidget
import net.minecraft.client.MinecraftClient

class TestHUDFeature : HUDFeature {
    override val key = "test_hud"
    override fun createWidgets() = listOf(
        AbstractWidget.TextWidget(key) {
            "Hello, SkyHelper! Tick ${MinecraftClient.getInstance().world?.time}"
        }
    )
}
