package me.anomz.skyhelper.hud

import me.anomz.skyhelper.utils.gui.AbstractWidget

/**
 * A self‑contained HUD feature.
 * When the game renders the overlay, we’ll ask each feature for its widgets.
 */
interface HUDFeature {
    /** A unique key for this feature, used in config IDs. */
    val key: String

    /** Called once at startup to create your widgets (you set their default position here). */
    fun createWidgets(): List<AbstractWidget>
}