// HudEditScreen.kt
package me.anomz.skyhelper.hud

import me.anomz.skyhelper.utils.gui.AbstractWidget
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.DrawContext
import net.minecraft.text.Text

class HudEditScreen : Screen(Text.empty()) {
    private var draggingWidget: AbstractWidget? = null

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        val widgets = getWidgets()
        widgets.lastOrNull { it.contains(mouseX, mouseY) }?.let { widget ->
            if (widget.mouseClicked(mouseX, mouseY, button)) {
                draggingWidget = widget
                return true
            }
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
        draggingWidget?.let { widget ->
            if (widget.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) return true
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        draggingWidget?.mouseReleased(mouseX, mouseY, button)
        draggingWidget = null
        return super.mouseReleased(mouseX, mouseY, button)
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)
        getWidgets().forEach { widget ->
            widget.render(context, mouseX, mouseY, null)
        }
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (keyCode == HudEditorSystem.key) { // Fixed reference
            HudEditorSystem.toggleEditor(MinecraftClient.getInstance())
            return true
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    private fun getWidgets(): List<AbstractWidget> {
        val provider = HudEditorSystem::class.java.getDeclaredField("widgetsProvider")
        provider.isAccessible = true
        return (provider.get(HudEditorSystem) as () -> List<AbstractWidget>)()
    }
}