package me.anomz.skyhelper.hud

import me.anomz.skyhelper.utils.gui.AbstractWidget
import me.anomz.skyhelper.utils.gui.HUDConfigPositions
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import net.minecraft.text.Text
import org.lwjgl.glfw.GLFW
import kotlin.div
import kotlin.text.toInt

object HudEditorSystem {
    private lateinit var editKey: KeyBinding
    private lateinit var widgetsProvider: () -> List<AbstractWidget>
    var editMode = false
        private set
    var key = GLFW.GLFW_KEY_KP_8

    /** Initialize the HUD editor with a provider for current widgets */
    fun init(widgetsProvider: () -> List<AbstractWidget>) {
        this.widgetsProvider = widgetsProvider

        // Register G as the edit HUD key
        editKey = KeyBindingHelper.registerKeyBinding(
            KeyBinding(
                "key.skyhelper.edit_hud",
                InputUtil.Type.KEYSYM,
                key,
                "category.skyhelper.keys"
            )
        )

        // Listen for G to enter edit mode
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            if (!editKey.wasPressed()) return@register
            client.execute { toggleEditor(client) }
        }

        // Always forward render & mouse events
        HudRenderCallback.EVENT.register { ctx, tickDelta ->
            val mc = MinecraftClient.getInstance()
            val window = mc.window
            val scale = window.scaleFactor
            val mx = mc.mouse.x / scale
            val my = mc.mouse.y / scale

            if (editMode) {
                HudEditManager.handleMouse(widgetsProvider(), mx, my)
            }
            widgetsProvider().forEach { it.render(ctx, mx.toInt(), my.toInt(), tickDelta) }
        }
    }

    fun toggleEditor(client: MinecraftClient) {
        println("toggleEditor called, currentScreen=${client.currentScreen}")
        if (client.currentScreen is HudEditScreen) {
            // EXIT
            HUDConfigPositions.save(widgetsProvider())
            HUDConfigPositions.load(widgetsProvider())
            editMode = false
            client.mouse.lockCursor()
            client.setScreen(null)
            client.player?.sendMessage(
                Text.literal("Exited HUD edit mode").formatted(net.minecraft.util.Formatting.YELLOW),
                false
            )
        } else {
            // ENTER
            HudEditManager.reset(widgetsProvider())
            editMode = true
            client.mouse.unlockCursor()
            client.setScreen(HudEditScreen())
            client.player?.sendMessage(
                Text.literal("Entered HUD edit mode").formatted(net.minecraft.util.Formatting.YELLOW),
                false
            )
        }
    }
}