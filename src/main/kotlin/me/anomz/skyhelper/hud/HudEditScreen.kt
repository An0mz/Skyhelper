package me.anomz.skyhelper.hud

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.DrawContext
import net.minecraft.text.Text
import org.lwjgl.glfw.GLFW

class HudEditScreen : Screen(Text.empty()) {
    override fun shouldPause(): Boolean = false

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)
        // Transparent background, no children
    }

    // Allow G to exit even when this screen has focus
    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (keyCode == GLFW.GLFW_KEY_G) {
            HudEditorSystem.toggleEditor(MinecraftClient.getInstance())
            return true
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }
}