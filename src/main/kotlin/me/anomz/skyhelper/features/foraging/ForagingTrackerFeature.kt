package me.anomz.skyhelper.features.foraging

import com.google.auto.service.AutoService
import me.anomz.skyhelper.config.SkyHelperConfig
import me.anomz.skyhelper.hud.HUDFeature
import me.anomz.skyhelper.utils.IslandUtils
import me.anomz.skyhelper.utils.gui.AbstractWidget
import me.anomz.skyhelper.utils.gui.HUDConfigPositions
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.RenderTickCounter
import net.minecraft.text.HoverEvent
import net.minecraft.text.Text
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.util.regex.Pattern
import kotlin.collections.fill
import kotlin.collections.plusAssign
import kotlin.text.get
import kotlin.text.toDouble
import kotlin.times

@AutoService(HUDFeature::class)
class ForagingTrackerFeature : HUDFeature {
    override val key = "foraging_tracker"

    private var treesFelled = 0L
    private var foragingXp = 0L
    private var hotfXp = 0L
    private var forestWhispers = 0L
    private val bonusDrops = mutableListOf<Pair<String, Int>>()
    private val gson = com.google.gson.Gson()
    private val configDir = FabricLoader.getInstance().configDir.resolve("skyhelper")
    private val dataFile = configDir.resolve("foraging_tracker.json")

    companion object {
        private const val PADDING = 4
        private val TREE_CUT = Pattern.compile("helped cut", Pattern.CASE_INSENSITIVE)
        private val XP_LINE = Pattern.compile("Foraging Experience:\\s*(\\d+)", Pattern.CASE_INSENSITIVE)
        private val TOOLTIP_REGEX = Regex("""(.+?) x([\d,]+)(?:[-→]([\d,]+))?""")
    }

    init {
        registerEvents()
    }

    private fun isOnGalatea(): Boolean = IslandUtils.isOnGalatea()
    private fun isHoldingAxe(): Boolean {
        val player = MinecraftClient.getInstance().player ?: return false
        val item = player.mainHandStack
        return item.item.toString().contains("axe", ignoreCase = true)
    }

    private fun registerEvents() {
        loadData()
        ClientTickEvents.END_CLIENT_TICK.register { /* no-op for now */ }
        ClientReceiveMessageEvents.CHAT.register { msg: Text, _, _, _, _ -> handleMessage(msg) }
        ClientReceiveMessageEvents.GAME.register { msg: Text, _ -> handleMessage(msg) }
        ClientPlayConnectionEvents.JOIN.register { _, _, _ -> loadData() }
    }

    private fun handleMessage(component: Text) {
        val raw = component.string
        if (TREE_CUT.matcher(raw).find()) {
            treesFelled++
            saveData()
            return
        }
        if (raw.contains("rewards gained", ignoreCase = true)) {
            val allComponents = listOf(component) + component.siblings
            val hoverText = allComponents
                .mapNotNull { it.style.hoverEvent }
                .filterIsInstance<HoverEvent.ShowText>()
                .mapNotNull { it.value as? Text }
                .firstOrNull()
                ?.string

            if (hoverText != null) {
                hoverText.lines().forEach { line ->
                    TOOLTIP_REGEX.matchEntire(line.trim())?.destructured?.let { (name, minStr, maxStr) ->
                        val min = minStr.replace(",", "").toIntOrNull() ?: return@let
                        val max = maxStr?.replace(",", "")?.toIntOrNull()
                        when (name) {
                            "Foraging Experience" -> {
                                foragingXp += min
                                if (max != null && min != max) foragingXp += max
                            }
                            "HOTF Experience" -> {
                                hotfXp += min
                                if (max != null && min != max) hotfXp += max
                            }
                            "Forest Whispers" -> {
                                forestWhispers += min
                                if (max != null && min != max) forestWhispers += max
                            }
                            else -> {
                                println("ForagingTracker: Unable to find the rewards.")
                            }
                        }
                    }
                }
                saveData()
            }
            return
        }
        XP_LINE.matcher(raw).takeIf { it.find() }?.let {
            val xp = it.group(1).toLong()
            foragingXp += xp
            saveData()
        }
    }

