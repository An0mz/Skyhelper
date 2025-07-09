package me.anomz.skyhelper

import me.anomz.skyhelper.api.ModuleInitializer
import net.fabricmc.api.ClientModInitializer
import me.anomz.skyhelper.config.SkyHelperConfig
import me.anomz.skyhelper.utils.IslandUtils
import java.util.ServiceLoader

object SkyHelperMod : ClientModInitializer {
	override fun onInitializeClient() {
		SkyHelperConfig.load()
		IslandUtils.init()
		ServiceLoader.load(ModuleInitializer::class.java).forEach { module ->
			module.initModule()
		}
	}
}