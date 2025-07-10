package me.anomz.skyhelper.config.features.foraging

import me.anomz.skyhelper.config.ConfigFeature
import me.anomz.skyhelper.config.SkyHelperConfig
import me.shedaniel.clothconfig2.api.ConfigBuilder
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder
import net.minecraft.text.Text

class SeaLumiesConfig : ConfigFeature {
    override val key = "sealumies"
    var enabled = true
    var color = 0xFFFFFF
    var alpha = 1f

    override fun addCategory(builder: ConfigBuilder, eb: ConfigEntryBuilder) {
        val cat = builder.getOrCreateCategory(
            Text.translatable("config.skyhelper.category.$key")
        )
        cat.addEntry(
            eb.startBooleanToggle(Text.translatable("$key.enabled"), enabled)
                .setSaveConsumer { enabled = it; SkyHelperConfig.save() }
                .build()
        )
        cat.addEntry(
            eb.startColorField(Text.translatable("$key.color"), color)
                .setSaveConsumer { color = it and 0xFFFFFF; SkyHelperConfig.save() }
                .build()
        )
        cat.addEntry(
            eb.startFloatField(Text.translatable("$key.alpha"), alpha)
                .setMin(0f)
                .setMax(1f)
                .setSaveConsumer { alpha = it.coerceIn(0f, 1f); SkyHelperConfig.save() }
                .build()
        )
    }
}