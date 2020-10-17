package me.geek.tom

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import io.ktor.application.*
import io.ktor.http.*
import kotlin.test.*
import io.ktor.server.testing.*
import me.geek.tom.mcchatserver.json.ChatMessage
import me.geek.tom.mcchatserver.main

private val GSON = GsonBuilder().create()

class ApplicationTest {

    @Test
    fun testChatEndpoint() {
        withTestApplication(Application::main) {
            val obj = ChatMessage(0, "test", "Chat", "test", "asdfsadf", "test message!")
            val msg = GSON.toJson(obj)
            println("MSG: $msg")
            handleRequest(HttpMethod.Post, "/v1/chat") {

                addHeader(HttpHeaders.Authorization, "Basic dGVzdHVzZXI6dGVzdHBhc3M=")
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(msg)

            }.response.let {
                assertEquals(HttpStatusCode.OK, it.status())
                val res = GSON.fromJson(it.content, JsonObject::class.java)
                assertEquals(true, res.get("response").asBoolean)
                assertEquals(1, res.entrySet().size)
            }
        }
    }

}
