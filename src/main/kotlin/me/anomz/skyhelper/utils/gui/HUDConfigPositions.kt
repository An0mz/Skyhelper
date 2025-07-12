package me.anomz.skyhelper.utils.gui

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

object HUDConfigPositions {
    private val gson = Gson()
    private val configFile = File("config/skyhelper_hud.json")
    val positions: MutableMap<String, Pair<Int, Int>> = mutableMapOf()

    fun save(widgets: List<AbstractWidget>) {
        val data = widgets.associate { it.featureKey to listOf(it.x.toDouble(), it.y.toDouble()) }
        configFile.writeText(gson.toJson(data))
        positions.clear()
        widgets.forEach { positions[it.featureKey] = it.x to it.y }
    }

    fun load(widgets: List<AbstractWidget>) {
        if (!configFile.exists()) return
        val type = object : TypeToken<Map<String, List<Double>>>() {}.type
        val map: Map<String, List<Double>> = gson.fromJson(configFile.readText(), type)
        positions.clear()
        widgets.forEach { w ->
            map[w.featureKey]?.let { (x, y) ->
                w.x = x.toInt()
                w.y = y.toInt()
                positions[w.featureKey] = w.x to w.y
            }
        }
    }
}