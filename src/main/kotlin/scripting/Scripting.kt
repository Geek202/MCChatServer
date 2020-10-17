package me.geek.tom.mcchatserver.scripting

import me.geek.tom.mcchatserver.json.ChatMessage
import org.mozilla.javascript.Context
import org.mozilla.javascript.Function
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.charset.Charset

private val LOGGER = LoggerFactory.getLogger("mcchatserver.scripting")

class Scripting(private val scriptFile: File) {
    fun checkMessage(message: ChatMessage): Boolean {
        val ctx = Context.enter()!!
        ctx.setClassShutter(Companion::checkClass)
        val scope = ctx.initStandardObjects()!!

        scriptFile.reader(Charset.defaultCharset()).use { `is` ->
            ctx.evaluateReader(scope, `is`, scriptFile.name, 1, null)
            val checkMessage = scope.get("checkMessage", scope)
            if (checkMessage !is Function) {
                LOGGER.warn("Script: $scriptFile does not provide a function called checkMessage!")
                return true
            }

            val res = checkMessage.call(ctx, scope, scope, arrayOf(message))
            Context.exit()
            return res as Boolean
        }
    }

    companion object {
        private val allowed = listOf(
            "me.geek.tom.mcchatserver.json.ChatMessage",
            "java.lang.String",
            "java.lang.System",
            "java.io.PrintStream"
        )

        fun checkClass(c: String): Boolean {
            return c in allowed
        }
    }
}
