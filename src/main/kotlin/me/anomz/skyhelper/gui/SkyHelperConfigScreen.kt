package me.anomz.skyhelper.gui

import me.anomz.skyhelper.config.SkyHelperConfig
import net.minecraft.client.gui.screen.Screen
import me.shedaniel.clothconfig2.api.ConfigBuilder
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.text.Text

@Environment(EnvType.CLIENT)
object SkyHelperConfigScreen {
    @JvmStatic
    fun create(parent: Screen?): Screen {
        // Ensure we have the latest JSON data
        SkyHelperConfig.load()

        // Build the root cloth‑config screen
        val builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Text.translatable("config.skyhelper.title"))
        val eb: ConfigEntryBuilder = builder.entryBuilder

        // Automatically add every feature’s category in key‑sorted order
        SkyHelperConfig.instance.features
            .sortedBy { it.key }
            .forEach { feature ->
                feature.addCategory(builder, eb)
            }

        // Persist to disk when the user clicks “Done”
        builder.setSavingRunnable { SkyHelperConfig.save() }

        return builder.build()
    }
}