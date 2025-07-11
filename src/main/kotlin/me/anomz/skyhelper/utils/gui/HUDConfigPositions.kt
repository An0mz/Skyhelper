package me.anomz.skyhelper.utils.gui

import com.google.gson.Gson
import me.anomz.skyhelper.utils.gui.AbstractWidget
import java.io.File

object HUDConfigPositions {
    private val gson = Gson()
    private val configFile = File("config/skyhelper_hud.json")

    fun save(widgets: List<AbstractWidget>) {
        val data = widgets.associate { it.featureKey to (it.x to it.y) }
        configFile.writeText(gson.toJson(data))
    }

    fun load(widgets: List<AbstractWidget>) {
        if (!configFile.exists()) return
        val map: Map<String, List<Double>> = gson.fromJson(
            configFile.readText(), Map::class.java
        ) as Map<String, List<Double>>
        widgets.forEach { w ->
            (map[w.featureKey]?.let { (x, y) ->
                w.x = x.toInt()
                w.y = y.toInt()
            })
        }
    }
}