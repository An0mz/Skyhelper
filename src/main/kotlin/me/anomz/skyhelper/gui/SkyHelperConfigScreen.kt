package me.anomz.skyhelper.gui

import me.anomz.skyhelper.config.SkyHelperConfig
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import me.shedaniel.clothconfig2.api.ConfigBuilder
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder

@Environment(EnvType.CLIENT)
object SkyHelperConfigScreen {
    @JvmStatic
    fun create(parent: Screen?): Screen {
        // reload JSON
        SkyHelperConfig.load()

        val builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Text.translatable("config.skyhelper.title"))
        val eb  = builder.entryBuilder
        val sea = builder.getOrCreateCategory(Text.translatable("config.skyhelper.category.sealumies"))
        val cfg = SkyHelperConfig.instance.seaLumies

        // enabled toggle
        sea.addEntry(
            eb.startBooleanToggle(
                Text.translatable("config.skyhelper.sealumies.enabled"),
                cfg.enabled
            )
                .setSaveConsumer { cfg.enabled = it }
                .build()
        )

        // single color picker (0xRRGGBB)
        sea.addEntry(
            eb.startColorField(
                Text.translatable("config.skyhelper.sealumies.color"),
                cfg.color
            )
                .setSaveConsumer { newHex ->
                    cfg.color = newHex and 0xFFFFFF
                }
                .build()
        )

        // single alpha slider
        sea.addEntry(
            eb.startFloatField(
                Text.translatable("config.skyhelper.sealumies.alpha"),
                cfg.alpha
            )
                .setSaveConsumer { newA ->
                    cfg.alpha = newA.coerceIn(0f, 1f)
                }
                .build()
        )

        // save on Done
        builder.setSavingRunnable { SkyHelperConfig.save() }
        return builder.build()
    }
}
