package me.anomz.skyhelper.config.features.foraging

import me.shedaniel.autoconfig.annotation.ConfigEntry

data class SeaLumiesConfig(
    var enabled: Boolean = true,
    var color: Int = 0x00FFFF, // Cyan
    var alpha: Float = 0.5f,
    @ConfigEntry.Gui.Excluded // hide this if you don’t need it in the GUI
    var renderRangeSq: Double = 32.0 * 32.0
)
