// src/main/kotlin/me/anomz/skyhelper/utils/gui/AbstractWidget.kt
package me.anomz.skyhelper.utils.gui

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.RenderTickCounter
import net.minecraft.text.Text

/**
 * A single draggable element on the HUD.
 * Subclass this to render text, icons, bars, etc.
 *
 * @param featureKey unique ID for saving/loading this widget’s pos
 */
abstract class AbstractWidget(
    val featureKey: String
) {
    var x = 10
    var y = 10
    private var dragging = false

    abstract fun render(ms: DrawContext, mouseX: Int, mouseY: Int, delta: RenderTickCounter?)
    open fun tick() {}

    open fun mouseClicked(mx: Double, my: Double, button: Int): Boolean {
        if (button == 0 && contains(mx, my)) {
            dragging = true
            return true
        }
        return false
    }
    open fun mouseDragged(mx: Double, my: Double, button: Int, dx: Double, dy: Double): Boolean {
        if (dragging) {
            x = (x + dx).toInt()
            y = (y + dy).toInt()
            return true
        }
        return false
    }
    open fun mouseReleased(mx: Double, my: Double, button: Int): Boolean {
        if (dragging) {
            dragging = false
            return true
        }
        return false
    }

    abstract fun contains(mx: Double, my: Double): Boolean

    /** Example text widget for a feature. */
    open class TextWidget(
        featureKey: String,
        private val label: () -> String
    ) : AbstractWidget(featureKey) {

        private val width: Int
            get() = MinecraftClient.getInstance()
                .textRenderer
                .getWidth(label()) + 4
        private val height: Int
            get() = MinecraftClient.getInstance()
                .textRenderer
                .fontHeight + 2

        override fun render(ms: DrawContext, mouseX: Int, mouseY: Int, delta: RenderTickCounter?) {
            val bg = if (me.anomz.skyhelper.hud.HudEditManager.editMode)
                0x8800AA00.toInt()
            else
                0x88000000.toInt()
            ms.fill(x, y, x + width, y + height, bg)
            ms.drawText(
                MinecraftClient.getInstance().textRenderer,
                Text.literal(label()),
                x + 2, y + 1, 0xFFFFFF, false
            )
        }

        override fun contains(mx: Double, my: Double) =
            mx in x.toDouble()..(x + width).toDouble() &&
                    my in y.toDouble()..(y + height).toDouble()
    }
}
