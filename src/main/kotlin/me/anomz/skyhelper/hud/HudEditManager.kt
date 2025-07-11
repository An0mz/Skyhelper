package me.anomz.skyhelper.hud

import me.anomz.skyhelper.utils.gui.AbstractWidget
import net.minecraft.client.MinecraftClient
import org.lwjgl.glfw.GLFW

object HudEditManager {
    /** Are we in edit mode? */
    var editMode = false
        internal set

    private var dragging: AbstractWidget? = null
    private var lastMouseDown = false
    private var lastMx = 0.0
    private var lastMy = 0.0

    /** Reset drag state when entering edit mode */
    fun reset(widgets: List<AbstractWidget>) {
        dragging = null
        lastMouseDown = false
        widgets.forEach { it.resetDrag() }
    }

    /** Handle click/drag/release on widgets */
    fun handleMouse(widgets: List<AbstractWidget>, mx: Double, my: Double) {
        if (!editMode) return

        val client = MinecraftClient.getInstance()
        val window = client.window.handle
        val pressed = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS

        if (pressed) {
            if (!lastMouseDown) {
                // just pressed: pick topmost under cursor
                dragging = widgets
                    .filter { it.contains(mx, my) }
                    .lastOrNull()
                    ?.also { it.mouseClicked(mx, my, 0) }
            } else {
                // held down: drag by delta
                val dx = mx - lastMx
                val dy = my - lastMy
                dragging?.mouseDragged(mx, my, 0, dx, dy)
            }
        } else if (lastMouseDown) {
            // released
            dragging?.mouseReleased(mx, my, 0)
            dragging = null
        }

        lastMouseDown = pressed
        lastMx = mx
        lastMy = my
    }
}
