package me.geek.tom.mcchatserver.config

import com.uchuhimo.konf.ConfigSpec

object ScriptSpec : ConfigSpec() {
    val scriptFile by required<String>()
}
