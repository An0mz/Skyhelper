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

object HudEditorSystem {
    private lateinit var editKey: KeyBinding
    private lateinit var widgetsProvider: () -> List<AbstractWidget>
    var editMode = false
        private set

    /** Initialize the HUD editor with a provider for current widgets */
    fun init(widgetsProvider: () -> List<AbstractWidget>) {
        this.widgetsProvider = widgetsProvider

        // Register G as the edit HUD key
        editKey = KeyBindingHelper.registerKeyBinding(
            KeyBinding(
                "key.skyhelper.edit_hud",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
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
            val mx = mc.mouse.x.toDouble()
            val my = mc.mouse.y.toDouble()

            if (editMode) {
                HudEditManager.handleMouse(widgetsProvider(), mx, my)
            }
            widgetsProvider().forEach { it.render(ctx, mx.toInt(), my.toInt(), tickDelta) }
        }
    }

    fun toggleEditor(client: MinecraftClient) {
        if (client.currentScreen is HudEditScreen) {
            // EXIT
            HUDConfigPositions.save(widgetsProvider())
            editMode = false
            client.mouse.lockCursor()
            client.setScreen(null)
            client.player?.sendMessage(
                Text.literal("Exited HUD edit mode").formatted(net.minecraft.util.Formatting.YELLOW),
                false
            )
        } else {
            // ENTER
            HudEditManager.reset()
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