    private fun loadData() {
        try {
            if (Files.exists(dataFile)) {
                val json = Files.readString(dataFile)
                val mapType = object : com.google.gson.reflect.TypeToken<Map<String, Long>>() {}.type
                val data: Map<String, Long> = gson.fromJson(json, mapType)
                treesFelled = data["treesFelled"] ?: 0L
                foragingXp = data["foragingXp"] ?: 0L
                hotfXp = data["hotfXp"] ?: 0L
                forestWhispers = data["forestWhispers"] ?: 0L
            }
        } catch (_: Exception) {
            treesFelled = 0L; foragingXp = 0L; hotfXp = 0L; forestWhispers = 0L
        }
    }

    private fun saveData() {
        try {
            if (!Files.exists(configDir)) Files.createDirectories(configDir)
            val data = mapOf(
                "treesFelled" to treesFelled,
                "foragingXp" to foragingXp,
                "hotfXp" to hotfXp,
                "forestWhispers" to forestWhispers
            )
            Files.writeString(
                dataFile,
                gson.toJson(data),
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
            )
        } catch (_: Exception) { /* swallow <-- wait what O_O */ }
    }

    fun getDisplayLines(): List<String> = buildList {
        add("§a§lForaging Tracker")
        add("§eTrees Felled: §f${String.format("%,d", treesFelled)}")
        add("§eForaging XP: §b${String.format("%,d", foragingXp)}")
        add("§eHOTF XP: §a${String.format("%,d", hotfXp)}")
        add("§eForest Whispers: §b${String.format("%,d", forestWhispers)}")
        if (bonusDrops.isNotEmpty()) {
            add("§bBonus Drops:")
            bonusDrops.forEach { (name, count) ->
                add("  §f$name: §e${String.format("%,d", count)}")
            }
        }
    }

    override fun createWidgets(): List<AbstractWidget> {
        val config = SkyHelperConfig.instance.foragingTracker
        println("ForagingTracker: isOnGalatea=${isOnGalatea()}, isHoldingAxe=${isHoldingAxe()}, onlyHoldingAxe=${config.onlyHoldingAxe}")
        val (startX, startY) = HUDConfigPositions.positions[key] ?: (10 to 10)
        return listOf(object : AbstractWidget(key) {
            init { x = startX; y = startY }
            private fun measure(): Pair<Int, Int> {
                val lines = getDisplayLines()
                val fr = MinecraftClient.getInstance().textRenderer
                val w = lines.maxOf { fr.getWidth(it) } + PADDING * 2
                val h = fr.fontHeight * lines.size + PADDING * 2
                return w to h
            }

            fun isMouseOver(mouseX: Double, mouseY: Double): Boolean {
                val (w, h) = measure()
                return mouseX in x.toDouble()..(x + w).toDouble() &&
                        mouseY in y.toDouble()..(y + h).toDouble()
            }

            fun onMouseClick(mouseX: Double, mouseY: Double, button: Int): Boolean {
                return isMouseOver(mouseX, mouseY)
            }

            fun draw(context: DrawContext, mouseX: Int, mouseY: Int) {
                val lines = getDisplayLines()
                val fr = MinecraftClient.getInstance().textRenderer
                val (w, h) = measure()
                val bg = 0x88000000.toInt()
                context.fill(x, y, x + w, y + h, bg)
                var yy = y + PADDING
                lines.forEach { line ->
                    context.drawText(fr, Text.literal(line), x + PADDING, yy, 0xFFFFFF, false)
                    yy += fr.fontHeight
                }
            }

            override fun render(
                ms: DrawContext,
                mouseX: Int,
                mouseY: Int,
                delta: RenderTickCounter?
            ) {
                val config = SkyHelperConfig.instance.foragingTracker
                val editMode = me.anomz.skyhelper.hud.HudEditManager.editMode
                // Only draw if in edit mode, or if on Galatea and (if config.onlyHoldingAxe) holding an axe
                if (editMode || (isOnGalatea() && (!config.onlyHoldingAxe || isHoldingAxe()))) {
                    draw(ms, mouseX, mouseY)
                }
            }

            override fun contains(mx: Double, my: Double): Boolean {
                return isMouseOver(mx, my)
            }
        })
    }
}