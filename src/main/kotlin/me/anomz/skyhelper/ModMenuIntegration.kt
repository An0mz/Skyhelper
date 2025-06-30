package me.anomz.skyhelper

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import net.minecraft.client.gui.screen.Screen
import me.anomz.skyhelper.gui.SkyHelperConfigScreen

class ModMenuIntegration : ModMenuApi {
    override fun getModConfigScreenFactory(): ConfigScreenFactory<Screen> =
        ConfigScreenFactory { parent: Screen ->
            SkyHelperConfigScreen.create(parent)
        }
}