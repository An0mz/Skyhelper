package me.anomz.skyhelper.config.features.tooltip

import me.anomz.skyhelper.config.ConfigFeature
import me.shedaniel.clothconfig2.api.ConfigBuilder
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder
import net.minecraft.text.Text

data class ScrollableTooltipConfig(
    override val key: String = "scrollable_tooltip",
    var enabled: Boolean = false
) : ConfigFeature {
    override fun addCategory(builder: ConfigBuilder, eb: ConfigEntryBuilder) {
        val cat = builder.getOrCreateCategory(Text.translatable("config.skyhelper.category.scrollable_tooltip"))
        cat.addEntry(
            eb.startBooleanToggle(Text.translatable("config.skyhelper.scrollable_tooltip.enabled"), enabled)
                .setSaveConsumer { enabled = it }
                .build()
        )
    }
}