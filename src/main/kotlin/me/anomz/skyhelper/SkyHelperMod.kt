package me.anomz.skyhelper

import net.fabricmc.api.ClientModInitializer
import me.anomz.skyhelper.config.SkyHelperConfig
import me.anomz.skyhelper.gui.SeaPickleHighlighter
import me.anomz.skyhelper.gui.SkyHelperConfigScreen
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.minecraft.client.MinecraftClient

object SkyHelperMod : ClientModInitializer {
	private val pickleHighlighter = SeaPickleHighlighter()

	override fun onInitializeClient() {
		SkyHelperConfig.load()

		// wire in your config‑screen command
		ClientCommandRegistrationCallback.EVENT.register { disp, _ ->
			disp.register(
				ClientCommandManager.literal("skyhelper").executes {
					MinecraftClient.getInstance().setScreen(
						SkyHelperConfigScreen.create(
							MinecraftClient.getInstance().currentScreen
						)
					)
					1
				}
			)
		}

		// _then_ register the highlighter
		pickleHighlighter.init()
	}
}
