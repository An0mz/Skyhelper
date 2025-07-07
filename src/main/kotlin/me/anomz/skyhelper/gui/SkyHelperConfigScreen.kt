package me.anomz.skyhelper.gui

import me.anomz.skyhelper.config.SkyHelperConfig
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import me.shedaniel.clothconfig2.api.ConfigBuilder
import me.shedaniel.clothconfig2.api.ConfigCategory
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder

@Environment(EnvType.CLIENT)
object SkyHelperConfigScreen {
    @JvmStatic
    fun create(parent: Screen?): Screen {
        SkyHelperConfig.load()
        val builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Text.translatable("config.skyhelper.title"))
        val eb = builder.entryBuilder

        addSeaLumiesCategory(builder, eb)
        addScrollableTooltipCategory(builder, eb)

        builder.setSavingRunnable { SkyHelperConfig.save() }
        return builder.build()
    }

    private fun addSeaLumiesCategory(builder: ConfigBuilder, eb: ConfigEntryBuilder) {
        val sea = builder.getOrCreateCategory(Text.translatable("config.skyhelper.category.sealumies"))
        val cfg = SkyHelperConfig.instance.seaLumies

        sea.addEntry(booleanToggle(eb, "config.skyhelper.sealumies.enabled", cfg::enabled))
        sea.addEntry(colorField(eb, "config.skyhelper.sealumies.color", cfg::color))
        sea.addEntry(floatField(eb, "config.skyhelper.sealumies.alpha", cfg::alpha, 0f, 1f))
    }

    private fun addScrollableTooltipCategory(builder: ConfigBuilder, eb: ConfigEntryBuilder) {
        val cat = builder.getOrCreateCategory(Text.translatable("config.skyhelper.category.tooltip"))
        val cfg = SkyHelperConfig.instance.scrollableTooltip

        cat.addEntry(booleanToggle(eb, "config.skyhelper.scrollabletooltip.enabled", cfg::enabled))
    }

    private fun booleanToggle(
        eb: ConfigEntryBuilder,
        key: String,
        ref: kotlin.reflect.KMutableProperty0<Boolean>
    ) = eb.startBooleanToggle(Text.translatable(key), ref.get())
        .setSaveConsumer {
            ref.set(it)
            SkyHelperConfig.save()
            // Optionally: trigger live update here
        }
        .build()

    private fun colorField(
        eb: ConfigEntryBuilder,
        key: String,
        ref: kotlin.reflect.KMutableProperty0<Int>
    ) = eb.startColorField(Text.translatable(key), ref.get())
        .setSaveConsumer {
            ref.set(it and 0xFFFFFF)
            SkyHelperConfig.save()
            // Optionally: trigger live update here
        }
        .build()

    private fun floatField(
        eb: ConfigEntryBuilder,
        key: String,
        ref: kotlin.reflect.KMutableProperty0<Float>,
        min: Float,
        max: Float
    ) = eb.startFloatField(Text.translatable(key), ref.get())
        .setSaveConsumer {
            ref.set(it.coerceIn(min, max))
            SkyHelperConfig.save()
            // Optionally: trigger live update here
        }
        .build()
}