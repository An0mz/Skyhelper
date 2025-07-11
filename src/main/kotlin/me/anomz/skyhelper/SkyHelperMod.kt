package me.anomz.skyhelper

import me.anomz.skyhelper.api.ModuleInitializer
import me.anomz.skyhelper.config.SkyHelperConfig
import me.anomz.skyhelper.gui.SkyHelperConfigScreen
import me.anomz.skyhelper.hud.HUDFeature
import me.anomz.skyhelper.hud.HudEditManager
import me.anomz.skyhelper.hud.HudEditScreen
import me.anomz.skyhelper.hud.HudEditorSystem
import me.anomz.skyhelper.utils.IslandUtils
import me.anomz.skyhelper.utils.gui.AbstractWidget
import me.anomz.skyhelper.utils.gui.HUDConfigPositions
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import org.lwjgl.glfw.GLFW
import java.util.*

object SkyHelperMod : ClientModInitializer {
	override fun onInitializeClient() {
		val features = ServiceLoader.load(HUDFeature::class.java).toList()
		val widgets: List<AbstractWidget> = features.flatMap { it.createWidgets() }
		HUDConfigPositions.load(widgets)
		// Load configuration and initialize utilities
		SkyHelperConfig.load()
		IslandUtils.init()

		ClientTickEvents.END_CLIENT_TICK.register { _ ->
			widgets.forEach { it.tick() }
		}

		HudRenderCallback.EVENT.register { drawContext, tickDelta ->
			val client = MinecraftClient.getInstance()
			val mx = client.mouse.x      // raw window coords
			val my = client.mouse.y

			// 1) let the edit manager handle press/drag/release
			HudEditManager.handleMouse(widgets, mx, my)

			// 2) render everything (including updated positions)
			widgets.forEach { it.render(drawContext, mx.toInt(), my.toInt(), tickDelta) }
		}


		// Register the command
		ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
			dispatcher.register(
				ClientCommandManager.literal("skyhelper").executes { ctx ->
					ctx.source.sendFeedback(Text.of("Opening SkyHelper config…"))
					ctx.source.client.execute {
						ctx.source.client.setScreen(
							SkyHelperConfigScreen.create(
								parent = ctx.source.client.currentScreen
							)
						)
					}
					1
				}
			)
		}

		// Register the keybinding
		val openInfoKey = KeyBindingHelper.registerKeyBinding(
			KeyBinding(
				"key.skyhelper.open_info",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_I,
				"category.skyhelper.keys"
			)
		)

		// Handle keybinding press
		ClientTickEvents.END_CLIENT_TICK.register { client ->
			if (openInfoKey.wasPressed()) {
				client.execute {
					client.setScreen(
						SkyHelperConfigScreen.create(
							parent = client.currentScreen
						)
					)
				}
			}
		}

		HudEditorSystem.init { widgets }



		// Initialize modules
		ServiceLoader.load(ModuleInitializer::class.java).forEach { module ->
			module.initModule()
		}
	}
}