package me.anomz.skyhelper.features.foraging

import com.google.auto.service.AutoService
import com.google.gson.JsonObject
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

@AutoService(HUDFeature::class)
class ForagingTrackerFeature : HUDFeature {
    override val key = "foraging_tracker"

    private var treesFelled = 0L
    private var foragingXp = 0L
    private var hotfXp = 0L
    private var forestWhispers = 0L
    private var inBonusSection = false
    private val separatorLine = "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"
    private val bonusRegex = Regex("""(.+?) \(([\d.]+%)\)""")
    private val bonusDrops = mutableMapOf<String, Pair<Int, Int>>()
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
        val raw = component.string.trim()
        if (TREE_CUT.matcher(raw).find()) {
            treesFelled++
            saveData()
            return
        }
        if (raw.contains("BONUS GIFT", ignoreCase = true)) {
            inBonusSection = true
            return
        }
        if (inBonusSection) {
            if (raw == separatorLine) {
                inBonusSection = false
                return
            }
            bonusRegex.matchEntire(raw)?.let { match ->
                var name = match.groupValues[1]
                if (name.startsWith("Enchanted Book")) {
                    val bookName = Regex("""Enchanted Book \((.+)\)""").find(name)?.groupValues?.get(1)
                    if (bookName != null) name = bookName
                }
                val color = component.style.color?.rgb ?: 0xFFFFFF // default white
                val (count, _) = bonusDrops[name] ?: (0 to color)
                bonusDrops[name] = (count + 1 to color)
                saveData()
            }
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
                val obj = gson.fromJson(json, JsonObject::class.java)
                treesFelled = obj["treesFelled"]?.asLong ?: 0L
                foragingXp = obj["foragingXp"]?.asLong ?: 0L
                hotfXp = obj["hotfXp"]?.asLong ?: 0L
                forestWhispers = obj["forestWhispers"]?.asLong ?: 0L
                bonusDrops.clear()
                obj["bonusDrops"]?.asJsonObject?.entrySet()?.forEach { (name, el) ->
                    val dropObj = el.asJsonObject
                    val count = dropObj["count"]?.asInt ?: 0
                    val color = dropObj["color"]?.asInt ?: 0xFFFFFF
                    bonusDrops[name] = count to color
                }
            }
        } catch (_: Exception) {
            treesFelled = 0L; foragingXp = 0L; hotfXp = 0L; forestWhispers = 0L; bonusDrops.clear()
        }
    }

    private fun saveData() {
        try {
            if (!Files.exists(configDir)) Files.createDirectories(configDir)
            val data = JsonObject().apply {
                addProperty("treesFelled", treesFelled)
                addProperty("foragingXp", foragingXp)
                addProperty("hotfXp", hotfXp)
                addProperty("forestWhispers", forestWhispers)
                val bonusObj = JsonObject()
                bonusDrops.forEach { (name, pair) ->
                    val (count, color) = pair
                    val dropObj = JsonObject().apply {
                        addProperty("count", count)
                        addProperty("color", color)
                    }
                    bonusObj.add(name, dropObj)
                }
                add("bonusDrops", bonusObj)
            }
            Files.writeString(
                dataFile,
                gson.toJson(data),
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
            )
        } catch (_: Exception) { /* swallow <-- O_O */ }
    }

    fun getDisplayLines(): List<String> = buildList {
        add("§a§lForaging Tracker")
        if (bonusDrops.isNotEmpty()) {
            add("§bBonus Drops:")
            bonusDrops.forEach { (name, pair) ->
                val (count, _) = pair
                add("  §f$name §8(§7x§r$count§8)")
            }
        }
        add("")
        add("§eTrees Felled: §f${String.format("%,d", treesFelled)}")
        add("§eForaging XP: §b${String.format("%,d", foragingXp)}")
        add("§eHOTF XP: §a${String.format("%,d", hotfXp)}")
        add("§eForest Whispers: §b${String.format("%,d", forestWhispers)}")
    }

    override fun createWidgets(): List<AbstractWidget> {
        val config = SkyHelperConfig.instance.foragingTracker
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
                if (!config.enabled) return
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