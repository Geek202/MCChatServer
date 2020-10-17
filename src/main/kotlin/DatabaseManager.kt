package me.geek.tom.mcchatserver

import com.squareup.sqldelight.sqlite.driver.asJdbcDriver
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import me.geek.tom.mcchatserver.storage.Storage

class DatabaseManager(host: String, port: Int, username: String, password: String, database: String) : AutoCloseable {
    private val config: HikariConfig = HikariConfig()
    private val pool: HikariDataSource
    val database: Storage

    init {
        config.jdbcUrl = "jdbc:mariadb://$host:$port/$database"
        config.username = username
        config.password = password

        pool = HikariDataSource(config)
        Storage.Schema.create(pool.asJdbcDriver())
        this.database = Storage.invoke(pool.asJdbcDriver())
    }

    override fun close() {
        pool.close()
    }
}