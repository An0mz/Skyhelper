package me.anomz.skyhelper.config.features.foraging

import me.anomz.skyhelper.config.ConfigFeature
import me.shedaniel.clothconfig2.api.ConfigBuilder
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder
import net.minecraft.text.Text

data class ForagingTrackerConfig(
    var enabled: Boolean = true,
    var onlyHoldingAxe: Boolean = true,
    override val key: String = "foragingtracker"
) : ConfigFeature {
    override fun addCategory(
        builder: ConfigBuilder,
        eb: ConfigEntryBuilder
    ) {
        val category = builder.getOrCreateCategory(Text.of("Foraging Tracker"))
        category.addEntry(
            eb.startBooleanToggle(Text.of("Enabled"), enabled)
                .setDefaultValue(true)
                .setSaveConsumer { enabled = it }
                .build()
        )
        category.addEntry(
            eb.startBooleanToggle(Text.of("Only Holding Axe"), onlyHoldingAxe)
                .setDefaultValue(true)
                .setSaveConsumer { onlyHoldingAxe = it }
                .build()
        )
    }
}