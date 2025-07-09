package me.anomz.skyhelper.utils

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import me.anomz.skyhelper.events.SkyHelperEvents
import net.azureaaron.hmapi.events.HypixelPacketEvents
import net.azureaaron.hmapi.network.HypixelNetworking
import net.azureaaron.hmapi.network.packet.s2c.HypixelS2CPacket
import net.azureaaron.hmapi.network.packet.v1.s2c.LocationUpdateS2CPacket
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.network.packet.CustomPayload
import net.minecraft.text.Text

object IslandUtils {
    var currentIsland: IslandType = IslandType.UNKNOWN
        private set

    fun init() {
        HypixelNetworking.registerToEvents(
            Object2IntOpenHashMap<CustomPayload.Id<HypixelS2CPacket>>().apply {
                put(LocationUpdateS2CPacket.ID, 1)
            }
        )

        HypixelPacketEvents.LOCATION_UPDATE.register { packet: HypixelS2CPacket ->
            if (packet is LocationUpdateS2CPacket) {
                onLocationPacket(packet)
            }
        }

        ClientReceiveMessageEvents.GAME.register { text: Text, _ ->
            val msg = text.string
            if (msg.startsWith("{") && msg.contains("\"map\"")) {
                try {
                    val json = com.google.gson.JsonParser.parseString(msg).asJsonObject
                    val raw = json.get("map").asString
                    val island = IslandType.fromRawMode(raw)
                    if (island != currentIsland) {
                        currentIsland = island
                        SkyHelperEvents.ISLAND_CHANGE.invoker().onIslandChange(island)
                    }
                } catch (_: Exception) {
                    // ignore
                }
            }
        }
    }

    private fun onLocationPacket(pkt: LocationUpdateS2CPacket) {
        val humanName = pkt.map.orElse("")
        val rawTag    = pkt.mode.orElse("")
        val islandRaw = if (humanName.isNotBlank()) humanName else rawTag

        val island = IslandType.fromRawMode(islandRaw)
        if (island != currentIsland) {
            currentIsland = island
            SkyHelperEvents.ISLAND_CHANGE.invoker().onIslandChange(island)
        }

    }

    fun isOnGalatea()       = currentIsland == IslandType.GALATEA
    fun isOnCrimsonIsle() = currentIsland == IslandType.CRIMSON_ISLE
    fun isOnTheEnd()        = currentIsland == IslandType.THE_END
    fun isOnHub()           = currentIsland == IslandType.HUB
    fun isOnDwarvenMines()  = currentIsland == IslandType.DWARVEN_MINES
    fun isOnTheRift()       = currentIsland == IslandType.THE_RIFT
    fun isOnGarden()        = currentIsland == IslandType.GARDEN
    fun isOnKuudrasHollow() = currentIsland == IslandType.KUUDRAS_HOLLOW
    fun isOnDungeon()       = currentIsland == IslandType.DUNGEON
    fun isOnPrivateIsland() = currentIsland == IslandType.PRIVATE_ISLAND
    fun isOnThePark()       = currentIsland == IslandType.THE_PARK
    fun isOnGlaciteMineshaft() = currentIsland == IslandType.GLACITE_MINESHAFT
    fun isOnGoldMine()      = currentIsland == IslandType.GOLD_MINE
    fun isOnDeepCaverns()   = currentIsland == IslandType.DEEP_CAVERNS
    fun isOnBackwaterBayou() = currentIsland == IslandType.BACKWATER_BAYOU
    fun isOnTheBarn()       = currentIsland == IslandType.THE_BARN
    fun isOnDungeonHub()    = currentIsland == IslandType.DUNGEON_HUB
    fun isOnSpidersDen()    = currentIsland == IslandType.SPIDERS_DEN
    fun isOnFarmingIslands() = currentIsland == IslandType.FARMING_ISLANDS
}