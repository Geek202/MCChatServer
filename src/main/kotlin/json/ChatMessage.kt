package me.geek.tom.mcchatserver.json

data class ChatMessage(
    val rule: Int,
    val server: String,
    val room: String,
    val player: String,
    val player_display_name: String,
    val text: String
) {

}