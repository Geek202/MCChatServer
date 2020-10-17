package me.geek.tom.mcchatserver

import com.google.gson.JsonSyntaxException
import com.google.gson.stream.MalformedJsonException
import com.uchuhimo.konf.Config
import com.uchuhimo.konf.source.toml
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import me.geek.tom.mcchatserver.config.DatabaseSpec
import me.geek.tom.mcchatserver.config.ScriptSpec
import me.geek.tom.mcchatserver.config.ServerSpec
import me.geek.tom.mcchatserver.json.ChatMessage
import me.geek.tom.mcchatserver.json.EventData
import me.geek.tom.mcchatserver.scripting.Scripting
import me.geek.tom.mcchatserver.scripting.Timer
import me.geek.tom.mcchatserver.storage.DatabaseQueries
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.sql.PreparedStatement

private val LOGGER = LoggerFactory.getLogger("mcchatserver")

fun Application.main() {
    install(ContentNegotiation) {
        gson {
            setLenient()
        }
    }

    install(Authentication) {
        basic(name = "mc_server_auth") {
            realm = "Access is restricted"
            validate {
                if (it.name == Main.INSTANCE?.config?.get(ServerSpec.mcEndpointUsername)
                    && it.password == Main.INSTANCE?.config?.get(ServerSpec.mcEndpointPassword)) {

                    UserIdPrincipal("Minecraft Server")
                } else {
                    null
                }
            }
        }
    }

    install(StatusPages) {
        exception<JsonSyntaxException> {
            call.respondText("", ContentType.Any, HttpStatusCode.BadRequest)
        }
    }

    routing {
        get("/") {
            call.respondText("Hello, World!", ContentType.Text.Html)
        }

        route("/v1/") {
            route("public/") {
                get("messages") {
                    call.respond(mapOf(
                        "messages" to getQueries().selectAllMessages().executeAsList()
                    ))
                }

                route("events/") {
                    get("all") {
                        call.respond(mapOf(
                            "events" to getQueries().selectAllEvents().executeAsList()
                        ))
                    }

                    get("join") {
                        call.respond(mapOf(
                            "events" to getQueries().selectJoinEvents().executeAsList()
                        ))
                    }

                    get("leave") {
                        call.respond(mapOf(
                            "events" to getQueries().selectLeaveEvents().executeAsList()
                        ))
                    }
                }
            }

            authenticate("mc_server_auth") {
                post("chat") {
                    val message = call.receive<ChatMessage>()
                    LOGGER.info("New message: $message! Checking...")

                    val timer = Timer()
                    timer.start()
                    val ok = Main.INSTANCE?.scripting?.checkMessage(message)!!
                    val time = timer.stop()
                    if (time >= 1000L) {
                        LOGGER.warn("Script execution took over 1000 ms!")
                    }

                    getQueries().insertMessage(
                        message.player_display_name,
                        message.player,
                        message.text,
                        message.server,
                        message.room,
                        ok
                    )

                    call.respond(mapOf("response" to ok))
                }

                post("join") {
                    val event = call.receive<EventData>()
                    LOGGER.info("New join event: $event")
                    getQueries().insertEvent(
                        "join",
                        event.user_id,
                        event.user_display_name,
                        event.server,
                        event.room
                    )
                    call.respond(mapOf("success" to true))
                }

                post("leave") {
                    val event = call.receive<EventData>()
                    LOGGER.info("New leave event: $event")
                    getQueries().insertEvent(
                        "leave",
                        event.user_id,
                        event.user_display_name,
                        event.server,
                        event.room
                    )
                    call.respond(mapOf("success" to true))
                }
            }
        }
    }
}

private fun getQueries(): DatabaseQueries {
    return Main.INSTANCE?.databaseManager?.database?.databaseQueries!!
}

class Main(val databaseManager: DatabaseManager, val config: Config, val scripting: Scripting) {
    companion object {
        var INSTANCE: Main? = null
            internal set
    }
}

fun main(args: Array<String>) {
    Thread.currentThread().contextClassLoader.getResourceAsStream("startup_message.txt")?.use { inStream ->
        InputStreamReader(inStream).use {
            BufferedReader(it).use { reader ->
                reader.lines().forEach(LOGGER::info)
            }
        }
    }

    val config = Config {
        addSpec(ServerSpec)
        addSpec(DatabaseSpec)
        addSpec(ScriptSpec)
    }
        .from.toml.resource("default.toml")
        .from.toml.file("config.toml", true)
    LOGGER.info("Loading server with config: $config")

    val databaseManager = DatabaseManager(
        config[DatabaseSpec.host],
        config[DatabaseSpec.port],
        config[DatabaseSpec.username],
        config[DatabaseSpec.password],
        config[DatabaseSpec.database]
    )

    Main.INSTANCE = Main(databaseManager, config, Scripting(File(config[ScriptSpec.scriptFile])))

    embeddedServer(Netty,
        host = config[ServerSpec.host],
        port = config[ServerSpec.port],
        module = Application::main
    ).start(wait = true)
}
