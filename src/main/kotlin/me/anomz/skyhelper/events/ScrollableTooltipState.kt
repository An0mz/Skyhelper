package me.anomz.skyhelper.features.tooltip

import net.minecraft.util.math.Box

object ScrollableTooltipState {
    /** how many pixels we’ve nudged the tooltip vertically  */
    @JvmField
    var offset: Int = 0

    /** last tooltip rectangle (un‑offset), in screen coordinates  */
    @JvmField
    var lastTooltipBox: Box? = null
}
