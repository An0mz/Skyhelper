package me.anomz.skyhelper.utils

enum class IslandType {
    GALATEA,
    CRIMSON_ISLE,
    THE_END,
    HUB,
    DWARVEN_MINES,
    THE_RIFT,
    GARDEN,
    KUUDRAS_HOLLOW,
    DUNGEON,
    PRIVATE_ISLAND,
    THE_PARK,
    GLACITE_MINESHAFT,
    GOLD_MINE,
    DEEP_CAVERNS,
    BACKWATER_BAYOU,
    THE_BARN,
    DUNGEON_HUB,
    SPIDERS_DEN,
    FARMING_ISLANDS,
    UNKNOWN;

    fun displayName(): String =
        when (this) {
            SPIDERS_DEN -> "Spider's Den"
            FARMING_ISLANDS -> "The Farming Islands"
            else -> name.split('_')
                .joinToString(" ") { word ->
                    word.lowercase().replaceFirstChar { it.uppercase() }
                }
        }

    companion object {
        fun fromRawMode(raw: String): IslandType {
            values().firstOrNull { it.name.equals(raw.replace("'", ""), ignoreCase = true) }?.let { return it }
            values().firstOrNull { it.displayName().equals(raw, ignoreCase = true) }?.let { return it }
            return UNKNOWN
        }
    }
}