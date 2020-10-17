package me.geek.tom.mcchatserver.scripting

class Timer {
    private var startTime = -1L
    private var endTime = -1L

    fun start() {
        startTime = System.currentTimeMillis()
    }

    fun stop(): Long {
        endTime = System.currentTimeMillis()
        return endTime - startTime
    }
}