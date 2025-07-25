package me.anomz.skyhelper

import me.anomz.skyhelper.api.ModuleInitializer
import me.anomz.skyhelper.config.SkyHelperConfig
import me.anomz.skyhelper.gui.SkyHelperConfigScreen
import me.anomz.skyhelper.utils.IslandUtils
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import net.minecraft.text.Text
import org.lwjgl.glfw.GLFW
import java.util.*

object SkyHelperMod : ClientModInitializer {
	override fun onInitializeClient() {
		// Load configuration and initialize utilities
		SkyHelperConfig.load()
		IslandUtils.init()

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

		// Initialize modules
		ServiceLoader.load(ModuleInitializer::class.java).forEach { module ->
			module.initModule()
		}
	}
}