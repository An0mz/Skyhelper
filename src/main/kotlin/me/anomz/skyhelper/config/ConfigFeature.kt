package me.anomz.skyhelper.config

import me.shedaniel.clothconfig2.api.ConfigBuilder
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder

interface ConfigFeature {
    /**
     * A unique key, used for translation keys like
     * "config.skyhelper.category.$key"
     */
    val key: String

    /**
     * Called by the screen builder to add this feature’s entries
     */
    fun addCategory(builder: ConfigBuilder, eb: ConfigEntryBuilder)
}
