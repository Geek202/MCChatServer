package me.geek.tom.mcchatserver.json

data class EventData(
    val server: String,
    val room: String,
    val user_id: String,
    val user_display_name: String
) {
}