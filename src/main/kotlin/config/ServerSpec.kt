package me.geek.tom.mcchatserver.config

import com.uchuhimo.konf.ConfigSpec

object ServerSpec : ConfigSpec() {
    val host by optional("0.0.0.0")
    val port by required<Int>()

    val mcEndpointUsername by optional("server")
    val mcEndpointPassword by optional("minecraft")
}
