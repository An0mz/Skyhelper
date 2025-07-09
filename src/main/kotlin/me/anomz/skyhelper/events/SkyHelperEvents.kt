package me.anomz.skyhelper.events

import me.anomz.skyhelper.utils.IslandType
import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory

fun interface IslandChangeListener {
    fun onIslandChange(newIsland: IslandType)
}

object SkyHelperEvents {
    /**
     * Fired any time the parsed island changes.
     * Register with `SkyHelperEvents.ISLAND_CHANGE.register { … }`
     */
    val ISLAND_CHANGE: Event<IslandChangeListener> =
        EventFactory.createArrayBacked(
            IslandChangeListener::class.java
        ) { listeners ->
            IslandChangeListener { island ->
                for (l in listeners) l.onIslandChange(island)
            }
        }
}
