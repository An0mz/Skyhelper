package me.anomz.skyhelper.config

import com.google.gson.GsonBuilder
import net.fabricmc.loader.api.FabricLoader
import java.nio.file.Files
import java.nio.file.Path
import me.anomz.skyhelper.config.features.foraging.SeaLumiesConfig
import me.anomz.skyhelper.config.features.tooltip.ScrollableTooltipConfig

data class SkyHelperConfig(
    var seaLumies: SeaLumiesConfig = SeaLumiesConfig(),
    var scrollableTooltip: ScrollableTooltipConfig = ScrollableTooltipConfig()
    // future sections…
) {
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
         * If the file doesn’t exist, or any error occurs, we create+write defaults.
         */
        fun load() {
            try {
                // ensure parent folder exists
                PATH.parent?.let { parent ->
                    if (!Files.exists(parent)) Files.createDirectories(parent)
                }

                instance = if (Files.exists(PATH)) {
                    // attempt to read
                    Files.newBufferedReader(PATH).use { reader ->
                        GSON.fromJson(reader, SkyHelperConfig::class.java)
                    }
                } else {
                    // write defaults
                    SkyHelperConfig().also { defaults ->
                        instance = defaults
                        save()  // writes skyhelper.json
                    }
                }
            } catch (t: Throwable) {
                // if anything at all fails, fall back to defaults and overwrite file
                instance = SkyHelperConfig()
                try {
                    save()
                } catch (_: Throwable) { /* silent */ }
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
                // swallow—nothing we can do
            }
        }
    }
}