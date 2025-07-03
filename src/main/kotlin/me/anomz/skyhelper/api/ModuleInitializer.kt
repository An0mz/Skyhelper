package me.anomz.skyhelper.api

interface ModuleInitializer {
    /** Called once on client init; do your event‑registration here. */
    fun initModule()
}