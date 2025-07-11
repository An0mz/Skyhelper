package me.anomz.skyhelper.config

import com.google.gson.GsonBuilder
import net.fabricmc.loader.api.FabricLoader
import java.nio.file.Files
import java.nio.file.Path
import me.anomz.skyhelper.config.features.foraging.SeaLumiesConfig
import me.anomz.skyhelper.config.features.tooltip.ScrollableTooltipConfig
import me.anomz.skyhelper.config.ConfigFeature

/**
 * Root configuration container for SkyHelper.
 *
 * Holds per-feature config sections and provides load/save utilities.
 */
data class SkyHelperConfig(
    /** Foraging: Sea Lumies node highlight settings */
    var seaLumies: SeaLumiesConfig = SeaLumiesConfig(),
    /** Tooltip enhancement settings */
    var scrollableTooltip: ScrollableTooltipConfig = ScrollableTooltipConfig(),
    val hudPositions: MutableMap<String, Pair<Int, Int>> = mutableMapOf()
    // Add new feature configs here...
) {
    /**
     * Dynamically discovers all feature configs (implementing ConfigFeature)
     * by scanning this instance's fields.
     */
    val features: List<ConfigFeature>
        get() = this::class.java.declaredFields
            .mapNotNull { field ->
                field.apply { isAccessible = true }
                field.get(this) as? ConfigFeature
            }

    companion object {
        private val GSON = GsonBuilder().setPrettyPrinting().create()
        private val PATH: Path = FabricLoader
            .getInstance()
            .configDir
            .resolve("skyhelper.json")

        /** The one and only live config. */
        lateinit var instance: SkyHelperConfig
            private set

        init {
            load()
        }

        /**
         * Reads `skyhelper.json` into [instance].
         * If missing or malformed, writes defaults.
         */
        fun load() {
            try {
                PATH.parent?.let { parent ->
                    if (!Files.exists(parent)) Files.createDirectories(parent)
                }

                instance = if (Files.exists(PATH)) {
                    Files.newBufferedReader(PATH).use { reader ->
                        GSON.fromJson(reader, SkyHelperConfig::class.java)
                    }
                } else {
                    // write defaults
                    SkyHelperConfig().also { defaults ->
                        instance = defaults
                        save()
                    }
                }
            } catch (t: Throwable) {
                // fallback to defaults on any error
                instance = SkyHelperConfig()
                try { save() } catch (_: Throwable) { }
            }
        }

        /** Persists the current [instance] back to disk. */
        fun save() {
            try {
                PATH.parent?.let { parent ->
                    if (!Files.exists(parent)) Files.createDirectories(parent)
                }
                Files.newBufferedWriter(PATH).use { writer ->
                    GSON.toJson(instance, writer)
                }
            } catch (_: Throwable) {
                // ignore write failures
            }
        }
    }
}
