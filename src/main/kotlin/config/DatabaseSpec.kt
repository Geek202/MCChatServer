package me.geek.tom.mcchatserver.config

import com.uchuhimo.konf.ConfigSpec

object DatabaseSpec : ConfigSpec() {
    val host by required<String>()
    val port by required<Int>()
    val username by required<String>()
    val password by required<String>()
    val database by required<String>()
}
